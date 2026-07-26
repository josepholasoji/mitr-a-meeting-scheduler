package com.doodle.challenge.controller;

import com.doodle.challenge.dto.*;
import com.doodle.challenge.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Time Slots")
public class TimeSlotController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CalendarService calendarService;

    public TimeSlotController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @PostMapping("/users/{userId}/timeslots")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a time slot on a user's calendar")
    public TimeSlotResponse createTimeSlot(@PathVariable UUID userId, @Valid @RequestBody CreateTimeSlotRequest request) {
        return calendarService.createTimeSlot(new CreateTimeSlotCommand(userId, request.startTime(), request.endTime()));
    }

    @GetMapping("/users/{userId}/timeslots")
    @Operation(summary = "List a user's non-deleted time slots, sorted by start time (paginated)")
    public PageResponse<TimeSlotResponse> getTimeSlots(
            @PathVariable UUID userId,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, capped at " + MAX_PAGE_SIZE) @RequestParam(defaultValue = "20") int size) {
        return calendarService.getTimeSlots(userId, PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE)));
    }

    @PatchMapping("/timeslots/{id}")
    @Operation(summary = "Reschedule a FREE time slot")
    public TimeSlotResponse updateTimeSlot(@PathVariable UUID id, @Valid @RequestBody UpdateTimeSlotRequest request) {
        return calendarService.updateTimeSlot(new UpdateTimeSlotCommand(id, request.startTime(), request.endTime()));
    }

    @DeleteMapping("/timeslots/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a time slot (rejected if it currently backs a meeting)")
    public void deleteTimeSlot(@PathVariable UUID id) {
        calendarService.deleteTimeSlot(id);
    }

    @PatchMapping("/timeslots/{id}/status")
    @Operation(summary = "Manually mark a time slot FREE or BUSY, independent of scheduling a meeting")
    public TimeSlotResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeTimeSlotStatusRequest request) {
        return calendarService.changeSlotStatus(new ChangeTimeSlotStatusCommand(id, request.status()));
    }
}
