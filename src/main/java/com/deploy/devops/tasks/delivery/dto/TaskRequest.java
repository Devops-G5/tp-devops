package com.deploy.devops.tasks.delivery.dto;

import com.deploy.devops.tasks.domain.Task;
import jakarta.validation.constraints.NotBlank;

public record TaskRequest(@NotBlank String name, String description, String status,
                          String type, String owner) {
    public Task toDomain() {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setStatus(status);
        task.setType(type);
        task.setOwner(owner);
        return task;
    }
}
