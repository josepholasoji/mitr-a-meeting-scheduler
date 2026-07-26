package com.doodle.challenge.controller;

import com.doodle.challenge.dto.CreateUserCommand;
import com.doodle.challenge.dto.CreateUserRequest;
import com.doodle.challenge.dto.UserResponse;
import com.doodle.challenge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(new CreateUserCommand(request.name(), request.email(), request.password()));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get a user by id")
    public UserResponse getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }
}
