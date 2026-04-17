package com.rudra.sessionbased_task_tracker.overview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryResponse {

    private ProjectInfo project;
    private List<StatItem> stats;
    private List<WorkflowItem> workflow;
    private MetricCards metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private String name;
        private String status;
        private int progress;
        private SprintInfo activeSprint;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SprintInfo {
        private String name;
        private int daysLeft;
        private int totalDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatItem {
        private String label;
        private long value;
        private String trend;
        private boolean trendUp;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowItem {
        private String status;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricCards {
        private long totalTickets;
        private long overdueTickets;
        private long unassignedTickets;
        private long teamMembers;
        private int completionRate;
        private SprintMetric activeSprint;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SprintMetric {
        private String name;
        private String goal;
        private String status;
        private int daysLeft;
        private int totalDays;
        private int totalTickets;
        private int completedTickets;
    }
}