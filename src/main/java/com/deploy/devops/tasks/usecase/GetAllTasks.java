package com.deploy.devops.tasks.usecase;

import com.deploy.devops.tasks.delivery.interfaces.IGetAllTasks;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.usecase.interfaces.ITaskRepository;
import java.util.List;

@RequiredArgsConstructor
public class GetAllTasks implements IGetAllTasks {
    private final ITaskRepository repository;

    @Override
    public List<Task> execute() { return repository.getAll(); }
}
