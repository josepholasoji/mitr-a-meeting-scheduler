package com.metr.challenge.dto;

public record CreateUserCommand(String name, String email, String rawPassword) {
}
