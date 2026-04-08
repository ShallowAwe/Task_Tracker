package com.rudra.sessionbased_task_tracker.comment.service;


import com.rudra.sessionbased_task_tracker.comment.dto.CommentResponse;
import com.rudra.sessionbased_task_tracker.comment.dto.CreateCommentRequest;
import com.rudra.sessionbased_task_tracker.comment.dto.UpdateCommentRequest;
import com.rudra.sessionbased_task_tracker.comment.entity.Comment;
import com.rudra.sessionbased_task_tracker.comment.exception.CommentNotFoundException;
import com.rudra.sessionbased_task_tracker.comment.repository.CommentRepository;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectRole;
import com.rudra.sessionbased_task_tracker.projectMember.exception.InsufficientPermissionException;
import com.rudra.sessionbased_task_tracker.projectMember.repository.ProjectMemberRepository;
import com.rudra.sessionbased_task_tracker.ticket.entity.Ticket;
import TicketNotFoundException;
import com.rudra.sessionbased_task_tracker.ticket.repository.TicketRepository;
import com.rudra.sessionbased_task_tracker.user.entity.User;
import com.rudra.sessionbased_task_tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse addComment(Long projectId,
                                      Long ticketId,
                                      CreateCommentRequest request,
                                      Long currentUserId) {

        // Validate membership
        ProjectMember membership = validateMembership(projectId, currentUserId);

        // Validate active ticket
        Ticket ticket = validateActiveTicket(projectId, ticketId);

        User author = membership.getUser();

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setDeleted(false);

        Comment saved = commentRepository.save(comment);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long projectId,
                                             Long ticketId,
                                             Long currentUserId) {

        validateMembership(projectId, currentUserId);
        validateActiveTicket(projectId, ticketId);

        return commentRepository.findByTicketIdAndDeletedFalse(ticketId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long projectId,
                                         Long ticketId,
                                         Long commentId,
                                         UpdateCommentRequest request,
                                         Long currentUserId) {

        validateMembership(projectId, currentUserId);
        validateActiveTicket(projectId, ticketId);

        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getTicket().getId().equals(ticketId)) {
            throw new CommentNotFoundException("Comment not found in this ticket");
        }

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new InsufficientPermissionException("Only author can edit comment");
        }


        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        comment.setContent(request.getContent().trim());

        comment.setUpdatedAt(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);
        return mapToResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long projectId,
                              Long ticketId,
                              Long commentId,
                              Long currentUserId) {

        ProjectMember membership = validateMembership(projectId, currentUserId);
        validateActiveTicket(projectId, ticketId);

        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getTicket().getId().equals(ticketId)) {
            throw new CommentNotFoundException("Comment not found in this ticket");
        }

        boolean isAuthor = comment.getAuthor().getId().equals(currentUserId);
        boolean isOwner = membership.getRole() == ProjectRole.OWNER;

        if (!isAuthor && !isOwner) {
            throw new InsufficientPermissionException("Insufficient permission to delete comment");
        }

        comment.setDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());
        comment.setDeletedBy(membership.getUser());
        commentRepository.save(comment);
    }


    /// HELPER METHODS
    private ProjectMember validateMembership(Long projectId, Long userId) {
        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not accessible"));
    }

    private Ticket validateActiveTicket(Long projectId, Long ticketId) {
        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));

        if (!ticket.getProject().getId().equals(projectId)) {
            throw new TicketNotFoundException("Ticket not found in this project");
        }

        return ticket;
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

}
