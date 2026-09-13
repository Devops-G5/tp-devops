package com.deploy.devops.tasks.usecase;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String id) {
        super("Task not found: " + id);
    }
}
