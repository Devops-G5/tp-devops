package com.deploy.devops.users.delivery.interfaces;

import com.deploy.devops.users.domain.User;

public interface IUpdateUser {
    User execute(String id, User user);
}
