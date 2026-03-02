package com.rudra.sessionbased_task_tracker.ticket.service;

import com.rudra.sessionbased_task_tracker.ticket.dto.CreateTicketRequest;
import com.rudra.sessionbased_task_tracker.ticket.dto.TicketResponse;
import com.rudra.sessionbased_task_tracker.ticket.dto.UpdateTicketRequest;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;

import java.util.List;

public interface TicketService {

    TicketResponse createTicket(Long projectId, CreateTicketRequest request, Long currentUserId);

    List<TicketResponse> getTicketsByProject(Long projectId, Long currentUserId);

    TicketResponse getTicketById(Long projectId, Long ticketId, Long currentUserId);

    TicketResponse updateTicket(Long projectId, Long ticketId, UpdateTicketRequest request, Long currentUserId);

    TicketResponse updateTicketStatus(Long projectId, Long ticketId, TicketStatus newStatus, Long currentUserId);

    TicketResponse assignTicket(Long projectId, Long ticketId, Long assigneeId, Long currentUserId);

    void deleteTicket(Long projectId, Long ticketId, Long currentUserId);
}