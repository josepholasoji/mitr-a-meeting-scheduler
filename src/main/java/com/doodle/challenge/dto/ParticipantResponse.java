package com.doodle.challenge.dto;

import com.doodle.challenge.entity.ParticipantRole;
import com.doodle.challenge.entity.ParticipantStatus;
import java.time.Instant;
import java.util.UUID;

public record ParticipantResponse(UUID userId, ParticipantRole role, ParticipantStatus status, Instant respondedAt) {
}
