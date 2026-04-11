package com.rudra.sessionbased_task_tracker.sprint.controller;

import com.rudra.sessionbased_task_tracker.common.dto.MessageResponse;
import com.rudra.sessionbased_task_tracker.sprint.dto.CreateSprintRequest;
import com.rudra.sessionbased_task_tracker.sprint.dto.SprintResponse;
import com.rudra.sessionbased_task_tracker.sprint.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        value = "/api/projects/{projectId}/sprints",
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateSprintRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        SprintResponse created = sprintService.createSprint(projectId, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SprintResponse>> getProjectSprints(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(sprintService.getProjectSprints(projectId, currentUserId));
    }

    @GetMapping("/{sprintId}")
    public ResponseEntity<SprintResponse> getSprintById(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(sprintService.getSprintById(projectId, sprintId, currentUserId));
    }

    @PatchMapping("/{sprintId}/start")
    public ResponseEntity<SprintResponse> startSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(sprintService.startSprint(projectId, sprintId, currentUserId));
    }

    @PatchMapping("/{sprintId}/complete")
    public ResponseEntity<SprintResponse> completeSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(sprintService.completeSprint(projectId, sprintId, currentUserId));
    }

    /**
     * Attach an existing ticket to this sprint. Idempotent — calling twice has
     * the same effect as calling once. Hence, PUT, not POST.
     */
    @PutMapping("/{sprintId}/tickets/{ticketId}")
    public ResponseEntity<Void> addTicketToSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Long currentUserId) {
        sprintService.addTicketToSprint(projectId, sprintId, ticketId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sprintId}/tickets/{ticketId}")
    public ResponseEntity<MessageResponse> removeTicketFromSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Long currentUserId) {
        sprintService.removeTicketFromSprint(projectId, sprintId, ticketId, currentUserId);
        return ResponseEntity.ok(new MessageResponse("Ticket removed from sprint successfully"));
    }

    @DeleteMapping("/{sprintId}")
    public ResponseEntity<MessageResponse> deleteSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal Long currentUserId) {
        sprintService.deleteSprint(projectId, sprintId, currentUserId);
        return ResponseEntity.ok(new MessageResponse("Sprint deleted successfully"));
    }
}