package com.rudra.sessionbased_task_tracker.Home.dto;

import com.rudra.sessionbased_task_tracker.activity.dto.ActivityResponse;
import com.rudra.sessionbased_task_tracker.overview.dto.DeadlineResponse;
import com.rudra.sessionbased_task_tracker.overview.dto.MyWorkResponse;
import com.rudra.sessionbased_task_tracker.overview.dto.ProjectSummaryResponse;
import com.rudra.sessionbased_task_tracker.project.dto.ProjectLite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {
    private boolean hasProjects;
    private String defaultProjectKey;
    private List<ProjectLite> projects;

    private ProjectSummaryResponse summary;
    private List<MyWorkResponse> myWork;
    private List<ActivityResponse> activities;
    private List<DeadlineResponse> deadlines;
}
