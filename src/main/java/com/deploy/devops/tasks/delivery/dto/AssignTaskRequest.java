package com.deploy.devops.tasks.delivery.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignTaskRequest(@NotBlank String userId) {
}
