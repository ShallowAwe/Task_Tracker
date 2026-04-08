package com.rudra.sessionbased_task_tracker.project.repository;

import com.rudra.sessionbased_task_tracker.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndDeletedFalse(Long id);

    Optional<Project> findByKey(String key);

    boolean existsByKey(String key);

    List<Project> findByOwnerId(Long ownerId);

    List<Project> findAllByArchivedFlagFalse();
    List<Project> findByOwnerIdAndArchivedFlagFalse(Long ownerId);

}
