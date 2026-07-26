package com.doodle.challenge.event;

import java.time.Instant;
import java.util.UUID;

public record ParticipantInvited(UUID meetingId, UUID userId, Instant occurredAt) implements DomainEvent {

    public ParticipantInvited(UUID meetingId, UUID userId) {
        this(meetingId, userId, Instant.now());
    }
}
