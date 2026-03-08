package com.rudra.sessionbased_task_tracker.ticket.repository;

import com.rudra.sessionbased_task_tracker.ticket.entity.Ticket;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketPriority;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByIdAndDeletedFalse(Long id);

    List<Ticket> findByProjectIdAndDeletedFalse(Long projectId);

    List<Ticket> findByProjectIdAndStatusAndDeletedFalse(Long projectId, TicketStatus status);

    boolean existsByIdAndProjectIdAndDeletedFalse(Long ticketId, Long projectId);

    long countByProjectIdAndStatusAndDeletedFalse(Long projectId, TicketStatus status);

    long countByProjectIdAndPriorityInAndDeletedFalse(Long projectId, List<TicketPriority> priorities);

    long countByProjectIdAndDeletedFalse(Long projectId);

    List<Ticket> findByProjectIdAndAssigneeIdAndDeletedFalse(Long projectId, Long assigneeId);

    List<Ticket> findByProjectIdAndDueDateNotNullAndDeletedFalseOrderByDueDateAsc(Long projectId);

    long countByProjectIdAndDueDateBetweenAndDeletedFalse(Long projectId, LocalDateTime start, LocalDateTime end);
}