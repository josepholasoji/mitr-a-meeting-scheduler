package com.doodle.challenge.event;

import java.time.Instant;
import java.util.UUID;

public record ParticipantDeclinedInvitation(UUID meetingId, UUID userId, Instant occurredAt)
        implements DomainEvent {

    public ParticipantDeclinedInvitation(UUID meetingId, UUID userId) {
        this(meetingId, userId, Instant.now());
    }
}
