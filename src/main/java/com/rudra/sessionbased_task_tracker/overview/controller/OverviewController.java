    package com.rudra.sessionbased_task_tracker.overview.controller;

    import com.rudra.sessionbased_task_tracker.activity.dto.ActivityResponse;
    import com.rudra.sessionbased_task_tracker.overview.dto.DeadlineResponse;
    import com.rudra.sessionbased_task_tracker.overview.dto.MyWorkResponse;
    import com.rudra.sessionbased_task_tracker.overview.dto.ProjectSummaryResponse;
    import com.rudra.sessionbased_task_tracker.overview.service.OverviewService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/v1/projects/{projectKey}")
    @RequiredArgsConstructor
    public class OverviewController {

        private final OverviewService overviewService;

        @GetMapping("/summary")
        public ResponseEntity<ProjectSummaryResponse> getProjectSummary(
                @PathVariable String projectKey,
                @RequestAttribute("userId") Long currentUserId) {
            return ResponseEntity.ok(overviewService.getProjectSummary(projectKey, currentUserId));
        }

        @GetMapping("/my-work")
        public ResponseEntity<List<MyWorkResponse>> getMyWork(
                @PathVariable String projectKey,
                @RequestAttribute("userId") Long currentUserId) {
            return ResponseEntity.ok(overviewService.getMyWork(projectKey, currentUserId));
        }

        @GetMapping("/activities")
        public ResponseEntity<List<ActivityResponse>> getActivities(
                @PathVariable String projectKey,
                @RequestAttribute("userId") Long currentUserId) {
            return ResponseEntity.ok(overviewService.getActivities(projectKey, currentUserId));
        }

        @GetMapping("/deadlines")
        public ResponseEntity<List<DeadlineResponse>> getDeadlines(
                @PathVariable String projectKey,
                @RequestAttribute("userId") Long currentUserId) {
            return ResponseEntity.ok(overviewService.getDeadlines(projectKey, currentUserId));
        }
    }