package com.doodle.challenge.event;

import java.time.Instant;
import java.util.UUID;

public record TimeSlotCreated(UUID slotId, UUID userId, Instant occurredAt) implements DomainEvent {

    public TimeSlotCreated(UUID slotId, UUID userId) {
        this(slotId, userId, Instant.now());
    }
}
