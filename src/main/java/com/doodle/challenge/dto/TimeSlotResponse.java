package com.doodle.challenge.dto;

import com.doodle.challenge.entity.TimeSlotStatus;
import java.time.Instant;
import java.util.UUID;

public record TimeSlotResponse(
        UUID id,
        UUID userId,
        Instant startTime,
        Instant endTime,
        TimeSlotStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
