package com.deploy.devops.tasks.delivery;

import com.deploy.devops.tasks.delivery.dto.AssignTaskRequest;
import com.deploy.devops.tasks.delivery.dto.TaskRequest;

import lombok.RequiredArgsConstructor;
import com.deploy.devops.tasks.delivery.interfaces.ITaskService;
import com.deploy.devops.tasks.domain.Task;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final ITaskService service;

    @PatchMapping("/{id}/assignee")
    public Task assign(@PathVariable String id, @Valid @RequestBody AssignTaskRequest request) {
        return service.assign(id, request.userId());
    }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest request) {
        Task task = service.create(request.toDomain());
        return ResponseEntity.created(URI.create("/api/tasks/" + task.getId())).body(task);
    }

    @GetMapping
    public List<Task> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Task get(@PathVariable String id) { return service.get(id); }

    @PutMapping("/{id}")
    public Task update(@PathVariable String id, @Valid @RequestBody TaskRequest request) {
        return service.update(id, request.toDomain());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
