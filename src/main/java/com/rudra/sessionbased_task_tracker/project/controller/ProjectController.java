package com.rudra.sessionbased_task_tracker.project.controller;

import com.rudra.sessionbased_task_tracker.common.dto.MessageResponse;
import com.rudra.sessionbased_task_tracker.project.dto.CreateProjectRequest;
import com.rudra.sessionbased_task_tracker.project.dto.ProjectResponse;
import com.rudra.sessionbased_task_tracker.project.dto.UpdateProjectRequest;
import com.rudra.sessionbased_task_tracker.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ProjectResponse> getProjectByKey(
            @PathVariable String key,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(projectService.getProjectByKey(key, userId));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived) {

        if (includeArchived) {
            return ResponseEntity.ok(projectService.getAllProjectsByUser(userId));
        }
        return ResponseEntity.ok(projectService.getActiveProjectsByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(projectService.updateProject(id, request, userId));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<MessageResponse> archiveProject(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        projectService.archiveProject(id, userId);
        return ResponseEntity.ok(new MessageResponse("Project archived successfully"));
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<MessageResponse> unarchiveProject(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        projectService.unarchiveProject(id, userId);
        return ResponseEntity.ok(new MessageResponse("Project unarchived successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        projectService.deleteProject(id, userId);
        return ResponseEntity.ok(new MessageResponse("Project deleted successfully"));
    }
}