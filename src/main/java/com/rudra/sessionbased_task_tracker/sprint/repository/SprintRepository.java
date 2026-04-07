package com.rudra.sessionbased_task_tracker.sprint.repository;

import com.rudra.sessionbased_task_tracker.sprint.entity.Sprint;
import com.rudra.sessionbased_task_tracker.sprint.entity.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectId(Long projectId);

    Optional<Sprint> findByProjectIdAndStatus(Long projectId, SprintStatus status);

    boolean existsByProjectIdAndStatus(Long projectId, SprintStatus status);

    List<Sprint> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}