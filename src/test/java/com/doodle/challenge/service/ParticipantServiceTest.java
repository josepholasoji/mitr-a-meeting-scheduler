package com.doodle.challenge.service;

import com.doodle.challenge.dto.InviteParticipantCommand;
import com.doodle.challenge.dto.ParticipantResponse;
import com.doodle.challenge.dto.RemoveParticipantCommand;
import com.doodle.challenge.dto.RespondToInvitationCommand;
import com.doodle.challenge.entity.Meeting;
import com.doodle.challenge.entity.ParticipantStatus;
import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.exception.DuplicateParticipantException;
import com.doodle.challenge.exception.MeetingAccessDeniedException;
import com.doodle.challenge.exception.ParticipantNotFoundException;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.mapper.ParticipantMapper;
import com.doodle.challenge.repository.MeetingRepository;
import com.doodle.challenge.repository.UserRepository;
import com.doodle.challenge.validator.MeetingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    private final MeetingPolicy meetingPolicy = new MeetingPolicy();
    private final ParticipantMapper participantMapper = new ParticipantMapper();

    private final UUID ownerId = UUID.randomUUID();
    private final Instant now = Instant.now();

    private ParticipantService service() {
        return new ParticipantService(meetingRepository, userRepository, meetingPolicy, participantMapper);
    }

    private Meeting newMeeting() {
        return Meeting.schedule(ownerId, UUID.randomUUID(), TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)), "Sync", null);
    }

    @Test
    void ownerCanInviteParticipant() {
        Meeting meeting = newMeeting();
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        UUID inviteeId = UUID.randomUUID();
        when(userRepository.existsById(inviteeId)).thenReturn(true);
        InviteParticipantCommand command = new InviteParticipantCommand(meeting.getId(), ownerId, inviteeId);

        ParticipantResponse response = service().inviteParticipant(command);

        assertThat(response.userId()).isEqualTo(inviteeId);
        assertThat(response.status()).isEqualTo(ParticipantStatus.INVITED);
    }

    @Test
    void nonOwnerCannotInviteParticipant() {
        Meeting meeting = newMeeting();
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        InviteParticipantCommand command = new InviteParticipantCommand(meeting.getId(), UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> service().inviteParticipant(command)).isInstanceOf(MeetingAccessDeniedException.class);
    }

    @Test
    void invitingMissingUserThrows() {
        Meeting meeting = newMeeting();
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        UUID missingUser = UUID.randomUUID();
        when(userRepository.existsById(missingUser)).thenReturn(false);
        InviteParticipantCommand command = new InviteParticipantCommand(meeting.getId(), ownerId, missingUser);

        assertThatThrownBy(() -> service().inviteParticipant(command)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void invitingDuplicateParticipantThrows() {
        Meeting meeting = newMeeting();
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        when(userRepository.existsById(ownerId)).thenReturn(true);
        InviteParticipantCommand command = new InviteParticipantCommand(meeting.getId(), ownerId, ownerId);

        assertThatThrownBy(() -> service().inviteParticipant(command)).isInstanceOf(DuplicateParticipantException.class);
    }

    @Test
    void nonOwnerCannotRemoveParticipant() {
        Meeting meeting = newMeeting();
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        RemoveParticipantCommand command = new RemoveParticipantCommand(meeting.getId(), UUID.randomUUID(), ownerId);

        assertThatThrownBy(() -> service().removeParticipant(command)).isInstanceOf(MeetingAccessDeniedException.class);
    }

    @Test
    void participantCanAcceptTheirOwnInvitation() {
        Meeting meeting = newMeeting();
        UUID inviteeId = UUID.randomUUID();
        meeting.inviteParticipant(inviteeId);
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        RespondToInvitationCommand command =
                new RespondToInvitationCommand(meeting.getId(), inviteeId, inviteeId, ParticipantStatus.ACCEPTED);

        ParticipantResponse response = service().respondToInvitation(command);

        assertThat(response.status()).isEqualTo(ParticipantStatus.ACCEPTED);
    }

    @Test
    void respondingOnBehalfOfSomeoneElseIsRejected() {
        Meeting meeting = newMeeting();
        UUID inviteeId = UUID.randomUUID();
        meeting.inviteParticipant(inviteeId);
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        RespondToInvitationCommand command =
                new RespondToInvitationCommand(meeting.getId(), inviteeId, UUID.randomUUID(), ParticipantStatus.ACCEPTED);

        assertThatThrownBy(() -> service().respondToInvitation(command)).isInstanceOf(MeetingAccessDeniedException.class);
    }

    @Test
    void respondingWithInvitedStatusIsRejected() {
        Meeting meeting = newMeeting();
        UUID inviteeId = UUID.randomUUID();
        meeting.inviteParticipant(inviteeId);
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        RespondToInvitationCommand command =
                new RespondToInvitationCommand(meeting.getId(), inviteeId, inviteeId, ParticipantStatus.INVITED);

        assertThatThrownBy(() -> service().respondToInvitation(command)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void respondingForUnknownParticipantThrows() {
        Meeting meeting = newMeeting();
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        UUID strangerId = UUID.randomUUID();
        RespondToInvitationCommand command =
                new RespondToInvitationCommand(meeting.getId(), strangerId, strangerId, ParticipantStatus.ACCEPTED);

        assertThatThrownBy(() -> service().respondToInvitation(command)).isInstanceOf(ParticipantNotFoundException.class);
    }
}
