package com.deploy.devops.tasks.delivery.interfaces;

import com.deploy.devops.tasks.domain.Task;

public interface IGetTask {
    Task execute(String id);
}
