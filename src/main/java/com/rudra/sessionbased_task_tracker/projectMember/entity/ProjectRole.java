package com.rudra.sessionbased_task_tracker.projectMember.entity;

import lombok.Getter;

@Getter
public enum ProjectRole {
    OWNER(4),
    MAINTAINER(3),
    DEVELOPER(2),
    VIEWER(1);

    private final int level;

    ProjectRole(int level) {
        this.level = level;
    }

    public boolean isAtLeast(ProjectRole required) {
        return this.level >= required.level;
    }
}