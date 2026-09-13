package com.deploy.devops.users.delivery.interfaces;

import com.deploy.devops.users.domain.User;

public interface IGetUser {
    User execute(String id);
}
