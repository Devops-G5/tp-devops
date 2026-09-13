package com.deploy.devops.tasks.usecase;

public class TaskAlreadyAssignedException extends RuntimeException {
    public TaskAlreadyAssignedException(String taskName) {
        super("Task " + taskName + " is already assigned." );
    }
}
