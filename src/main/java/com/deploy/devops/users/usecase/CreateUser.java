package com.deploy.devops.users.usecase;

import lombok.RequiredArgsConstructor;
import com.deploy.devops.users.delivery.interfaces.ICreateUser;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;

@RequiredArgsConstructor
public class CreateUser implements ICreateUser {
    private final IUserRepository repository;

    @Override
    public User execute(User user) {
        user.setId(null);
        return repository.create(user);
    }
}
