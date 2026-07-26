package com.doodle.challenge.dto;

import java.time.Instant;
import java.util.UUID;

public record UpdateTimeSlotCommand(UUID slotId, Instant startTime, Instant endTime) {
}
