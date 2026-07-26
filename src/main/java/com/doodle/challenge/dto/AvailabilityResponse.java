package com.doodle.challenge.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID userId,
        Instant windowStart,
        Instant windowEnd,
        List<TimeSlotResponse> freeSlots,
        List<TimeSlotResponse> busySlots) {
}
