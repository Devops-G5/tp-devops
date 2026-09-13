package com.deploy.devops.users;

import com.deploy.devops.users.data.mapper.UserMapper;

import com.deploy.devops.users.delivery.dto.UserRequest;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.infrastructure.UserDataSource;
import com.deploy.devops.users.infrastructure.UserDocument;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserDataSourceTests {
    @Test
    void updateUsesRequestedIdAndReplacesAllEditableFieldsWithoutUpsert() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        User user = new UserRequest("Ana", "Perez", "ana", "ana@example.com", "designer").toDomain();
        user.setId("untrusted-id");
        assertTrue(new UserDataSource(mongo).update("requested-id", user).isEmpty());

        var query = ArgumentCaptor.forClass(Query.class);
        var update = ArgumentCaptor.forClass(Update.class);
        var options = ArgumentCaptor.forClass(FindAndModifyOptions.class);
        verify(mongo).findAndModify(query.capture(), update.capture(), options.capture(), eq(UserDocument.class));
        assertEquals("requested-id", query.getValue().getQueryObject().getString("id"));
        var fields = (org.bson.Document) update.getValue().getUpdateObject().get("$set");
        assertEquals(5, fields.size());
        assertEquals("Ana", fields.getString("name"));
        assertEquals("Perez", fields.getString("lastname"));
        assertEquals("ana", fields.getString("username"));
        assertEquals("ana@example.com", fields.getString("email"));
        assertEquals("designer", fields.getString("rol"));
        assertFalse(fields.containsKey("id"));
        assertFalse(options.getValue().isUpsert());
        assertTrue(options.getValue().isReturnNew());
    }

    @Test
    void documentMappingPreservesAllFields() {
        User user = new UserRequest("Ana", "Perez", "ana", "ana@example.com", "developer").toDomain();
        user.setId("id");
        org.assertj.core.api.Assertions.assertThat(UserMapper.toDomain(UserMapper.toDocument(user)))
                .usingRecursiveComparison().isEqualTo(user);
    }
}
