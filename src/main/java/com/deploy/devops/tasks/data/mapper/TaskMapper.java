package com.deploy.devops.tasks.data.mapper;

import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.infrastructure.TaskDocument;

public final class TaskMapper {
    private TaskMapper() {
    }

    public static TaskDocument toDocument(Task task) {
        TaskDocument document = new TaskDocument();
        document.setId(task.getId());
        document.setName(task.getName());
        document.setDescription(task.getDescription());
        document.setStatus(task.getStatus());
        document.setType(task.getType());
        document.setOwner(task.getOwner());
        document.setAssignee(task.getAssignee());
        document.setCreated(task.getCreated());
        document.setUpdated(task.getUpdated());
        return document;
    }

    public static Task toDomain(TaskDocument document) {
        Task task = new Task();
        task.setId(document.getId());
        task.setName(document.getName());
        task.setDescription(document.getDescription());
        task.setStatus(document.getStatus());
        task.setType(document.getType());
        task.setOwner(document.getOwner());
        task.setAssignee(document.getAssignee());
        task.setCreated(document.getCreated());
        task.setUpdated(document.getUpdated());
        return task;
    }
}
