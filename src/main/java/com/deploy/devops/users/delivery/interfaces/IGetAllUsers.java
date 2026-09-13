package com.deploy.devops.users.delivery.interfaces;

import com.deploy.devops.users.domain.User;
import java.util.List;

public interface IGetAllUsers {
    List<User> execute();
}
