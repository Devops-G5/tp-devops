package com.deploy.devops.tasks.delivery.interfaces;

import com.deploy.devops.tasks.domain.Task;

public interface ICreateTask {
    Task execute(Task task);
}
