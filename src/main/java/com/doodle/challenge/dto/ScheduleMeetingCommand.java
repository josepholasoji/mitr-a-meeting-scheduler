package com.doodle.challenge.dto;

import java.util.List;
import java.util.UUID;

// no separate ownerId - the owner is always the TimeSlot's owner, derived rather than trusted as caller input
public record ScheduleMeetingCommand(UUID timeSlotId, String title, String description, List<UUID> participantIds) {
}
