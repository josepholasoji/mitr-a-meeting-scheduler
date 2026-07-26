package com.doodle.challenge.event;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredAt();
}
