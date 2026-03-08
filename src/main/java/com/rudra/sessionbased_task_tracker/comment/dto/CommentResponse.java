package com.rudra.sessionbased_task_tracker.comment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private Long ticketId;

    private Long authorId;
    private String authorName;

    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}