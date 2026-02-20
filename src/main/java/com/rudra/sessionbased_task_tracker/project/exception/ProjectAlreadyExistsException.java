package com.rudra.sessionbased_task_tracker.project.exception;

public class ProjectAlreadyExistsException extends RuntimeException {
    public ProjectAlreadyExistsException(String key) {
        super(
                "Project already exists: " + key
        );
    }
}
