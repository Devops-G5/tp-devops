package com.deploy.devops.tasks.usecase;

import lombok.RequiredArgsConstructor;
import com.deploy.devops.tasks.delivery.interfaces.ICreateTask;
import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.usecase.interfaces.ITaskRepository;
import java.util.Date;

@RequiredArgsConstructor
public class CreateTask implements ICreateTask {
    private final ITaskRepository repository;

    @Override
    public Task execute(Task task) {
        task.setId(null);
        task.setAssignee(null);
        Date now = new Date();
        task.setCreated(now);
        task.setUpdated(now);
        return repository.create(task);
    }
}
