package com.rudra.sessionbased_task_tracker.auth.service;

import com.rudra.sessionbased_task_tracker.auth.dto.LoginRequest;
import com.rudra.sessionbased_task_tracker.auth.dto.RegisterUser;
import com.rudra.sessionbased_task_tracker.auth.entity.RefreshToken;
import com.rudra.sessionbased_task_tracker.auth.exception.InvalidTokenException;
import com.rudra.sessionbased_task_tracker.auth.repository.RefreshTokenRepository;
import com.rudra.sessionbased_task_tracker.common.dto.AuthResponse;
import com.rudra.sessionbased_task_tracker.common.security.JwtTokenProvider;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import com.rudra.sessionbased_task_tracker.user.exception.UserAlreadyExistsException;
import com.rudra.sessionbased_task_tracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long REFRESH_TOKEN_DAYS = 7;

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse register(RegisterUser dto) {
        if (userService.checkIfUserExists(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail());
        }

        User newUser = new User();
        newUser.setEmail(dto.getEmail());
        newUser.setName(dto.getName());
        newUser.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        newUser.setCreatedAt(LocalDateTime.now());

        User savedUser = userService.createUser(newUser);
        return generateTokensAndPersistSession(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userService.getUser(request.getEmail());
        return generateTokensAndPersistSession(user);
    }

    /**
     * Refresh token rotation: validates the incoming refresh token, revokes it,
     * and issues a brand-new access + refresh token pair. If the same refresh
     * token is presented twice, the second attempt will fail because the token
     * is now revoked — a strong signal of token theft.
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (storedToken.isRevoked()) {
            // Token reuse detected — possible theft. Consider revoking ALL
            // refresh tokens for this user as a defensive measure.
            log.warn("Revoked refresh token presented for user {}", storedToken.getUserId());
            throw new InvalidTokenException("Refresh token revoked");
        }

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }

        // Rotate: revoke the old token and issue a new pair.
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userService.getUserById(userId);

        return generateTokensAndPersistSession(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMe(String token) {
        if (token == null || !jwtTokenProvider.isAccessToken(token)) {
            throw new InvalidTokenException("Invalid access token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userService.getUserById(userId);

        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName());
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!storedToken.isRevoked()) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
        }
    }

    private AuthResponse generateTokensAndPersistSession(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken session = new RefreshToken();
        session.setToken(refreshToken);
        session.setUserId(user.getId());
        session.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS));
        session.setRevoked(false);

        refreshTokenRepository.save(session);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}