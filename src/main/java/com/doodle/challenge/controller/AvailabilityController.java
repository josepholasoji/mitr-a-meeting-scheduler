package com.doodle.challenge.controller;

import com.doodle.challenge.dto.AvailabilityQuery;
import com.doodle.challenge.dto.AvailabilityResponse;
import com.doodle.challenge.entity.TimeSlotStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@Tag(name = "Availability")
public class AvailabilityController {


    public AvailabilityController() {
    }

    @GetMapping("/users/{userId}/availability")
    @Operation(summary = "Get a user's free/busy slots within a time window, optionally filtered by status")
    public AvailabilityResponse getAvailability(
            @PathVariable UUID userId,
            @Parameter(example = "2026-08-01T00:00:00Z") @RequestParam Instant from,
            @Parameter(example = "2026-08-02T00:00:00Z") @RequestParam Instant to,
            @RequestParam(required = false) TimeSlotStatus status) {
       throw new NotImplementedException("Not implemented");    // Not implemented
    }
}
