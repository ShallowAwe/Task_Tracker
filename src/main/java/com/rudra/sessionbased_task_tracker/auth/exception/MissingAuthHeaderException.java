package com.rudra.sessionbased_task_tracker.auth.exception;

public class MissingAuthHeaderException extends RuntimeException {
    public MissingAuthHeaderException(String message) {
        super(message);
    }
}
