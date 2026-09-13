package com.deploy.devops.tasks.delivery;

import com.deploy.devops.tasks.delivery.interfaces.IDeleteTask;
import com.deploy.devops.tasks.delivery.interfaces.IGetTask;
import com.deploy.devops.tasks.delivery.interfaces.IGetAllTasks;
import com.deploy.devops.tasks.delivery.interfaces.IUpdateTask;
import com.deploy.devops.tasks.delivery.interfaces.IAssignTask;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.tasks.delivery.interfaces.ICreateTask;
import com.deploy.devops.tasks.delivery.interfaces.ITaskService;
import com.deploy.devops.tasks.domain.Task;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService {
    private final ICreateTask createTask;
    private final IUpdateTask updateTask;
    private final IDeleteTask deleteTask;
    private final IGetTask getTask;
    private final IGetAllTasks getAllTasks;
    private final IAssignTask assignTask;

    public Task assign(String taskId, String userId) { return assignTask.execute(taskId, userId); }
    public Task create(Task task) { return createTask.execute(task); }
    public Task update(String id, Task task) { return updateTask.execute(id, task); }
    public void delete(String id) { deleteTask.execute(id); }
    public Task get(String id) { return getTask.execute(id); }
    public List<Task> getAll() { return getAllTasks.execute(); }
}
