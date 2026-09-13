package com.deploy.devops.users.delivery;

import com.deploy.devops.users.delivery.interfaces.IDeleteUser;
import com.deploy.devops.users.delivery.interfaces.IGetUser;
import com.deploy.devops.users.delivery.interfaces.IGetAllUsers;
import com.deploy.devops.users.delivery.interfaces.IUpdateUser;
import lombok.RequiredArgsConstructor;
import com.deploy.devops.users.delivery.interfaces.ICreateUser;
import com.deploy.devops.users.delivery.interfaces.IUserService;
import com.deploy.devops.users.domain.User;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final ICreateUser createUser;
    private final IUpdateUser updateUser;
    private final IDeleteUser deleteUser;
    private final IGetUser getUser;
    private final IGetAllUsers getAllUsers;

    public User create(User user) { return createUser.execute(user); }
    public User update(String id, User user) { return updateUser.execute(id, user); }
    public void delete(String id) { deleteUser.execute(id); }
    public User get(String id) { return getUser.execute(id); }
    public List<User> getAll() { return getAllUsers.execute(); }
}
