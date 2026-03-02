package com.rudra.sessionbased_task_tracker.ticket.controller;

import com.rudra.sessionbased_task_tracker.common.dto.MessageResponse;
import com.rudra.sessionbased_task_tracker.ticket.dto.CreateTicketRequest;
import com.rudra.sessionbased_task_tracker.ticket.dto.TicketResponse;
import com.rudra.sessionbased_task_tracker.ticket.dto.UpdateTicketRequest;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;
import com.rudra.sessionbased_task_tracker.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTicketRequest request,
            @RequestAttribute("userId") Long currentUserId) {

        TicketResponse response =
                ticketService.createTicket(projectId, request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getTickets(
            @PathVariable Long projectId,
            @RequestAttribute("userId") Long currentUserId) {

        return ResponseEntity.ok(
                ticketService.getTicketsByProject(projectId, currentUserId)
        );
    }


    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @RequestAttribute("userId") Long currentUserId) {

        return ResponseEntity.ok(
                ticketService.getTicketById(projectId, ticketId, currentUserId)
        );
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request,
            @RequestAttribute("userId") Long currentUserId) {

        return ResponseEntity.ok(
                ticketService.updateTicket(projectId, ticketId, request, currentUserId)
        );
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status,
            @RequestAttribute("userId") Long currentUserId) {

        return ResponseEntity.ok(
                ticketService.updateTicketStatus(projectId, ticketId, status, currentUserId)
        );
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @RequestParam Long assigneeId,
            @RequestAttribute("userId") Long currentUserId) {

        return ResponseEntity.ok(
                ticketService.assignTicket(projectId, ticketId, assigneeId, currentUserId)
        );
    }
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<MessageResponse> deleteTicket(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @RequestAttribute("userId") Long currentUserId) {

        ticketService.deleteTicket(projectId, ticketId, currentUserId);

        return ResponseEntity.ok(
                new MessageResponse("Ticket deleted successfully")
        );
    }
}
