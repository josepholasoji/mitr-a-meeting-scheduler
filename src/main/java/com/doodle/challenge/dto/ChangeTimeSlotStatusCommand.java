package com.doodle.challenge.dto;

import com.doodle.challenge.entity.TimeSlotStatus;
import java.util.UUID;

public record ChangeTimeSlotStatusCommand(UUID slotId, TimeSlotStatus status) {
}
