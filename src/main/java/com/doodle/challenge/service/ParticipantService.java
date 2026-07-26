package com.doodle.challenge.service;

import com.doodle.challenge.dto.InviteParticipantCommand;
import com.doodle.challenge.dto.ParticipantResponse;
import com.doodle.challenge.dto.RemoveParticipantCommand;
import com.doodle.challenge.dto.RespondToInvitationCommand;
import com.doodle.challenge.entity.Meeting;
import com.doodle.challenge.entity.Participant;
import com.doodle.challenge.entity.ParticipantStatus;
import com.doodle.challenge.exception.MeetingAccessDeniedException;
import com.doodle.challenge.exception.MeetingNotFoundException;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.mapper.ParticipantMapper;
import com.doodle.challenge.repository.MeetingRepository;
import com.doodle.challenge.repository.UserRepository;
import com.doodle.challenge.validator.MeetingPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ParticipantService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingPolicy meetingPolicy;
    private final ParticipantMapper participantMapper;

    public ParticipantService(
            MeetingRepository meetingRepository,
            UserRepository userRepository,
            MeetingPolicy meetingPolicy,
            ParticipantMapper participantMapper) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.meetingPolicy = meetingPolicy;
        this.participantMapper = participantMapper;
    }

    @Transactional
    public ParticipantResponse inviteParticipant(InviteParticipantCommand command) {
        Meeting meeting = requireMeeting(command.meetingId());
        meetingPolicy.ensureCanManageParticipants(meeting, command.requesterId());
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFoundException(command.userId());
        }

        Participant participant = meeting.inviteParticipant(command.userId());
        meetingRepository.save(meeting);
        return participantMapper.toResponse(participant);
    }

    @Transactional
    public void removeParticipant(RemoveParticipantCommand command) {
        Meeting meeting = requireMeeting(command.meetingId());
        meetingPolicy.ensureCanManageParticipants(meeting, command.requesterId());
        meeting.removeParticipant(command.userId());
        meetingRepository.save(meeting);
    }

    @Transactional
    public ParticipantResponse respondToInvitation(RespondToInvitationCommand command) {
        Meeting meeting = requireMeeting(command.meetingId());
        if (!command.userId().equals(command.requesterId())) {
            throw new MeetingAccessDeniedException(meeting.getId(), command.requesterId());
        }
        if (command.status() == ParticipantStatus.ACCEPTED) {
            meeting.acceptInvitation(command.userId());
        } else if (command.status() == ParticipantStatus.DECLINED) {
            meeting.declineInvitation(command.userId());
        } else {
            throw new IllegalArgumentException("Invitation response status must be ACCEPTED or DECLINED");
        }
        meetingRepository.save(meeting);

        return meeting.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(command.userId()))
                .findFirst()
                .map(participantMapper::toResponse)
                .orElseThrow();
    }

    private Meeting requireMeeting(UUID meetingId) {
        return meetingRepository.findById(meetingId).orElseThrow(() -> new MeetingNotFoundException(meetingId));
    }
}
