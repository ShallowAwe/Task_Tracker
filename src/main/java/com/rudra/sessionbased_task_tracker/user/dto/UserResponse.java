package com.rudra.sessionbased_task_tracker.user.dto;

import com.rudra.sessionbased_task_tracker.user.entity.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        String avatar,
        LocalDateTime createdAt
) {
    public static UserResponse from(com.rudra.sessionbased_task_tracker.user.entity.User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatar(),
                user.getCreatedAt()
        );
    }
}
