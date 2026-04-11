package com.rudra.sessionbased_task_tracker.auth.controller;

import com.rudra.sessionbased_task_tracker.auth.dto.ChangePasswordRequest;
import com.rudra.sessionbased_task_tracker.auth.dto.LoginRequest;
import com.rudra.sessionbased_task_tracker.auth.dto.RefreshTokenRequest;
import com.rudra.sessionbased_task_tracker.auth.dto.RegisterUser;
import com.rudra.sessionbased_task_tracker.auth.exception.InvalidTokenException;
import com.rudra.sessionbased_task_tracker.auth.exception.MissingAuthHeaderException;
import com.rudra.sessionbased_task_tracker.auth.service.AuthService;
import com.rudra.sessionbased_task_tracker.common.dto.AuthResponse;
import com.rudra.sessionbased_task_tracker.common.dto.MessageResponse;
import com.rudra.sessionbased_task_tracker.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUser dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @GetMapping("/getMe")
    public ResponseEntity<?> getMe(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MissingAuthHeaderException("Missing or malformed Authorization header");
        }
        String token = header.substring("Bearer ".length()).trim();
        return ResponseEntity.ok(authService.getMe(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal Long userId) {

        userService.changePassword(userId, request.currentPassword(), request.newPassword());

        // Revoke all existing sessions — forces re-login with new password
        authService.revokeAllSessionsForUser(userId);

        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

}