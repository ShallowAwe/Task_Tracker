package com.rudra.sessionbased_task_tracker.projectMember.service;

import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.project.repository.ProjectRepository;
import com.rudra.sessionbased_task_tracker.projectMember.dto.AddMemberRequest;
import com.rudra.sessionbased_task_tracker.projectMember.dto.ProjectMemberResponse;
import com.rudra.sessionbased_task_tracker.projectMember.dto.UpdateMemberRoleRequest;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectRole;
import com.rudra.sessionbased_task_tracker.projectMember.exception.InsufficientPermissionException;
import com.rudra.sessionbased_task_tracker.projectMember.exception.MemberAlreadyExistsException;
import com.rudra.sessionbased_task_tracker.projectMember.exception.MemberNotFoundException;
import com.rudra.sessionbased_task_tracker.projectMember.repository.ProjectMemberRepository;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import com.rudra.sessionbased_task_tracker.user.exception.UserNotFoundException;
import com.rudra.sessionbased_task_tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // ---------------- ADD MEMBER ----------------
    @Transactional
    public ProjectMemberResponse addMember(Long projectId, AddMemberRequest request, Long currentUserId) {

        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        ProjectMember currentMember = validateMemberPermission(
                projectId, currentUserId,
                "Only OWNER or MAINTAINER can add members"
        );

        if (currentMember.getRole() != ProjectRole.OWNER
                && request.getRole().getLevel() >= currentMember.getRole().getLevel()) {
            throw new InsufficientPermissionException(
                    "Cannot assign a role equal to or higher than your own");
        }

        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new MemberAlreadyExistsException("User is already a member of this project");
        }

        ProjectMember newMember = new ProjectMember();
        newMember.setProject(project);
        newMember.setUser(userToAdd);
        newMember.setRole(request.getRole());

        ProjectMember savedMember = projectMemberRepository.save(newMember);

        return mapToResponse(savedMember);
    }

    // ---------------- GET MEMBERS ----------------
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, Long currentUserId) {

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
            throw new ProjectNotFoundException("Project not found with id: " + projectId);
        }

        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------- UPDATE ROLE ----------------
    @Transactional
    public ProjectMemberResponse updateMemberRole(
            Long projectId,
            Long memberId,
            UpdateMemberRoleRequest request,
            Long currentUserId
    ) {

        ProjectMember currentMember = validateMemberPermission(
                projectId, currentUserId,
                "Only OWNER or MAINTAINER can update member roles"
        );

        ProjectMember memberToUpdate = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        if (!memberToUpdate.getProject().getId().equals(projectId)) {
            throw new MemberNotFoundException("Member not found in this project");
        }

        if (memberToUpdate.getRole() == ProjectRole.OWNER) {
            throw new InsufficientPermissionException("Cannot change OWNER role");
        }

        if (currentMember.getRole() != ProjectRole.OWNER
                && request.getRole().getLevel() >= currentMember.getRole().getLevel()) {
            throw new InsufficientPermissionException(
                    "Cannot assign a role equal to or higher than your own");
        }

        if (currentMember.getRole() != ProjectRole.OWNER
                && memberToUpdate.getRole().getLevel() >= currentMember.getRole().getLevel()) {
            throw new InsufficientPermissionException(
                    "Cannot modify a member with a role equal to or higher than your own");
        }

        memberToUpdate.setRole(request.getRole());

        ProjectMember updatedMember = projectMemberRepository.save(memberToUpdate);

        return mapToResponse(updatedMember);
    }

    // ---------------- REMOVE MEMBER ----------------
    @Transactional
    public void removeMember(Long projectId, Long memberId, Long currentUserId) {

        ProjectMember currentMember = validateMemberPermission(
                projectId, currentUserId,
                "Only OWNER or MAINTAINER can remove members"
        );

        ProjectMember memberToRemove = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        if (!memberToRemove.getProject().getId().equals(projectId)) {
            throw new MemberNotFoundException("Member not found in this project");
        }

        if (memberToRemove.getRole() == ProjectRole.OWNER) {
            throw new InsufficientPermissionException("Cannot remove OWNER from project");
        }

        if (currentMember.getRole() != ProjectRole.OWNER
                && memberToRemove.getRole().getLevel() >= currentMember.getRole().getLevel()) {
            throw new InsufficientPermissionException(
                    "Cannot remove a member with a role equal to or higher than your own");
        }

        projectMemberRepository.delete(memberToRemove);
    }

    // ---------------- UPDATE LAST ACCESSED ----------------
    @Transactional
    public void updateLastAccessed(Long projectId, Long userId) {

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not accessible"));

        member.setLastAccessedAt(LocalDateTime.now());

        projectMemberRepository.save(member);
    }

    // ---------------- VALIDATION ----------------
    private ProjectMember validateMemberPermission(Long projectId, Long userId, String errorMessage) {

        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (membership.getRole() != ProjectRole.OWNER && membership.getRole() != ProjectRole.MAINTAINER) {
            throw new InsufficientPermissionException(errorMessage);
        }

        return membership;
    }

    // ---------------- MAPPER ----------------
    private ProjectMemberResponse mapToResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .projectKey(member.getProject().getKey())
                .projectName(member.getProject().getName())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .userAvatar(member.getUser().getAvatar())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
