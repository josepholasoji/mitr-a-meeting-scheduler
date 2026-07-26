package com.doodle.challenge.dto;

public record CreateUserCommand(String name, String email, String rawPassword) {
}
