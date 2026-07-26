package com.doodle.challenge.event;

import java.time.Instant;
import java.util.UUID;

public record MeetingUpdated(UUID meetingId, Instant occurredAt) implements DomainEvent {

    public MeetingUpdated(UUID meetingId) {
        this(meetingId, Instant.now());
    }
}
