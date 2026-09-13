package com.deploy.devops.tasks.delivery.interfaces;

import com.deploy.devops.tasks.domain.Task;
import java.util.List;

public interface IGetAllTasks {
    List<Task> execute();
}
