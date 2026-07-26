package com.doodle.challenge.mapper;

import com.doodle.challenge.dto.MeetingResponse;
import com.doodle.challenge.entity.Meeting;
import org.springframework.stereotype.Component;

@Component
public class MeetingMapper {

    private final ParticipantMapper participantMapper;

    public MeetingMapper(ParticipantMapper participantMapper) {
        this.participantMapper = participantMapper;
    }

    public MeetingResponse toResponse(Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getOwnerId(),
                meeting.getTimeSlotId(),
                meeting.getRange().start(),
                meeting.getRange().end(),
                meeting.getParticipants().stream().map(participantMapper::toResponse).toList(),
                meeting.getCreatedAt(),
                meeting.getUpdatedAt());
    }
}
