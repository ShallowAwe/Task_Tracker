package com.rudra.sessionbased_task_tracker.projectMember.controller;

import com.rudra.sessionbased_task_tracker.projectMember.dto.AddMemberRequest;
import com.rudra.sessionbased_task_tracker.projectMember.dto.ProjectMemberResponse;
import com.rudra.sessionbased_task_tracker.projectMember.dto.UpdateMemberRoleRequest;
import com.rudra.sessionbased_task_tracker.projectMember.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddMemberRequest request,
            @RequestAttribute("userId") Long currentUserId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectMemberService.addMember(projectId, request, currentUserId));
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            @PathVariable Long projectId,
            @RequestAttribute("userId") Long currentUserId) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId, currentUserId));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @RequestAttribute("userId") Long currentUserId) {
        return ResponseEntity.ok(
                projectMemberService.updateMemberRole(projectId, memberId, request, currentUserId));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Map<String, String>> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestAttribute("userId") Long currentUserId) {
        projectMemberService.removeMember(projectId, memberId, currentUserId);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }
}