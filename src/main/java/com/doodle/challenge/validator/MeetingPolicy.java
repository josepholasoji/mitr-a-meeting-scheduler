package com.doodle.challenge.validator;

import com.doodle.challenge.entity.Meeting;
import com.doodle.challenge.exception.MeetingAccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MeetingPolicy {

    public boolean isOwner(Meeting meeting, UUID requesterId) {
        return meeting.getOwnerId().equals(requesterId);
    }

    public void ensureCanModify(Meeting meeting, UUID requesterId) {
        if (!isOwner(meeting, requesterId)) {
            throw new MeetingAccessDeniedException(meeting.getId(), requesterId);
        }
    }

    public void ensureCanCancel(Meeting meeting, UUID requesterId) {
        ensureCanModify(meeting, requesterId);
    }

    public void ensureCanManageParticipants(Meeting meeting, UUID requesterId) {
        ensureCanModify(meeting, requesterId);
    }
}
