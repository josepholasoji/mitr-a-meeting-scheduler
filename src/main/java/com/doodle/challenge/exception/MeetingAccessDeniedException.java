package com.doodle.challenge.exception;

import java.util.UUID;

public class MeetingAccessDeniedException extends DomainException {

    public MeetingAccessDeniedException(UUID meetingId, UUID requesterId) {
        super("User " + requesterId + " is not permitted to modify meeting " + meetingId);
    }
}
