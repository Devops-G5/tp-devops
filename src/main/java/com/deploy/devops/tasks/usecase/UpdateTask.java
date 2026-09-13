package com.deploy.devops.tasks.usecase;

import com.deploy.devops.tasks.delivery.interfaces.IUpdateTask;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.usecase.interfaces.ITaskRepository;
import java.util.Date;

@RequiredArgsConstructor
public class UpdateTask implements IUpdateTask {
    private final ITaskRepository repository;

    @Override
    public Task execute(String id, Task task) {
        task.setUpdated(new Date());
        return repository.update(id, task).orElseThrow(() -> new TaskNotFoundException(id));
    }
}
