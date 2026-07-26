package com.doodle.challenge.dto;

import com.doodle.challenge.entity.TimeSlotStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTimeSlotStatusRequest(@NotNull TimeSlotStatus status) {
}
