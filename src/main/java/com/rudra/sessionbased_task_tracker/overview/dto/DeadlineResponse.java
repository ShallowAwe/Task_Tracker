package com.rudra.sessionbased_task_tracker.overview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineResponse {

    private Long id;
    private String key;
    private String title;
    private LocalDateTime dueDate;
    private boolean overdue;
}