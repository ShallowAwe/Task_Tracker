package com.rudra.sessionbased_task_tracker.sprint.dto;

import com.rudra.sessionbased_task_tracker.sprint.entity.SprintStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSprintRequest {
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
}