package com.doodle.challenge.controller;

import com.doodle.challenge.dto.*;
import com.doodle.challenge.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Meetings")
public class MeetingController {

    private static final int MAX_PAGE_SIZE = 100;

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping("/meetings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Schedule a meeting by converting a FREE time slot")
    public MeetingResponse scheduleMeeting(@Valid @RequestBody ScheduleMeetingRequest request) {
        List<UUID> participantIds = request.participantIds() == null ? List.of() : request.participantIds();
        return meetingService.scheduleMeeting(
                new ScheduleMeetingCommand(request.timeSlotId(), request.title(), request.description(), participantIds));
    }

    @GetMapping("/meetings/{id}")
    @Operation(summary = "Get a meeting by id")
    public MeetingResponse getMeeting(@PathVariable UUID id) {
        return meetingService.getMeeting(id);
    }

    @GetMapping("/users/{userId}/meetings")
    @Operation(summary = "List all meetings a user owns or participates in, sorted by start time (paginated)")
    public PageResponse<MeetingResponse> getMeetingsForUser(
            @PathVariable UUID userId,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, capped at " + MAX_PAGE_SIZE) @RequestParam(defaultValue = "20") int size) {
        return meetingService.getMeetingsForUser(userId, PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE)));
    }

    @PatchMapping("/meetings/{id}")
    @Operation(summary = "Update a meeting's title/description (owner only)")
    public MeetingResponse updateMeeting(
            @PathVariable UUID id, @AuthenticationPrincipal UUID requesterId, @Valid @RequestBody UpdateMeetingRequest request) {
        return meetingService.updateMeeting(new UpdateMeetingCommand(id, requesterId, request.title(), request.description()));
    }

    @DeleteMapping("/meetings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a meeting (owner only) - frees its time slot")
    public void cancelMeeting(@PathVariable UUID id, @AuthenticationPrincipal UUID requesterId) {
        meetingService.cancelMeeting(new CancelMeetingCommand(id, requesterId));
    }
}
