package com.deploy.devops.tasks;

import com.deploy.devops.tasks.data.mapper.TaskMapper;

import com.deploy.devops.tasks.domain.Task;
import com.deploy.devops.tasks.infrastructure.TaskDataSource;
import com.deploy.devops.tasks.infrastructure.TaskDocument;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskDataSourceTests {
    @Test
    void updateIsAtomicAndDoesNotOverwriteCreationMetadataOrUpsert() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        Task task = new Task();
        task.setName("Updated");
        task.setId("untrusted-id");
        task.setCreated(new Date(0));
        task.setUpdated(new Date());
        task.setAssignee("must-not-overwrite-assignment");
        assertTrue(new TaskDataSource(mongo).update("requested-id", task).isEmpty());

        var query = ArgumentCaptor.forClass(Query.class);
        var update = ArgumentCaptor.forClass(Update.class);
        var options = ArgumentCaptor.forClass(FindAndModifyOptions.class);
        verify(mongo).findAndModify(query.capture(), update.capture(), options.capture(), eq(TaskDocument.class));
        assertEquals("requested-id", query.getValue().getQueryObject().getString("id"));
        var fields = (org.bson.Document) update.getValue().getUpdateObject().get("$set");
        assertFalse(fields.containsKey("created"));
        assertFalse(fields.containsKey("id"));
        assertEquals("Updated", fields.getString("name"));
        assertEquals(task.getUpdated(), fields.getDate("updated"));
        assertFalse(fields.containsKey("assignee"));
        assertFalse(options.getValue().isUpsert());
        assertTrue(options.getValue().isReturnNew());
    }

    @Test
    void documentMappingPreservesAllDomainFields() {
        Task task = new Task();
        task.setId("id");
        task.setName("name");
        task.setDescription("description");
        task.setStatus("pending");
        task.setType("devops");
        task.setOwner("owner");
        task.setAssignee("assignee");
        task.setCreated(new Date(1000));
        task.setUpdated(new Date(2000));
        Task result = TaskMapper.toDomain(TaskMapper.toDocument(task));
        org.assertj.core.api.Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(task);
    }

    @Test
    void assignmentRequiresUnassignedTaskAndUpdatesOnlyAssigneeAndTimestamp() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        Date now = new Date();
        TaskDocument stored = new TaskDocument();
        stored.setId("task-id");
        stored.setName("Original name");
        stored.setAssignee("user-id");
        stored.setUpdated(now);
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
                eq(TaskDocument.class))).thenReturn(stored);
        Task result = new TaskDataSource(mongo).assignIfUnassigned("task-id", "user-id", now).orElseThrow();
        assertEquals("Original name", result.getName());
        assertEquals("user-id", result.getAssignee());

        var query = ArgumentCaptor.forClass(Query.class);
        var update = ArgumentCaptor.forClass(Update.class);
        var options = ArgumentCaptor.forClass(FindAndModifyOptions.class);
        verify(mongo).findAndModify(query.capture(), update.capture(), options.capture(), eq(TaskDocument.class));
        assertEquals(new org.bson.Document("id", "task-id").append("assignee", null),
                query.getValue().getQueryObject());
        assertEquals(new org.bson.Document("$set", new org.bson.Document("assignee", "user-id")
                .append("updated", now)), update.getValue().getUpdateObject());
        assertFalse(options.getValue().isUpsert());
        assertTrue(options.getValue().isReturnNew());
    }
}
