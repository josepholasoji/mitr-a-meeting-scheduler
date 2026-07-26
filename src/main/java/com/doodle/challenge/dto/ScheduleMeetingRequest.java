package com.doodle.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ScheduleMeetingRequest(
        @NotNull UUID timeSlotId,
        @NotBlank @Schema(example = "Sprint planning") String title,
        @Schema(example = "Plan the next two-week sprint") String description,
        @Schema(description = "Optional initial invitees, in addition to the owner") List<UUID> participantIds) {
}
