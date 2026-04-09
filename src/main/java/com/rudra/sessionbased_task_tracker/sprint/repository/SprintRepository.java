package com.rudra.sessionbased_task_tracker.sprint.repository;

import com.rudra.sessionbased_task_tracker.sprint.entity.Sprint;
import com.rudra.sessionbased_task_tracker.sprint.entity.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    Optional<Sprint> findByIdAndDeletedFalse(Long id);

    List<Sprint> findByProjectIdAndDeletedFalse(Long projectId);

    Optional<Sprint> findByProjectIdAndStatusAndDeletedFalse(Long projectId, SprintStatus status);

    boolean existsByProjectIdAndStatusAndDeletedFalse(Long projectId, SprintStatus status);

    List<Sprint> findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(Long projectId);
}