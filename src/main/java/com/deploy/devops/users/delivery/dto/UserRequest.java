package com.deploy.devops.users.delivery.dto;

import com.deploy.devops.users.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(@NotBlank String name, @NotBlank String lastname,
                            @NotBlank String username, @NotBlank @Email String email,
                            @NotBlank String rol) {
    public User toDomain() {
        User user = new User();
        user.setName(name);
        user.setLastname(lastname);
        user.setUsername(username);
        user.setEmail(email);
        user.setRol(rol);
        return user;
    }
}
