package com.doodle.challenge.mapper;

import com.doodle.challenge.dto.ParticipantResponse;
import com.doodle.challenge.entity.Participant;
import org.springframework.stereotype.Component;

@Component
public class ParticipantMapper {

    public ParticipantResponse toResponse(Participant participant) {
        return new ParticipantResponse(
                participant.getUserId(), participant.getRole(), participant.getStatus(), participant.getRespondedAt());
    }
}
