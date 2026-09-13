package com.deploy.devops.tasks.usecase;

import com.deploy.devops.tasks.delivery.interfaces.IDeleteTask;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.tasks.usecase.interfaces.ITaskRepository;

@RequiredArgsConstructor
public class DeleteTask implements IDeleteTask {
    private final ITaskRepository repository;

    @Override
    public void execute(String id) {
        if (!repository.delete(id)) {
            throw new TaskNotFoundException(id);
        }
    }
}
