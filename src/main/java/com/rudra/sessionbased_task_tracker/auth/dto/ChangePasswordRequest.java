package com.rudra.sessionbased_task_tracker.auth.dto;

import jakarta.validation.constraints.*;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,

        @NotBlank
        @Size(min = 8, max = 64,
                message = "Password must be between 8 and 64 chars")
        String newPassword,

        @NotBlank String confirmPassword
) {
    @AssertTrue(message = "New passwords do not match")
    boolean isPasswordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
