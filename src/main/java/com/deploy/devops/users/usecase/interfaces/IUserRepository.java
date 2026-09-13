package com.deploy.devops.users.usecase.interfaces;

import com.deploy.devops.users.domain.User;
import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    User create(User user);
    Optional<User> update(String id, User user);
    boolean delete(String id);
    Optional<User> get(String id);
    List<User> getAll();
}
