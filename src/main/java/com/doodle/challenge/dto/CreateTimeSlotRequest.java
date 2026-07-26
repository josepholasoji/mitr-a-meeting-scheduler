package com.doodle.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateTimeSlotRequest(
        @NotNull @Schema(example = "2026-08-01T09:00:00Z") Instant startTime,
        @NotNull @Schema(example = "2026-08-01T09:30:00Z") Instant endTime) {
}
