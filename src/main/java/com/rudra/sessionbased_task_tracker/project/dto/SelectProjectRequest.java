package com.rudra.sessionbased_task_tracker.project.dto;

import jakarta.validation.constraints.NotBlank;

public record SelectProjectRequest(
        @NotBlank(message = "Project key is required")
        String projectKey
) {
}
