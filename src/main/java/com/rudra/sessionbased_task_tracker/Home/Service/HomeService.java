package com.rudra.sessionbased_task_tracker.Home.Service;

import com.rudra.sessionbased_task_tracker.Home.dto.HomeResponse;

import com.rudra.sessionbased_task_tracker.overview.services.OverviewService;
import com.rudra.sessionbased_task_tracker.project.dto.ProjectLite;
import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.service.ProjectAccessService;
import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ProjectAccessService projectAccessService;
    private final OverviewService overviewService;

    @Transactional(readOnly = true)
    public HomeResponse getHome(Long userId) {
        List<ProjectMember> memberships = projectAccessService.getActiveMemberships(userId);

        if (memberships.isEmpty()) {
            return HomeResponse.builder()
                    .hasProjects(false)
                    .build();
        }

        Project defaultProject = resolveDefault(memberships);

        if (defaultProject == null) {
            return HomeResponse.builder()
                    .hasProjects(true)
                    .defaultProjectKey(null)
                    .projects(mapProjects(memberships))
                    .build();
        }

        return HomeResponse.builder()
                .hasProjects(true)
                .defaultProjectKey(defaultProject.getKey())
                .projects(mapProjects(memberships))
                .summary(overviewService.getProjectSummary(defaultProject.getKey(), userId))
                .myWork(overviewService.getMyWork(defaultProject.getKey(), userId))
                .activities(overviewService.getActivities(defaultProject.getKey(), userId))
                .deadlines(overviewService.getDeadlines(defaultProject.getKey(), userId))
                .build();
    }

    // ---------------- DEFAULT RESOLVER ----------------
    private Project resolveDefault(List<ProjectMember> memberships) {

        return memberships.stream()
                .filter(membership -> membership.getLastAccessedAt() != null)
                .findFirst()
                .map(ProjectMember::getProject)
                .orElse(null);
    }

    // ---------------- PROJECT MAPPER ----------------
    private List<ProjectLite> mapProjects(List<ProjectMember> memberships) {

        return memberships.stream()
                .map(m -> ProjectLite.builder()
                        .id(m.getProject().getId())
                        .key(m.getProject().getKey())
                        .name(m.getProject().getName())
                        .build())
                .toList();
    }
}
