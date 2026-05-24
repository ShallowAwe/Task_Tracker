package com.rudra.sessionbased_task_tracker.user.controller;

import com.rudra.sessionbased_task_tracker.user.dto.UpdateProfileRequest;
import com.rudra.sessionbased_task_tracker.user.dto.UserProfileResponse;
import com.rudra.sessionbased_task_tracker.user.dto.UserResponse;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import com.rudra.sessionbased_task_tracker.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> listAll(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.listAll(pageable));
    }



    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Long userId) {

        User updatedUser = userService.updateUserById(
                userId,
                request.getEmail(),
                request.getName()

        );

        UserProfileResponse response = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getAvatar()
        );

        return ResponseEntity.ok(response);
    }

    //update profile avatar
    @PostMapping("/updateAvatar")
    public ResponseEntity<UserProfileResponse> updateAvatar(
             @RequestParam MultipartFile file,
                @AuthenticationPrincipal Long userId
            ){

        User updatedUser = userService.updateAvatar(file, userId);
        UserProfileResponse response = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getAvatar()
        );
        return ResponseEntity.ok(response);
    }

}
