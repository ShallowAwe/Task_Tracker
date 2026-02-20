package com.rudra.sessionbased_task_tracker.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String key;
    private String name;
    private String description;
    private Long ownerId;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}