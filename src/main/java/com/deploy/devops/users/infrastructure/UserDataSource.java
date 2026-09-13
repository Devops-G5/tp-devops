package com.deploy.devops.users.infrastructure;

import com.deploy.devops.users.data.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.users.data.interfaces.IUserDataSource;
import com.deploy.devops.users.domain.User;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDataSource implements IUserDataSource {
    private final MongoTemplate mongoTemplate;

    @Override
    public User create(User user) {
        return UserMapper.toDomain(mongoTemplate.insert(UserMapper.toDocument(user)));
    }

    @Override
    public Optional<User> update(String id, User user) {
        Update update = new Update()
                .set("name", user.getName())
                .set("lastname", user.getLastname())
                .set("username", user.getUsername())
                .set("email", user.getEmail())
                .set("rol", user.getRol());
        // Preserve the id and never insert a missing user.
        UserDocument result = mongoTemplate.findAndModify(byId(id), update,
                FindAndModifyOptions.options().returnNew(true), UserDocument.class);
        return Optional.ofNullable(result).map(UserMapper::toDomain);
    }

    @Override
    public boolean delete(String id) {
        return mongoTemplate.remove(byId(id), UserDocument.class).getDeletedCount() > 0;
    }

    @Override
    public Optional<User> get(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, UserDocument.class))
                .map(UserMapper::toDomain);
    }

    @Override
    public List<User> getAll() {
        return mongoTemplate.findAll(UserDocument.class).stream().map(UserMapper::toDomain).toList();
    }

    private Query byId(String id) {
        return Query.query(Criteria.where("id").is(id));
    }
}
