package com.deploy.devops.users.usecase;

import com.deploy.devops.users.delivery.interfaces.IDeleteUser;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;

@RequiredArgsConstructor
public class DeleteUser implements IDeleteUser {
    private final IUserRepository repository;

    @Override
    public void execute(String id) {
        if (!repository.delete(id)) {
            throw new UserNotFoundException(id);
        }
    }
}
