package com.deploy.devops.users.delivery;

import com.deploy.devops.users.delivery.dto.UserRequest;

import lombok.RequiredArgsConstructor;
import com.deploy.devops.users.delivery.interfaces.IUserService;
import com.deploy.devops.users.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService service;

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody UserRequest request) {
        User user = service.create(request.toDomain());
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(user);
    }

    @GetMapping
    public List<User> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public User get(@PathVariable String id) { return service.get(id); }

    @PutMapping("/{id}")
    public User update(@PathVariable String id, @Valid @RequestBody UserRequest request) {
        return service.update(id, request.toDomain());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
