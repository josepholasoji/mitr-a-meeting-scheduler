package com.doodle.challenge.dto;

import java.util.UUID;

public record UpdateMeetingCommand(UUID meetingId, UUID requesterId, String title, String description) {
}
