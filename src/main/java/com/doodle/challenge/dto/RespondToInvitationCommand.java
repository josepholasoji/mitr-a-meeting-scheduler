package com.doodle.challenge.dto;

import com.doodle.challenge.entity.ParticipantStatus;
import java.util.UUID;

// requesterId must match userId - you can't accept/decline on someone else's behalf
public record RespondToInvitationCommand(UUID meetingId, UUID userId, UUID requesterId, ParticipantStatus status) {
}
