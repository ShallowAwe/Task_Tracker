package com.rudra.sessionbased_task_tracker.sprint.dto;

import com.rudra.sessionbased_task_tracker.sprint.entity.SprintStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String name;
    private String goal;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalTickets;
    private int completedTickets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}