package com.rudra.sessionbased_task_tracker.ticket.dto;


import com.rudra.sessionbased_task_tracker.ticket.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private TicketPriority priority = TicketPriority.MEDIUM;

    private Long assigneeId; // optional
}