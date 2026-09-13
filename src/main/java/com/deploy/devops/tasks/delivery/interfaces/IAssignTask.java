package com.deploy.devops.tasks.delivery.interfaces;

import com.deploy.devops.tasks.domain.Task;

public interface IAssignTask {
    Task execute(String taskId, String userId);
}
