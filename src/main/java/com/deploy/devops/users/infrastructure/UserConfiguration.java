package com.deploy.devops.users.infrastructure;

import com.deploy.devops.users.usecase.*;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {
    @Bean
    CreateUser createUser(IUserRepository repository) { return new CreateUser(repository); }
    @Bean
    UpdateUser updateUser(IUserRepository repository) { return new UpdateUser(repository); }
    @Bean
    GetUser getUser(IUserRepository repository) { return new GetUser(repository); }
    @Bean
    GetAllUsers getAllUsers(IUserRepository repository) { return new GetAllUsers(repository); }
    @Bean
    DeleteUser deleteUser(IUserRepository repository) { return new DeleteUser(repository); }
}
