package com.rudra.sessionbased_task_tracker.ticket.service;

import com.rudra.sessionbased_task_tracker.activity.service.ActivityService;
import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.project.repository.ProjectRepository;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.exception.InsufficientPermissionException;
import com.rudra.sessionbased_task_tracker.projectMember.repository.ProjectMemberRepository;
import com.rudra.sessionbased_task_tracker.ticket.dto.CreateTicketRequest;
import com.rudra.sessionbased_task_tracker.ticket.dto.TicketResponse;
import com.rudra.sessionbased_task_tracker.ticket.dto.UpdateTicketRequest;
import com.rudra.sessionbased_task_tracker.ticket.entity.Ticket;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketPriority;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;
import com.rudra.sessionbased_task_tracker.ticket.exception.TicketNotFoundException;
import com.rudra.sessionbased_task_tracker.ticket.repository.TicketRepository;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import com.rudra.sessionbased_task_tracker.user.exception.UserNotFoundException;
import com.rudra.sessionbased_task_tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ActivityService activityService;

    private static final String PROJECT_NOT_ACCESSIBLE = "Project not found";
    private static final String TICKET_NOT_FOUND = "Ticket does not exist";
    private static final String TICKET_NOT_IN_PROJECT = "Ticket does not belong to this project";

    @Override
    @Transactional
    public TicketResponse createTicket(Long projectId, CreateTicketRequest request, Long currentUserId) {

        // Validate project exists
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        // Validate membership
        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_ACCESSIBLE));

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

        activityService.log(project, reporter, "created", "Ticket " + saved.getId(), saved.getTitle());

        return mapToResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByProject(Long projectId, Long currentUserId) {
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_ACCESSIBLE));

        return ticketRepository.findByProjectIdAndDeletedFalse(projectId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByProject(Long projectId, Long currentUserId, Pageable pageable) {
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_ACCESSIBLE));

        return ticketRepository.findByProjectIdAndDeletedFalse(projectId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public TicketResponse getTicketById(Long projectId, Long ticketId, Long currentUserId) {

        // validating the membership
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_ACCESSIBLE));

        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("unable to find the ticket on TicketId:" + ticketId));

        if (!ticket.getProject().getId().equals(projectId)) {
            throw new TicketNotFoundException(TICKET_NOT_IN_PROJECT);
        }

        return mapToResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long projectId, Long ticketId, UpdateTicketRequest request, Long currentUserId) {

        // 1. Validate membership in the project
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_ACCESSIBLE));

        // 2. Load ticket (excluding soft-deleted)
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND));

        // 3. Ensure ticket belongs to the project in the URL
        if (!ticket.getProject().getId().equals(projectId)) {
            throw new TicketNotFoundException(TICKET_NOT_IN_PROJECT);
        }

        // 4. Authorize BEFORE mutating: only the project owner or the ticket reporter may update
        Long projectOwnerId = ticket.getProject().getOwner().getId();
        Long reporterId     = ticket.getReporter().getId();

        boolean isOwner    = projectOwnerId.equals(currentUserId);
        boolean isReporter = reporterId.equals(currentUserId);

        if (!isOwner && !isReporter) {
            throw new InsufficientPermissionException(
                    "Only the project owner or the ticket reporter can update this ticket");
        }

        // 5. Apply partial update — only fields the client actually sent
        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            ticket.setDueDate(request.getDueDate());
        }

        // 6. Persist and return
        Ticket saved = ticketRepository.save(ticket);
        return mapToResponse(saved);
    }


    @Override
    @Transactional
    public TicketResponse updateTicketStatus(Long projectId, Long ticketId, TicketStatus newStatus, Long currentUserId) {
        // 1. Validate membership
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_ACCESSIBLE));

        // 2. Check if ticket exists
        Ticket updatedTicket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND));

        // 3. Ensure ticket belongs to project
        if (!updatedTicket.getProject().getId().equals(projectId)) {
            throw new TicketNotFoundException(TICKET_NOT_IN_PROJECT);
        }
        // 4. update the status of the ticket
        if (newStatus != null) {
            updatedTicket.setStatus(newStatus);
        }
        // 5. Authorize: only the reporter OR the project owner may delete
        Long reporterId = updatedTicket.getReporter().getId();
        Long ownerId    = updatedTicket.getProject().getOwner().getId();

        boolean isReporter = reporterId.equals(currentUserId);
        boolean isOwner    = ownerId.equals(currentUserId);

        if (!isReporter && !isOwner) {
            throw new InsufficientPermissionException(
                    "Ticket can only be deleted by the project owner or the ticket reporter");
        }

        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        activityService.log(updatedTicket.getProject(), currentUser, "changed status",
                "Ticket " + ticketId, newStatus.name());

        Ticket saved = ticketRepository.save(updatedTicket);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse assignTicket(Long projectId, Long ticketId, Long assigneeId, Long currentUserId) {
        // 1. Validate membership
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_ACCESSIBLE));

        // 2. Check if ticket exists
        Ticket updatedTicket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND));

        // 3. Ensure ticket belongs to project
        if (!updatedTicket.getProject().getId().equals(projectId)) {
            throw new TicketNotFoundException(TICKET_NOT_IN_PROJECT);
        }

        ProjectMember assigneeMember = projectMemberRepository
                .findByProjectIdAndUserId(projectId, assigneeId)
                .orElseThrow(() -> new UserNotFoundException("Assignee must be a project member"));

        updatedTicket.setAssignee(assigneeMember.getUser());

        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        activityService.log(updatedTicket.getProject(), currentUser, "assigned",
                "Ticket " + ticketId, assigneeMember.getUser().getName());

        Ticket saved = ticketRepository.save(updatedTicket);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTicket(Long projectId, Long ticketId, Long currentUserId) {

        // 1. Validate membership
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_ACCESSIBLE));

        // 2. Load ticket (excluding already-deleted)
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND));

        // 3. Ensure ticket belongs to the project in the URL
        if (!ticket.getProject().getId().equals(projectId)) {
            throw new TicketNotFoundException(TICKET_NOT_IN_PROJECT);
        }

        // 4. Authorize: only the reporter OR the project owner may delete
        Long reporterId = ticket.getReporter().getId();
        Long ownerId    = ticket.getProject().getOwner().getId();

        boolean isReporter = reporterId.equals(currentUserId);
        boolean isOwner    = ownerId.equals(currentUserId);

        if (!isReporter && !isOwner) {
            throw new InsufficientPermissionException(
                    "Ticket can only be deleted by the project owner or the ticket reporter");
        }

        // 5. Soft delete
        ticket.setDeleted(true);
        ticketRepository.save(ticket);

        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        activityService.log(ticket.getProject(), currentUser, "deleted", "Ticket " + ticketId, ticket.getTitle());
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