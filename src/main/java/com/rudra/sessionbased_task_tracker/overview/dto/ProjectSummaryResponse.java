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
}