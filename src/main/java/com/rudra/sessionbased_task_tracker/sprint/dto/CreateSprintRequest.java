package com.rudra.sessionbased_task_tracker.sprint.dto;

import com.rudra.sessionbased_task_tracker.sprint.entity.SprintStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSprintRequest {
    @NotBlank
    private String name;

    @Size(max = 200)
    private String goal;
    @NotNull
    @FutureOrPresent
    private LocalDate startDate;
    @NotNull
    @Future
    private LocalDate endDate;
}