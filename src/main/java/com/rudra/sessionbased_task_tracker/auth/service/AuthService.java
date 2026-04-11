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
import java.util.UUID;

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
        // New login → brand-new family
        return generateTokensAndPersistSession(savedUser, UUID.randomUUID());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userService.getUser(request.getEmail());
        // New login → brand-new family
        return generateTokensAndPersistSession(user, UUID.randomUUID());
    }

    /**
     * Refresh token rotation with reuse detection.
     *
     * Rotation: each successful refresh invalidates the presented token and
     * issues a new pair within the SAME family.
     *
     * Reuse detection: if a token is presented that has already been revoked,
     * we treat the entire family as compromised and revoke every token in it.
     * This logs out the attacker AND the legitimate client (who must
     * re-authenticate), which is the correct response to suspected theft.
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        // === REUSE DETECTION ===
        if (storedToken.isRevoked()) {
            log.error("SECURITY ALERT: Refresh token reuse detected for user {} family {}. " +
                            "Revoking entire token family.",
                    storedToken.getUserId(), storedToken.getFamilyId());
            refreshTokenRepository.revokeAllByFamilyId(storedToken.getFamilyId());
            throw new InvalidTokenException(
                    "Refresh token reuse detected. All sessions in this family have been revoked.");
        }

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new InvalidTokenException("Refresh token expired");
        }

        // Rotate within the same family
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = userService.getUserById(storedToken.getUserId());

        return generateTokensAndPersistSession(user, storedToken.getFamilyId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMe(String token) {
        if (token == null || !jwtTokenProvider.isAccessToken(token)) {
            throw new InvalidTokenException("Invalid access token");
        }

        Long userId = jwtTokenProvider.getUserIdFromAccessToken(token);
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

        // Revoke the entire family on logout — this device's whole rotation chain.
        // If you'd rather only kill the current token, swap this for the old behavior.
        refreshTokenRepository.revokeAllByFamilyId(storedToken.getFamilyId());
    }

    /** Optional: nuclear option for "log out everywhere" / password change. */
    @Transactional
    public void revokeAllSessionsForUser(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private AuthResponse generateTokensAndPersistSession(User user, UUID familyId) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken session = new RefreshToken();
        session.setToken(refreshToken);
        session.setUserId(user.getId());
        session.setFamilyId(familyId);
        session.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS));
        session.setRevoked(false);

        refreshTokenRepository.save(session);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}