package com.doodle.challenge.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        String title,
        String description,
        UUID ownerId,
        UUID timeSlotId,
        Instant startTime,
        Instant endTime,
        List<ParticipantResponse> participants,
        Instant createdAt,
        Instant updatedAt) {
}
