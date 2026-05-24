package com.rudra.sessionbased_task_tracker.user.dto;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String avatar
) {}