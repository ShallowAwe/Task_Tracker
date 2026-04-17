package com.rudra.sessionbased_task_tracker.overview.services;
import com.rudra.sessionbased_task_tracker.activity.dto.ActivityResponse;
import com.rudra.sessionbased_task_tracker.activity.entity.Activity;
import com.rudra.sessionbased_task_tracker.activity.repository.ActivityRepository;
import com.rudra.sessionbased_task_tracker.overview.dto.DeadlineResponse;
import com.rudra.sessionbased_task_tracker.overview.dto.MyWorkResponse;
import com.rudra.sessionbased_task_tracker.overview.dto.ProjectSummaryResponse;
import com.rudra.sessionbased_task_tracker.project.entity.Project;
import com.rudra.sessionbased_task_tracker.project.exception.ProjectNotFoundException;
import com.rudra.sessionbased_task_tracker.project.repository.ProjectRepository;
import com.rudra.sessionbased_task_tracker.projectMember.repository.ProjectMemberRepository;
import com.rudra.sessionbased_task_tracker.sprint.entity.Sprint;
import com.rudra.sessionbased_task_tracker.sprint.entity.SprintStatus;
import com.rudra.sessionbased_task_tracker.sprint.repository.SprintRepository;
import com.rudra.sessionbased_task_tracker.ticket.entity.Ticket;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketPriority;
import com.rudra.sessionbased_task_tracker.ticket.entity.TicketStatus;
import com.rudra.sessionbased_task_tracker.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OverviewService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TicketRepository ticketRepository;
    private final ActivityRepository activityRepository;
    private final SprintRepository sprintRepository;

    @Transactional(readOnly = true)
    public ProjectSummaryResponse getProjectSummary(String projectKey, Long currentUserId) {
        Project project = findProjectAndValidateMembership(projectKey, currentUserId);
        Long projectId = project.getId();

        // --- Stats ---
        long openCount = ticketRepository.countByProjectIdAndStatusAndDeletedFalse(projectId, TicketStatus.OPEN);
        long inProgressCount = ticketRepository.countByProjectIdAndStatusAndDeletedFalse(projectId, TicketStatus.IN_PROGRESS);
        long resolvedCount = ticketRepository.countByProjectIdAndStatusAndDeletedFalse(projectId, TicketStatus.RESOLVED);
        long closedCount = ticketRepository.countByProjectIdAndStatusAndDeletedFalse(projectId, TicketStatus.CLOSED);
        long totalCount = ticketRepository.countByProjectIdAndDeletedFalse(projectId);

        // Due today
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        long dueTodayCount = ticketRepository.countByProjectIdAndDueDateBetweenAndDeletedFalse(projectId, startOfDay, endOfDay);

        // High priority = HIGH + CRITICAL
        long highPriorityCount = ticketRepository.countByProjectIdAndPriorityInAndDeletedFalse(
                projectId, List.of(TicketPriority.HIGH, TicketPriority.CRITICAL));

        List<ProjectSummaryResponse.StatItem> stats = new ArrayList<>();
        stats.add(ProjectSummaryResponse.StatItem.builder()
                .label("Open Issues").value(openCount).trend("0").trendUp(false).type("OPEN").build());
        stats.add(ProjectSummaryResponse.StatItem.builder()
                .label("Due Today").value(dueTodayCount).trend("0").trendUp(false).type("DUE_TODAY").build());
        stats.add(ProjectSummaryResponse.StatItem.builder()
                .label("In Progress").value(inProgressCount).trend("0").trendUp(false).type("IN_PROGRESS").build());
        stats.add(ProjectSummaryResponse.StatItem.builder()
                .label("High Priority").value(highPriorityCount).trend("0").trendUp(false).type("HIGH_PRIORITY").build());

        // --- Workflow ---
        List<ProjectSummaryResponse.WorkflowItem> workflow = List.of(
                ProjectSummaryResponse.WorkflowItem.builder().status("Open").count(openCount).build(),
                ProjectSummaryResponse.WorkflowItem.builder().status("In Progress").count(inProgressCount).build(),
                ProjectSummaryResponse.WorkflowItem.builder().status("Resolved").count(resolvedCount).build(),
                ProjectSummaryResponse.WorkflowItem.builder().status("Closed").count(closedCount).build()
        );

        // --- Progress ---
        int progress = 0;
        if (totalCount > 0) {
            progress = (int) ((closedCount + resolvedCount) * 100 / totalCount);
        }

        // --- Project status derived from progress ---
        String projectStatus;
        if (progress >= 75) {
            projectStatus = "On Track";
        } else if (progress >= 40) {
            projectStatus = "At Risk";
        } else {
            projectStatus = "Behind";
        }

        // --- Active Sprint ---
        Optional<Sprint> activeSprintOpt = sprintRepository.findByProjectIdAndStatusAndDeletedFalse(
                projectId, SprintStatus.ACTIVE);

        ProjectSummaryResponse.SprintInfo sprintInfo = null;
        ProjectSummaryResponse.SprintMetric sprintMetric = null;

        if (activeSprintOpt.isPresent()) {
            Sprint activeSprint = activeSprintOpt.get();
            int daysLeft = 0;
            int totalDays = 0;
            if (activeSprint.getStartDate() != null && activeSprint.getEndDate() != null) {
                totalDays = (int) ChronoUnit.DAYS.between(activeSprint.getStartDate(), activeSprint.getEndDate());
                daysLeft = Math.max(0, (int) ChronoUnit.DAYS.between(LocalDate.now(), activeSprint.getEndDate()));
            }

            sprintInfo = ProjectSummaryResponse.SprintInfo.builder()
                    .name(activeSprint.getName())
                    .daysLeft(daysLeft)
                    .totalDays(totalDays)
                    .build();

            int sprintTotal = ticketRepository.countBySprintIdAndDeletedFalse(activeSprint.getId());
            int sprintCompleted = ticketRepository.countCompletedTicketsInSprint(
                    activeSprint.getId(), List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));

            sprintMetric = ProjectSummaryResponse.SprintMetric.builder()
                    .name(activeSprint.getName())
                    .goal(activeSprint.getGoal())
                    .status(activeSprint.getStatus().name())
                    .daysLeft(daysLeft)
                    .totalDays(totalDays)
                    .totalTickets(sprintTotal)
                    .completedTickets(sprintCompleted)
                    .build();
        }

        // --- Metric Cards ---
        long overdueCount = ticketRepository.countOverdueTickets(
                projectId, LocalDateTime.now(), List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
        long unassignedCount = ticketRepository.countByProjectIdAndAssigneeIsNullAndDeletedFalse(projectId);
        long teamMemberCount = projectMemberRepository.countByProjectId(projectId);

        ProjectSummaryResponse.MetricCards metrics = ProjectSummaryResponse.MetricCards.builder()
                .totalTickets(totalCount)
                .overdueTickets(overdueCount)
                .unassignedTickets(unassignedCount)
                .teamMembers(teamMemberCount)
                .completionRate(progress)
                .activeSprint(sprintMetric)
                .build();

        ProjectSummaryResponse.ProjectInfo projectInfo = ProjectSummaryResponse.ProjectInfo.builder()
                .name(project.getName())
                .status(projectStatus)
                .progress(progress)
                .activeSprint(sprintInfo)
                .build();

        return ProjectSummaryResponse.builder()
                .project(projectInfo)
                .stats(stats)
                .workflow(workflow)
                .metrics(metrics)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MyWorkResponse> getMyWork(String projectKey, Long currentUserId) {
        Project project = findProjectAndValidateMembership(projectKey, currentUserId);

        List<Ticket> tickets = ticketRepository.findByProjectIdAndAssigneeIdAndDeletedFalse(
                project.getId(), currentUserId);

        return tickets.stream()
                .map(ticket -> MyWorkResponse.builder()
                        .id(ticket.getId())
                        .key(project.getKey() + "-" + ticket.getId())
                        .title(ticket.getTitle())
                        .status(ticket.getStatus().name())
                        .priority(ticket.getPriority().name())
                        .dueDate(ticket.getDueDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivities(String projectKey, Long currentUserId) {
        Project project = findProjectAndValidateMembership(projectKey, currentUserId);

        List<Activity> activities = activityRepository.findTop20ByProjectIdOrderByCreatedAtDesc(project.getId());

        return activities.stream()
                .map(activity -> ActivityResponse.builder()
                        .id(activity.getId())
                        .user(ActivityResponse.UserInfo.builder()
                                .name(activity.getUser().getName())
                                .avatar(null)
                                .build())
                        .action(activity.getAction())
                        .target(activity.getTarget())
                        .to(activity.getToValue())
                        .timestamp(activity.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeadlineResponse> getDeadlines(String projectKey, Long currentUserId) {
        Project project = findProjectAndValidateMembership(projectKey, currentUserId);

        List<Ticket> tickets = ticketRepository
                .findUpcomingDeadlines(project.getId(), List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));

        LocalDateTime now = LocalDateTime.now();

        return tickets.stream()
                .map(ticket -> DeadlineResponse.builder()
                        .id(ticket.getId())
                        .key(project.getKey() + "-" + ticket.getId())
                        .title(ticket.getTitle())
                        .dueDate(ticket.getDueDate())
                        .overdue(ticket.getDueDate() != null && ticket.getDueDate().isBefore(now))
                        .build())
                .collect(Collectors.toList());
    }

    private Project findProjectAndValidateMembership(String projectKey, Long currentUserId) {
        Project project = projectRepository.findByKeyAndDeletedFalse(projectKey)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with key: " + projectKey));

        projectMemberRepository.findByProjectIdAndUserId(project.getId(), currentUserId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not accessible"));

        return project;
    }
}