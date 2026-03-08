package com.rudra.sessionbased_task_tracker.ticket.dto;

import com.rudra.sessionbased_task_tracker.ticket.entity.TicketPriority;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TicketResponse {

    private Long id;

    private Long projectId;
    private String projectKey;

    private String title;
    private String description;

    private TicketStatus status;
    private TicketPriority priority;

    private Long reporterId;
    private String reporterName;

    private Long assigneeId;
    private String assigneeName;

    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}