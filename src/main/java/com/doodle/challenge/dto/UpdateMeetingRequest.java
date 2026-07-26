package com.doodle.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateMeetingRequest(
        @NotBlank @Schema(example = "Sprint planning (rescheduled)") String title,
        @Schema(example = "Plan the next two-week sprint - moved out by a day") String description) {
}
