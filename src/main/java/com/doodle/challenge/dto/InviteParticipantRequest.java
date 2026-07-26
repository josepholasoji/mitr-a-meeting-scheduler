package com.doodle.challenge.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InviteParticipantRequest(@NotNull UUID userId) {
}
