package com.deploy.devops.tasks.delivery.interfaces;

import com.deploy.devops.tasks.domain.Task;

public interface IUpdateTask {
    Task execute(String id, Task task);
}
