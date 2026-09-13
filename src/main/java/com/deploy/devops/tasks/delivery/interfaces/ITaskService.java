package com.deploy.devops.tasks.delivery.interfaces;

import com.deploy.devops.tasks.domain.Task;
import java.util.List;

public interface ITaskService {
    Task assign(String taskId, String userId);
    Task create(Task task);
    Task update(String id, Task task);
    void delete(String id);
    Task get(String id);
    List<Task> getAll();
}
