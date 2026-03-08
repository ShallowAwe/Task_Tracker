package com.rudra.sessionbased_task_tracker.ticket.dto;

import com.rudra.sessionbased_task_tracker.ticket.entity.Ticket;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketPriority;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateTicketRequest {

    @Size(max = 255, message = "Tittle must not exceed 255 characters")
    private String title;
    @Size(max = 2000, message = "Tittle must not exceed 2000 characters")
    private String description;
    private TicketPriority priority;
    private LocalDateTime dueDate;
}