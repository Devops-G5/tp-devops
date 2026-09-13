package com.deploy.devops.users.data;

import com.deploy.devops.users.data.interfaces.IUserDataSource;
import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {
    private final IUserDataSource dataSource;

    public User create(User user) { return dataSource.create(user); }
    public Optional<User> update(String id, User user) { return dataSource.update(id, user); }
    public boolean delete(String id) { return dataSource.delete(id); }
    public Optional<User> get(String id) { return dataSource.get(id); }
    public List<User> getAll() { return dataSource.getAll(); }
}
