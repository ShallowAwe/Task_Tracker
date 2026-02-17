package com.rudra.sessionbased_task_tracker.Controller;

import com.rudra.sessionbased_task_tracker.Domain.RefreshToken;
import com.rudra.sessionbased_task_tracker.Domain.User;
import com.rudra.sessionbased_task_tracker.Dto.LoginRequest;
import com.rudra.sessionbased_task_tracker.Dto.RegisterUser;
import com.rudra.sessionbased_task_tracker.Repository.RefreshTokenRepository;
import com.rudra.sessionbased_task_tracker.Security.JwtTokenProvider;
import com.rudra.sessionbased_task_tracker.Service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // ✅ Extracted shared logic into a helper method
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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUser registerUser) {

        if (userService.checkIfUserExists(registerUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "User already exists"));
        }

        User newUser = new User();
        newUser.setEmail(registerUser.getEmail());
        newUser.setName(registerUser.getName());
        newUser.setPassword_hash(passwordEncoder.encode(registerUser.getPassword()));
        newUser.setCreated_at(LocalDateTime.now());

        userService.createUser(newUser);

        // ✅ Reusing the helper instead of duplicating token logic
        Map<String, String> tokens = generateTokensAndPersistSession(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userService.getUser(request.getEmail());
        Map<String, String> tokens = generateTokensAndPersistSession(user);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid refresh token"));
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (storedToken.isRevoked() ||
                storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token expired or revoked"));
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        return ResponseEntity.ok(
                Map.of("accessToken", newAccessToken)
        );
    }
    @GetMapping("/me")
    public ResponseEntity<?> getUserDetails(
            @RequestHeader("Authorization") String header) {

        String token = header.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        User user = userService.getUserById(userId);

        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getName()
                )
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Refresh token required"));
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }


}