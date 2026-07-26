package com.doodle.challenge.event;

import java.time.Instant;
import java.util.UUID;

public record TimeSlotMarkedFree(UUID slotId, UUID userId, Instant occurredAt) implements DomainEvent {

    public TimeSlotMarkedFree(UUID slotId, UUID userId) {
        this(slotId, userId, Instant.now());
    }
}
