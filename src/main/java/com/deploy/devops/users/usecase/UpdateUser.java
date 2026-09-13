package com.deploy.devops.users.usecase;

import com.deploy.devops.users.delivery.interfaces.IUpdateUser;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;

@RequiredArgsConstructor
public class UpdateUser implements IUpdateUser {
    private final IUserRepository repository;

    @Override
    public User execute(String id, User user) {
        return repository.update(id, user).orElseThrow(() -> new UserNotFoundException(id));
    }
}
