package com.deploy.devops.users.data.interfaces;

import com.deploy.devops.users.domain.User;
import java.util.List;
import java.util.Optional;

public interface IUserDataSource {
    User create(User user);
    Optional<User> update(String id, User user);
    boolean delete(String id);
    Optional<User> get(String id);
    List<User> getAll();
}
