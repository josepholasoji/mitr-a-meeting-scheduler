package com.doodle.challenge.dto;

import java.util.UUID;

public record RemoveParticipantCommand(UUID meetingId, UUID requesterId, UUID userId) {
}
