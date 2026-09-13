package com.deploy.devops.tasks.usecase.interfaces;

import com.deploy.devops.tasks.domain.Task;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ITaskRepository {
    Task create(Task task);
    Optional<Task> update(String id, Task task);
    Optional<Task> assignIfUnassigned(String id, String userId, Date updated);
    boolean delete(String id);
    Optional<Task> get(String id);
    List<Task> getAll();
}
