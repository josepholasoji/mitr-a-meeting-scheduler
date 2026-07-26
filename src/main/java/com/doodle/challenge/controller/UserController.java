package com.metr.challenge.controller;

import com.metr.challenge.dto.CreateUserCommand;
import com.metr.challenge.dto.CreateUserRequest;
import com.metr.challenge.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Users")
public class UserController {

    public UserController() {
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
     }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get a user by id")
    public UserResponse getUser(@PathVariable UUID id) {
    }
}
