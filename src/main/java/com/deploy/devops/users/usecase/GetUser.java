package com.deploy.devops.users.usecase;

import com.deploy.devops.users.delivery.interfaces.IGetUser;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;

@RequiredArgsConstructor
public class GetUser implements IGetUser {
    private final IUserRepository repository;

    @Override
    public User execute(String id) {
        return repository.get(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
