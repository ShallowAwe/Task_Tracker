package com.rudra.sessionbased_task_tracker.projectMember.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
