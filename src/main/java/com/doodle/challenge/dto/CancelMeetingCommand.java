package com.doodle.challenge.dto;

import java.util.UUID;

public record CancelMeetingCommand(UUID meetingId, UUID requesterId) {
}
