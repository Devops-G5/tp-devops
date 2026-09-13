package com.deploy.devops.tasks.infrastructure;

import com.deploy.devops.tasks.usecase.*;
import com.deploy.devops.tasks.usecase.interfaces.ITaskRepository;
import com.deploy.devops.users.usecase.interfaces.IUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskConfiguration {
    @Bean
    AssignTask assignTask(ITaskRepository repository, IUserRepository userRepository) {
        return new AssignTask(repository, userRepository);
    }
    @Bean
    CreateTask createTask(ITaskRepository repository) { return new CreateTask(repository); }
    @Bean
    UpdateTask updateTask(ITaskRepository repository) { return new UpdateTask(repository); }
    @Bean
    GetTask getTask(ITaskRepository repository) { return new GetTask(repository); }
    @Bean
    GetAllTasks getAllTasks(ITaskRepository repository) { return new GetAllTasks(repository); }
    @Bean
    DeleteTask deleteTask(ITaskRepository repository) { return new DeleteTask(repository); }
}
