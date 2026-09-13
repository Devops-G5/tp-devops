package com.deploy.devops.tasks.delivery;

import com.deploy.devops.tasks.usecase.TaskNotFoundException;
import com.deploy.devops.tasks.usecase.TaskAlreadyAssignedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TaskExceptionHandler {
    @ExceptionHandler(TaskAlreadyAssignedException.class)
    public ProblemDetail alreadyAssigned(TaskAlreadyAssignedException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail notFound(TaskNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }
}
