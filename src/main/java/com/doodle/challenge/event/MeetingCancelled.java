package com.doodle.challenge.event;

import java.time.Instant;
import java.util.UUID;

public record MeetingCancelled(UUID meetingId, UUID timeSlotId, Instant occurredAt) implements DomainEvent {

    public MeetingCancelled(UUID meetingId, UUID timeSlotId) {
        this(meetingId, timeSlotId, Instant.now());
    }
}
