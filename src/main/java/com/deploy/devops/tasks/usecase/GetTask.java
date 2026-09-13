package com.deploy.devops.tasks.usecase;

import com.deploy.devops.tasks.delivery.interfaces.IGetTask;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.usecase.interfaces.ITaskRepository;

@RequiredArgsConstructor
public class GetTask implements IGetTask {
    private final ITaskRepository repository;

    @Override
    public Task execute(String id) {
        return repository.get(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
}
