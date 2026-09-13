package com.deploy.devops.users.delivery.interfaces;

import com.deploy.devops.users.domain.User;

public interface ICreateUser {
    User execute(User user);
}
