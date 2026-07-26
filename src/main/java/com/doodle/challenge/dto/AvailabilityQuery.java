package com.doodle.challenge.dto;

import com.doodle.challenge.entity.TimeSlotStatus;
import java.time.Instant;
import java.util.UUID;

public record AvailabilityQuery(UUID userId, Instant from, Instant to, TimeSlotStatus status) {
}
