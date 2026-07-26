package com.metr.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Schema(example = "Ada Lovelace") String name,
        @NotBlank @Email @Schema(example = "ada@example.com") String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") @Schema(example = "correct-horse-battery-staple") String password) {
}
