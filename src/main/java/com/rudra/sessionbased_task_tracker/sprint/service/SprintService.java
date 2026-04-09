package com.rudra.sessionbased_task_tracker.sprint.service;

import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.project.repository.ProjectRepository;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectRole;
import com.rudra.sessionbased_task_tracker.projectMember.exception.InsufficientPermissionException;
import com.rudra.sessionbased_task_tracker.projectMember.repository.ProjectMemberRepository;
import com.rudra.sessionbased_task_tracker.sprint.dto.CreateSprintRequest;
import com.rudra.sessionbased_task_tracker.sprint.dto.SprintResponse;
import com.rudra.sessionbased_task_tracker.sprint.entity.Sprint;
import com.rudra.sessionbased_task_tracker.sprint.entity.SprintStatus;
import com.rudra.sessionbased_task_tracker.sprint.exception.SprintNotFoundException;
import com.rudra.sessionbased_task_tracker.sprint.repository.SprintRepository;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;
import com.rudra.sessionbased_task_tracker.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TicketRepository ticketRepository;

    private static final List<TicketStatus> DONE_STATUSES = List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED);

    @Transactional
    public SprintResponse createSprint(Long projectId, CreateSprintRequest request, Long currentUserId) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        validateSprintPermission(projectId, currentUserId);

        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Sprint sprint = Sprint.builder()
                .project(project)
                .name(request.getName())
                .goal(request.getGoal())
                .status(SprintStatus.PLANNING)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    public List<SprintResponse> getProjectSprints(Long projectId, Long currentUserId) {
        validateMembership(projectId, currentUserId);

        return sprintRepository.findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SprintResponse getSprintById(Long projectId, Long sprintId, Long currentUserId) {
        validateMembership(projectId, currentUserId);
        Sprint sprint = getSprintAndValidateProject(projectId, sprintId);
        return mapToResponse(sprint);
    }

    @Transactional
    public SprintResponse startSprint(Long projectId, Long sprintId, Long currentUserId) {
        validateSprintPermission(projectId, currentUserId);

        if (sprintRepository.existsByProjectIdAndStatusAndDeletedFalse(projectId, SprintStatus.ACTIVE)) {
            throw new IllegalStateException("Project already has an active sprint. Complete it first.");
        }

        Sprint sprint = getSprintAndValidateProject(projectId, sprintId);

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new IllegalStateException("Only PLANNING sprints can be started");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Transactional
    public SprintResponse completeSprint(Long projectId, Long sprintId, Long currentUserId) {
        validateSprintPermission(projectId, currentUserId);

        Sprint sprint = getSprintAndValidateProject(projectId, sprintId);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE sprints can be completed");
        }

        sprint.setStatus(SprintStatus.COMPLETED);

        // Bulk move incomplete non-deleted tickets back to backlog
        ticketRepository.moveIncompleteTicketsToBacklog(sprintId, DONE_STATUSES);

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Transactional
    public void addTicketToSprint(Long projectId, Long sprintId, Long ticketId, Long currentUserId) {
        validateSprintPermission(projectId, currentUserId);

        Sprint sprint = getSprintAndValidateProject(projectId, sprintId);

        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new IllegalStateException("Cannot add tickets to a completed sprint");
        }

        // Use soft-delete-aware lookup
        var ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (!ticket.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Ticket does not belong to this project");
        }

        ticket.setSprint(sprint);
        ticketRepository.save(ticket);
    }

    @Transactional
    public void removeTicketFromSprint(Long projectId, Long sprintId, Long ticketId, Long currentUserId) {
        validateSprintPermission(projectId, currentUserId);

        var ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (ticket.getSprint() == null || !ticket.getSprint().getId().equals(sprintId)) {
            throw new RuntimeException("Ticket is not in this sprint");
        }

        ticket.setSprint(null);
        ticketRepository.save(ticket);
    }

    @Transactional
    public void deleteSprint(Long projectId, Long sprintId, Long currentUserId) {
        ProjectMember member = validateSprintPermission(projectId, currentUserId);

        Sprint sprint = getSprintAndValidateProject(projectId, sprintId);

        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete an active sprint. Complete it first.");
        }

        // Bulk detach all non-deleted tickets before deleting sprint
        ticketRepository.detachAllTicketsFromSprint(sprintId);
        sprint.setDeletedAt(LocalDateTime.now());
        sprint.setDeletedBy(member.getUser());
        sprint.setDeleted(true);
        sprintRepository.save(sprint);
    }

    // --- Helper methods ---

    private Sprint getSprintAndValidateProject(Long projectId, Long sprintId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(sprintId)
                .orElseThrow(() -> new SprintNotFoundException("Sprint not found with id: " + sprintId));

        if (!sprint.getProject().getId().equals(projectId)) {
            throw new SprintNotFoundException("Sprint does not belong to this project");
        }
        return sprint;
    }

    private ProjectMember validateSprintPermission(Long projectId, Long userId) {
        var membership = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (membership.getRole() != ProjectRole.OWNER && membership.getRole() != ProjectRole.MAINTAINER) {
            throw new InsufficientPermissionException("Only OWNER or MAINTAINER can manage sprints");
        }
        return membership;
    }

    private void validateMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ProjectNotFoundException("Project not found with id: " + projectId);
        }
    }

    private SprintResponse mapToResponse(Sprint sprint) {
        // Uses soft-delete-aware counts
        int totalTickets = ticketRepository.countBySprintIdAndDeletedFalse(sprint.getId());
        int completedTickets = ticketRepository.countCompletedTicketsInSprint(sprint.getId(), DONE_STATUSES);

        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProject().getId())
                .projectName(sprint.getProject().getName())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .totalTickets(totalTickets)
                .completedTickets(completedTickets)
                .createdAt(sprint.getCreatedAt())
                .updatedAt(sprint.getUpdatedAt())
                .build();
    }
}