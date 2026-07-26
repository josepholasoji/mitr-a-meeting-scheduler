package com.doodle.challenge.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateTimeSlotCommand(UUID userId, Instant startTime, Instant endTime) {
}
