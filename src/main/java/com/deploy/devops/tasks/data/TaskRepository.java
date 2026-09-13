package com.deploy.devops.tasks.data;

import com.deploy.devops.tasks.data.interfaces.ITaskDataSource;
import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.usecase.interfaces.ITaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskRepository implements ITaskRepository {
    private final ITaskDataSource dataSource;

    public Task create(Task task) { return dataSource.create(task); }
    public Optional<Task> update(String id, Task task) { return dataSource.update(id, task); }
    public Optional<Task> assignIfUnassigned(String id, String userId, Date updated) {
        return dataSource.assignIfUnassigned(id, userId, updated);
    }
    public boolean delete(String id) { return dataSource.delete(id); }
    public Optional<Task> get(String id) { return dataSource.get(id); }
    public List<Task> getAll() { return dataSource.getAll(); }
}
