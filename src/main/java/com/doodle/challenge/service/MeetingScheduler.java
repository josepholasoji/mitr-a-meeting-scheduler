package com.doodle.challenge.service;

import com.doodle.challenge.entity.Meeting;
import com.doodle.challenge.entity.TimeSlot;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MeetingScheduler {

    public Meeting schedule(TimeSlot slot, UUID ownerId, String title, String description) {
        slot.markBusy();
        return Meeting.schedule(ownerId, slot.getId(), slot.getRange(), title, description);
    }

    public void cancel(Meeting meeting, TimeSlot slot) {
        meeting.cancel();
        slot.markFree();
    }
}
