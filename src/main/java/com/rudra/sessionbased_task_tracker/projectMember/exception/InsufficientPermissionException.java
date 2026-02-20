package com.rudra.sessionbased_task_tracker.projectMember.exception;

public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException(String message) {
        super(message);
    }
}
