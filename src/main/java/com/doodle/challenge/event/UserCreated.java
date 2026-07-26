package com.doodle.challenge.event;

import java.time.Instant;
import java.util.UUID;

public record UserCreated(UUID userId, Instant occurredAt) implements DomainEvent {

    public UserCreated(UUID userId) {
        this(userId, Instant.now());
    }
}
