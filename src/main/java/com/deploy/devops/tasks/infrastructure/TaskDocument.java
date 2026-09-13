package com.deploy.devops.tasks.infrastructure;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "tasks")
public class TaskDocument {
    @Id
    private String id;
    private String name;
    private String description;
    private String status;
    private String type;
    private String owner;
    private String assignee;
    private Date created;
    private Date updated;
}
