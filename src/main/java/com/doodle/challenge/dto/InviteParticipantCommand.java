package com.doodle.challenge.dto;

import java.util.UUID;

public record InviteParticipantCommand(UUID meetingId, UUID requesterId, UUID userId) {
}
