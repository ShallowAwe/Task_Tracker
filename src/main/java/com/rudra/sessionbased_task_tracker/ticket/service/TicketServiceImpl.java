package com.rudra.sessionbased_task_tracker.ticket.service;

import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.project.repository.ProjectRepository;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectRole;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class

TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public TicketResponse createTicket(Long projectId, CreateTicketRequest request, Long currentUserId) {

        /// validating the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project Not Found"));

        ///validating membership
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(
                        () -> new ProjectNotFoundException("Project Not Accessible")
                );

        /// Current User
        User reporter = membership.getUser();
        /// Assignee validation

        User assignee = null;
        if (request.getAssigneeId() != null) {

            projectMemberRepository
                    .findByProjectIdAndUserId(projectId, request.getAssigneeId())
                    .orElseThrow(() -> new UserNotFoundException("Assignee must be a Project-Member"));

            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new UserNotFoundException("User Not Found"));

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
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.setDeleted(false);
        Ticket saved = ticketRepository.save(ticket);


        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByProject(Long projectId, Long currentUserId) {

        /// validating the membership...
        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException("Project Not Accessible"));
        return ticketRepository.findByProjectIdAndDeletedFalse(projectId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long projectId, Long ticketId, Long currentUserId) {

        /// Validate Membership

        projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Unable To Access The Project"
                ));

        /// Validate active Ticket
        Ticket ticket = ticketRepository
                .findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket Not Found"
                ));

        /// Ensuring the Token belongs to the Project.....
        if (!ticket.getProject().getId().equals(projectId)) {
            throw new RuntimeException(
                    "Ticket Not Found for This Project"
            );
        }
        return mapToResponse(ticket);


    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long projectId,
                                       Long ticketId,
                                       UpdateTicketRequest request,
                                       Long currentUserId) {

        // Validate membership
        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not accessible"));

        // Validate active ticket
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Ticket not found in this project");
        }

        // Permission check
        boolean isReporter = ticket.getReporter().getId().equals(currentUserId);
        boolean isPrivileged = membership.getRole() == ProjectRole.OWNER
                || membership.getRole().name().equals("MAINTAINER");

        if (!isReporter && !isPrivileged) {
            throw new RuntimeException("Insufficient permission to update ticket");
        }

        // Update fields only if provided
        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }

        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        ticket.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicketStatus(Long projectId, Long ticketId, TicketStatus newStatus, Long currentUserId) {

        ///  Validate MemberShip......
         projectMemberRepository
                 .findByProjectIdAndUserId(projectId,currentUserId)
                 .orElseThrow(()-> new ProjectNotFoundException("Project Not Accessible"));

         /// validate ticket .....
        Ticket ticket = ticketRepository
                .findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Ticket Does not belong to this project"
                ));

        if (!ticket.getProject().getId().equals(projectId) ) {
            throw new RuntimeException(
                    "Ticket Does Not Belong To The Project"
            );
        }
        /// validate the transition
        validateStatusTransition(ticket.getStatus(), newStatus);

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(ticket);
    }

    @Override
    public TicketResponse assignTicket(Long projectId, Long ticketId, Long assigneeId, Long currentUserId) {

        /// validating membership of current user....,
        ProjectMember currentMembership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(
                        () -> new ProjectNotFoundException(
                                "Invalid status Transaction"
                        ));


        if (currentMembership.getRole() != ProjectRole.OWNER  &&
            currentMembership.getRole() != ProjectRole.MAINTAINER) {
          throw  new RuntimeException("Insufficient Permission to Assign Tickets");
        }

        ///   validate ticket
        Ticket ticket = ticketRepository
                .findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket Not Found"
                ));

        if (!ticket.getProject().getId().equals(projectId)) {
            throw new RuntimeException(
                    "Ticket not found in this project"
            );
        }

        ///  validating assignee membershipds
        ProjectMember assigneeMembership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new RuntimeException(
                        "Assignee must be the project member"
                ));
          ticket.setAssignee(assigneeMembership.getUser());
          ticket.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(ticket);
    }

    @Override
    @Transactional
    public void deleteTicket(Long projectId, Long ticketId, Long currentUserId) {

        ProjectMember membership = projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not accessible"));

        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Ticket not found in this project");
        }

        boolean isReporter = ticket.getReporter().getId().equals(currentUserId);
        boolean isOwner = membership.getRole() == ProjectRole.OWNER;

        if (!isReporter && !isOwner) {
            throw new RuntimeException("Insufficient permission to delete ticket");
        }

        ticket.setDeleted(true);
        ticket.setUpdatedAt(LocalDateTime.now());
    }

    /// HELPER METHOD///

    private void validateStatusTransition(TicketStatus current, TicketStatus next){

        if (current == next) return;
        switch (current) {
            case OPEN -> {
                if (next  != TicketStatus.IN_PROGRESS) {
                    throw new RuntimeException("Invalid status Transaction");
                }
            }
            case IN_PROGRESS -> {
                if (next !=  TicketStatus.RESOLVED) {
                      throw new RuntimeException("Invalid status Transaction");
                }
            }
            case RESOLVED -> {
                if (next != TicketStatus.CLOSED){
                    throw  new RuntimeException("Invalid status Transaction");
                }
            }
            case CLOSED -> {
                throw new RuntimeException("Cannot Change The Status of the Closed Ticket");
            }
        }
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
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}

