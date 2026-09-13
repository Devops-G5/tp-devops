package com.deploy.devops.users.data.mapper;

import com.deploy.devops.users.domain.User;
import com.deploy.devops.users.infrastructure.UserDocument;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDocument toDocument(User user) {
        UserDocument document = new UserDocument();
        document.setId(user.getId());
        document.setName(user.getName());
        document.setLastname(user.getLastname());
        document.setUsername(user.getUsername());
        document.setEmail(user.getEmail());
        document.setRol(user.getRol());
        return document;
    }

    public static User toDomain(UserDocument document) {
        User user = new User();
        user.setId(document.getId());
        user.setName(document.getName());
        user.setLastname(document.getLastname());
        user.setUsername(document.getUsername());
        user.setEmail(document.getEmail());
        user.setRol(document.getRol());
        return user;
    }
}
