package com.doodle.challenge.controller;

import com.doodle.challenge.dto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import jdk.jshell.spi.ExecutionControl;

@RestController
@Tag(name = "Meetings")
public class MeetingController {

    private static final int MAX_PAGE_SIZE = 100;


    public MeetingController() {
    }

    @PostMapping("/meetings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Schedule a meeting by converting a FREE time slot")
    public MeetingResponse scheduleMeeting(@Valid @RequestBody ScheduleMeetingRequest request) {
        throw new NotImplementedException("Not implemented");    // Not implemented
      }

    @GetMapping("/meetings/{id}")
    @Operation(summary = "Get a meeting by id")
    public MeetingResponse getMeeting(@PathVariable UUID id) {
       throw new NotImplementedException("Not implemented");    // Not implemented
     }

    @GetMapping("/users/{userId}/meetings")
    @Operation(summary = "List all meetings a user owns or participates in, sorted by start time (paginated)")
    public PageResponse<MeetingResponse> getMeetingsForUser(
            @PathVariable UUID userId,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, capped at " + MAX_PAGE_SIZE) @RequestParam(defaultValue = "20") int size) {
       throw new NotImplementedException("Not implemented");    // Not implemented
     }

    @PatchMapping("/meetings/{id}")
    @Operation(summary = "Update a meeting's title/description (owner only)")
    public MeetingResponse updateMeeting(
            @PathVariable UUID id, @AuthenticationPrincipal UUID requesterId, @Valid @RequestBody UpdateMeetingRequest request) {
        throw new NotImplementedException("Not implemented");    // Not implemented
    }

    @DeleteMapping("/meetings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a meeting (owner only) - frees its time slot")
    public void cancelMeeting(@PathVariable UUID id, @AuthenticationPrincipal UUID requesterId) {
        throw new NotImplementedException("Not implemented");    // Not implemented
    }
}
