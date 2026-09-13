package com.deploy.devops.tasks.infrastructure;

import com.deploy.devops.tasks.data.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.tasks.data.interfaces.ITaskDataSource;
import com.deploy.devops.tasks.domain.Task;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskDataSource implements ITaskDataSource {
    private final MongoTemplate mongoTemplate;

    @Override
    public Task create(Task task) {
        return TaskMapper.toDomain(mongoTemplate.insert(TaskMapper.toDocument(task)));
    }

    @Override
    public Optional<Task> update(String id, Task task) {
        Update update = new Update()
                .set("name", task.getName())
                .set("description", task.getDescription())
                .set("status", task.getStatus())
                .set("type", task.getType())
                .set("owner", task.getOwner())
                .set("updated", task.getUpdated());

        TaskDocument result = mongoTemplate.findAndModify(byId(id), update,
                FindAndModifyOptions.options().returnNew(true), TaskDocument.class);
        return Optional.ofNullable(result).map(TaskMapper::toDomain);
    }

    @Override
    public Optional<Task> assignIfUnassigned(String id, String userId, Date updated) {
        Query query = Query.query(Criteria.where("id").is(id).and("assignee").is(null));
        Update update = new Update().set("assignee", userId).set("updated", updated);
        TaskDocument result = mongoTemplate.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), TaskDocument.class);
        return Optional.ofNullable(result).map(TaskMapper::toDomain);
    }

    @Override
    public boolean delete(String id) {
        return mongoTemplate.remove(byId(id), TaskDocument.class).getDeletedCount() > 0;
    }

    @Override
    public Optional<Task> get(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, TaskDocument.class))
                .map(TaskMapper::toDomain);
    }

    @Override
    public List<Task> getAll() {
        return mongoTemplate.findAll(TaskDocument.class).stream().map(TaskMapper::toDomain).toList();
    }

    private Query byId(String id) {
        return Query.query(Criteria.where("id").is(id));
    }
}
