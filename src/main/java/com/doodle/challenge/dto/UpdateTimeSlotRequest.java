package com.doodle.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UpdateTimeSlotRequest(
        @NotNull @Schema(example = "2026-08-01T10:00:00Z") Instant startTime,
        @NotNull @Schema(example = "2026-08-01T10:30:00Z") Instant endTime) {
}
