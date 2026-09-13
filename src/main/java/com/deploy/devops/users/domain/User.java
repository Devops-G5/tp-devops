package com.deploy.devops.users.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {
    private String id;
    private String name;
    private String lastname;
    private String username;
    private String email;
    private String rol;
}
