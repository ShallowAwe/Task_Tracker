package com.rudra.sessionbased_task_tracker.Service;

import com.rudra.sessionbased_task_tracker.Domain.RefreshToken;
import com.rudra.sessionbased_task_tracker.Domain.User;
import com.rudra.sessionbased_task_tracker.Dto.LoginRequest;
import com.rudra.sessionbased_task_tracker.Dto.RegisterUser;
import com.rudra.sessionbased_task_tracker.Exception.InvalidTokenException;
import com.rudra.sessionbased_task_tracker.Exception.UserAlreadyExistsException;
import com.rudra.sessionbased_task_tracker.Repository.RefreshTokenRepository;
import com.rudra.sessionbased_task_tracker.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public Map<String, String> register(RegisterUser dto) {
        if (userService.checkIfUserExists(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail());
        }

        User newUser = new User();
        newUser.setEmail(dto.getEmail());
        newUser.setName(dto.getName());
        newUser.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
        newUser.setCreated_at(LocalDateTime.now());

        User savedUser = userService.createUser(newUser);
        return generateTokensAndPersistSession(savedUser);
    }

    public Map<String, String> login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userService.getUser(request.getEmail());
        return generateTokensAndPersistSession(user);
    }

    public Map<String, String> refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        return Map.of("accessToken", newAccessToken);
    }

    public Map<String, Object> getMe(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new InvalidTokenException("Invalid token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userService.getUserById(userId);

        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName()
        );
    }

    public void logout(String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidTokenException("Refresh token is required");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
    }

    private Map<String, String> generateTokensAndPersistSession(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken session = new RefreshToken();
        session.setToken(refreshToken);
        session.setUserId(user.getId());
        session.setExpiryDate(LocalDateTime.now().plusDays(7));
        session.setRevoked(false);

        refreshTokenRepository.save(session);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }
}