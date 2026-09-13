package com.deploy.devops.users.infrastructure;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
public class UserDocument {
    @Id
    private String id;
    private String name;
    private String lastname;
    private String username;
    private String email;
    private String rol;
}
