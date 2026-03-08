package com.rudra.sessionbased_task_tracker.ticket.service;

import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.project.repository.ProjectRepository;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.repository.ProjectMemberRepository;
import com.rudra.sessionbased_task_tracker.ticket.dto.CreateTicketRequest;
import com.rudra.sessionbased_task_tracker.ticket.dto.TicketResponse;
import com.rudra.sessionbased_task_tracker.ticket.dto.UpdateTicketRequest;
import com.rudra.sessionbased_task_tracker.ticket.entity.Ticket;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketPriority;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;
import com.rudra.sessionbased_task_tracker.ticket.repository.TicketRepository;
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
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TicketResponse createTicket(Long projectId, CreateTicketRequest request, Long currentUserId) {

        // Validate project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        // Validate membership
        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not accessible"));

        // Reporter = current user
        User reporter = membership.getUser();

        // Assignee validation (optional)
        User assignee = null;
        if (request.getAssigneeId() != null) {
            projectMemberRepository
                    .findByProjectIdAndUserId(projectId, request.getAssigneeId())
                    .orElseThrow(() -> new UserNotFoundException("Assignee must be a project member"));

            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        }

        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setProject(project);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setReporter(reporter);
        ticket.setAssignee(assignee);
        ticket.setDueDate(request.getDueDate());
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.setDeleted(false);

        Ticket saved = ticketRepository.save(ticket);

        return mapToResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByProject(Long projectId, Long currentUserId) {


        // Validate membership
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not accessible"));

        return ticketRepository.findByProjectIdAndDeletedFalse(projectId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TicketResponse getTicketById(Long projectId, Long ticketId, Long currentUserId) {
        return null;
    }

    @Override
    public TicketResponse updateTicket(Long projectId, Long ticketId, UpdateTicketRequest request, Long currentUserId) {
        return null;
    }

    @Override
    public TicketResponse updateTicketStatus(Long projectId, Long ticketId, TicketStatus newStatus, Long currentUserId) {
        return null;
    }

    @Override
    public TicketResponse assignTicket(Long projectId, Long ticketId, Long assigneeId, Long currentUserId) {
        return null;
    }

    @Override
    public void deleteTicket(Long projectId, Long ticketId, Long currentUserId) {

    }


    private TicketResponse mapToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .projectId(ticket.getProject().getId())
                .projectKey(ticket.getProject().getKey())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .reporterId(ticket.getReporter().getId())
                .reporterName(ticket.getReporter().getName())
                .assigneeId(ticket.getAssignee() != null ? ticket.getAssignee().getId() : null)
                .assigneeName(ticket.getAssignee() != null ? ticket.getAssignee().getName() : null)
                .dueDate(ticket.getDueDate())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}