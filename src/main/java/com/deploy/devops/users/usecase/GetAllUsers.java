package com.deploy.devops.users.usecase;

import com.deploy.devops.users.delivery.interfaces.IGetAllUsers;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;
import java.util.List;

@RequiredArgsConstructor
public class GetAllUsers implements IGetAllUsers {
    private final IUserRepository repository;

    @Override
    public List<User> execute() { return repository.getAll(); }
}
