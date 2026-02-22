package com.rudra.sessionbased_task_tracker.auth.controller;

import com.rudra.sessionbased_task_tracker.auth.dto.LoginRequest;
import com.rudra.sessionbased_task_tracker.auth.dto.RegisterUser;
import com.rudra.sessionbased_task_tracker.auth.service.AuthService;
import com.rudra.sessionbased_task_tracker.common.dto.AuthResponse;
import com.rudra.sessionbased_task_tracker.common.dto.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.refresh(request.get("refreshToken")));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String header) {
        return ResponseEntity.ok(authService.getMe(header.replace("Bearer ", "")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        authService.logout(request.get("refreshToken"));
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}