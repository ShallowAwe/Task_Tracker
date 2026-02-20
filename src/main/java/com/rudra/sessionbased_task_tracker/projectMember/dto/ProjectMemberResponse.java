package com.rudra.sessionbased_task_tracker.projectMember.dto;

import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {

    private Long id;
    private Long projectId;
    private String projectKey;
    private String projectName;
    private Long userId;
    private String userName;
    private String userEmail;
    private ProjectRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}