package com.rudra.sessionbased_task_tracker.projectMember.dto;

import com.rudra.sessionbased_task_tracker.projectMember.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")
    private ProjectRole role;
}