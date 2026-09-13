package com.deploy.devops.tasks.usecase;

import com.deploy.devops.tasks.delivery.interfaces.IAssignTask;
import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.usecase.interfaces.ITaskRepository;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.usecase.UserNotFoundException;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@RequiredArgsConstructor
public class AssignTask implements IAssignTask {
    private final ITaskRepository repository;
    private final IUserRepository userRepository;

    @Override
    public Task execute(String taskId, String userId) {
        Task task = repository.get(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        if (task.getAssignee() != null) {
            throw new TaskAlreadyAssignedException(task.getName());
        }
        User user = userRepository.get(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return repository.assignIfUnassigned(taskId, userId, new Date()).orElseThrow(() -> {
            Task current = repository.get(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
            return new TaskAlreadyAssignedException(current.getName());
        });
    }
}
