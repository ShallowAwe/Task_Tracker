package com.rudra.sessionbased_task_tracker.sprint.controller;

import com.rudra.sessionbased_task_tracker.sprint.dto.CreateSprintRequest;
import com.rudra.sessionbased_task_tracker.sprint.dto.SprintResponse;
import com.rudra.sessionbased_task_tracker.sprint.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(
            @PathVariable Long projectId,
            @RequestBody CreateSprintRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sprintService.createSprint(projectId, request, currentUserId));
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

    @PostMapping("/{sprintId}/tickets/{ticketId}")
    public ResponseEntity<Void> addTicketToSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Long currentUserId) {
        sprintService.addTicketToSprint(projectId, sprintId, ticketId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{sprintId}/tickets/{ticketId}")
    public ResponseEntity<Void> removeTicketFromSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Long currentUserId) {
        sprintService.removeTicketFromSprint(projectId, sprintId, ticketId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{sprintId}")
    public ResponseEntity<Void> deleteSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal Long currentUserId) {
        sprintService.deleteSprint(projectId, sprintId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}