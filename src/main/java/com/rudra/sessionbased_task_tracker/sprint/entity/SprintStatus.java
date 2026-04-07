package com.rudra.sessionbased_task_tracker.sprint.entity;

public enum SprintStatus {
    PLANNING, // sprint is being prepared, tickets being added
    ACTIVE, // sprint is in progress (only ONE active per project)
    COMPLETED // sprint is done
}