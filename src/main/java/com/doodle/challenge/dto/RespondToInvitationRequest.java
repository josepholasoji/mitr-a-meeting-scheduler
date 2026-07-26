package com.doodle.challenge.dto;

import com.doodle.challenge.entity.ParticipantStatus;
import jakarta.validation.constraints.NotNull;

public record RespondToInvitationRequest(@NotNull ParticipantStatus status) {
}
