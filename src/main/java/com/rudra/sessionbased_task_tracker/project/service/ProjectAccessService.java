package com.rudra.sessionbased_task_tracker.project.service;

import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import com.rudra.sessionbased_task_tracker.projectMember.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final ProjectMemberRepository projectMemberRepository;

    @Transactional(readOnly = true)
    public List<ProjectMember> getActiveMemberships(Long userId) {
        return projectMemberRepository.findByUserIdAndProjectDeletedFalse(userId).stream()
                .sorted(Comparator
                        .comparing(ProjectMember::getLastAccessedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ProjectMember::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
