package com.rudra.sessionbased_task_tracker.project.controller;

import com.rudra.sessionbased_task_tracker.project.dto.CreateProjectRequest;
import com.rudra.sessionbased_task_tracker.project.dto.ProjectResponse;
import com.rudra.sessionbased_task_tracker.project.dto.UpdateProjectRequest;
import com.rudra.sessionbased_task_tracker.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ProjectResponse> getProjectByKey(
            @PathVariable String key,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(projectService.getProjectByKey(key, userId));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestAttribute("userId") Long userId,
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
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(projectService.updateProject(id, request, userId));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Map<String, String>> archiveProject(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        projectService.archiveProject(id, userId);
        return ResponseEntity.ok(Map.of("message", "Project archived successfully"));
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<Map<String, String>> unarchiveProject(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        projectService.unarchiveProject(id, userId);
        return ResponseEntity.ok(Map.of("message", "Project unarchived successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProject(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        projectService.deleteProject(id, userId);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }
}