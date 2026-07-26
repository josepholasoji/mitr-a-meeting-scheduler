package com.doodle.challenge.event;

import java.time.Instant;
import java.util.UUID;

public record MeetingScheduled(UUID meetingId, UUID timeSlotId, UUID ownerId, Instant occurredAt)
        implements DomainEvent {

    public MeetingScheduled(UUID meetingId, UUID timeSlotId, UUID ownerId) {
        this(meetingId, timeSlotId, ownerId, Instant.now());
    }
}
