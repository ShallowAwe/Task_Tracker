package com.rudra.sessionbased_task_tracker.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(max = 5000, message = "Comment too long")
    private String content;
}