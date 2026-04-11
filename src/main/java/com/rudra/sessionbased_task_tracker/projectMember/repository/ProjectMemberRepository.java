package com.rudra.sessionbased_task_tracker.projectMember.repository;

import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMember> findByProjectId(Long projectId);

    List<ProjectMember> findByUserId(Long userId);

    @Query("""
            SELECT pm FROM ProjectMember pm
            JOIN FETCH pm.project p
            JOIN FETCH p.owner
            WHERE pm.user.id = :userId
              AND p.deleted = false
            """)
    List<ProjectMember> findByUserIdAndProjectDeletedFalse(@Param("userId") Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);


    void deleteByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMember> findByProjectIdAndRole(Long projectId, ProjectRole role);

    long countByProjectId(Long projectId);
}