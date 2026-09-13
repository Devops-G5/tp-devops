package com.deploy.devops.tasks.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@NoArgsConstructor
public class Task {
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
