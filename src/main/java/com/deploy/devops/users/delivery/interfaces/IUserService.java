package com.deploy.devops.users.delivery.interfaces;

import com.deploy.devops.users.domain.User;
import java.util.List;

public interface IUserService {
    User create(User user);
    User update(String id, User user);
    void delete(String id);
    User get(String id);
    List<User> getAll();
}
