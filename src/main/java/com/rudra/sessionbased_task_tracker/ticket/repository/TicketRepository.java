package com.rudra.sessionbased_task_tracker.ticket.repository;

import com.rudra.sessionbased_task_tracker.ticket.entity.Ticket;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketPriority;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    long countByProjectIdAndDueDateBetweenAndDeletedFalse(
            Long projectId, LocalDateTime start, LocalDateTime end);

    // ---- Sprint-related queries ----

    /**
     * Counts non-deleted tickets currently in the sprint.
     * SprintService uses this for the totalTickets metric.
     */
    int countBySprintIdAndDeletedFalse(Long sprintId);

    /**
     * Counts non-deleted tickets in the sprint whose status is in the "done" set
     * (typically RESOLVED, CLOSED).
     */
    @Query("""
            SELECT COUNT(t) FROM Ticket t
            WHERE t.sprint.id = :sprintId
              AND t.deleted = false
              AND t.status IN :doneStatuses
            """)
    int countCompletedTicketsInSprint(
            @Param("sprintId") Long sprintId,
            @Param("doneStatuses") List<TicketStatus> doneStatuses);

    /**
     * On sprint completion: detach all incomplete (not in doneStatuses), non-deleted
     * tickets from the sprint, sending them back to the backlog.
     * Completed tickets stay attached to preserve sprint history.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Ticket t
            SET t.sprint = null
            WHERE t.sprint.id = :sprintId
              AND t.deleted = false
              AND t.status NOT IN :doneStatuses
            """)
    int moveIncompleteTicketsToBacklog(
            @Param("sprintId") Long sprintId,
            @Param("doneStatuses") List<TicketStatus> doneStatuses);

    /**
     * On sprint deletion: detach every non-deleted ticket from the sprint
     * before the sprint row is removed.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Ticket t
            SET t.sprint = null
            WHERE t.sprint.id = :sprintId
              AND t.deleted = false
            """)
    int detachAllTicketsFromSprint(@Param("sprintId") Long sprintId);
}
