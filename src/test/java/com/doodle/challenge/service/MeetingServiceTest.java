package com.doodle.challenge.service;

import com.doodle.challenge.dto.*;
import com.doodle.challenge.entity.Meeting;
import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.entity.TimeSlot;
import com.doodle.challenge.entity.TimeSlotStatus;
import com.doodle.challenge.exception.*;
import com.doodle.challenge.mapper.MeetingMapper;
import com.doodle.challenge.mapper.ParticipantMapper;
import com.doodle.challenge.repository.MeetingRepository;
import com.doodle.challenge.repository.TimeSlotRepository;
import com.doodle.challenge.repository.UserRepository;
import com.doodle.challenge.validator.MeetingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    private final MeetingScheduler meetingScheduler = new MeetingScheduler();
    private final MeetingPolicy meetingPolicy = new MeetingPolicy();
    private final MeetingMapper meetingMapper = new MeetingMapper(new ParticipantMapper());

    private final UUID ownerId = UUID.randomUUID();
    private final Instant now = Instant.now();

    private MeetingService service() {
        return new MeetingService(timeSlotRepository, meetingRepository, userRepository, meetingScheduler, meetingPolicy, meetingMapper);
    }

    @Test
    void scheduleMeetingMarksSlotBusyAndInvitesParticipants() {
        TimeSlot slot = TimeSlot.create(ownerId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        UUID inviteeId = UUID.randomUUID();
        when(userRepository.existsById(inviteeId)).thenReturn(true);
        ScheduleMeetingCommand command = new ScheduleMeetingCommand(slot.getId(), "Planning", "Q3", List.of(inviteeId));

        MeetingResponse response = service().scheduleMeeting(command);

        assertThat(slot.getStatus()).isEqualTo(TimeSlotStatus.BUSY);
        assertThat(response.participants()).hasSize(2);
        verify(timeSlotRepository).save(slot);
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void scheduleMeetingThrowsWhenSlotMissing() {
        UUID missingSlotId = UUID.randomUUID();
        when(timeSlotRepository.findById(missingSlotId)).thenReturn(Optional.empty());
        ScheduleMeetingCommand command = new ScheduleMeetingCommand(missingSlotId, "Planning", null, List.of());

        assertThatThrownBy(() -> service().scheduleMeeting(command)).isInstanceOf(TimeSlotNotFoundException.class);
    }

    @Test
    void scheduleMeetingThrowsWhenSlotAlreadyBusy() {
        TimeSlot slot = TimeSlot.create(ownerId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        slot.markBusy();
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        ScheduleMeetingCommand command = new ScheduleMeetingCommand(slot.getId(), "Planning", null, List.of());

        assertThatThrownBy(() -> service().scheduleMeeting(command)).isInstanceOf(SlotNotFreeException.class);
    }

    @Test
    void scheduleMeetingThrowsWhenInviteeMissing() {
        TimeSlot slot = TimeSlot.create(ownerId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        UUID missingInvitee = UUID.randomUUID();
        when(userRepository.existsById(missingInvitee)).thenReturn(false);
        ScheduleMeetingCommand command = new ScheduleMeetingCommand(slot.getId(), "Planning", null, List.of(missingInvitee));

        assertThatThrownBy(() -> service().scheduleMeeting(command)).isInstanceOf(UserNotFoundException.class);
        verify(meetingRepository, never()).save(any());
    }

    @Test
    void getMeetingThrowsWhenMissing() {
        UUID missingId = UUID.randomUUID();
        when(meetingRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getMeeting(missingId)).isInstanceOf(MeetingNotFoundException.class);
    }

    @Test
    void getMeetingsForUserReturnsMappedPageFromTheCombinedOwnerOrParticipantQuery() {
        when(userRepository.existsById(ownerId)).thenReturn(true);
        Meeting owned = Meeting.schedule(ownerId, UUID.randomUUID(), TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)), "Owned", null);
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Meeting> page = new PageImpl<>(List.of(owned), pageRequest, 1);
        when(meetingRepository.findDistinctByOwnerIdOrParticipantsUserIdOrderByRangeStartAsc(ownerId, ownerId, pageRequest))
                .thenReturn(page);

        PageResponse<MeetingResponse> meetings = service().getMeetingsForUser(ownerId, pageRequest);

        assertThat(meetings.content()).extracting(MeetingResponse::id).containsExactly(owned.getId());
        assertThat(meetings.totalElements()).isEqualTo(1);
    }

    @Test
    void getMeetingsForUserRejectsWhenUserMissing() {
        when(userRepository.existsById(ownerId)).thenReturn(false);

        assertThatThrownBy(() -> service().getMeetingsForUser(ownerId, PageRequest.of(0, 20))).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateMeetingRejectedForNonOwner() {
        Meeting meeting = Meeting.schedule(ownerId, UUID.randomUUID(), TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)), "Sync", null);
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        UpdateMeetingCommand command = new UpdateMeetingCommand(meeting.getId(), UUID.randomUUID(), "New title", null);

        assertThatThrownBy(() -> service().updateMeeting(command)).isInstanceOf(MeetingAccessDeniedException.class);
    }

    @Test
    void cancelMeetingFreesSlotAndDeletesMeeting() {
        TimeSlot slot = TimeSlot.create(ownerId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        slot.markBusy();
        Meeting meeting = Meeting.schedule(ownerId, slot.getId(), slot.getRange(), "Sync", null);
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        CancelMeetingCommand command = new CancelMeetingCommand(meeting.getId(), ownerId);

        service().cancelMeeting(command);

        assertThat(slot.getStatus()).isEqualTo(TimeSlotStatus.FREE);
        verify(timeSlotRepository).save(slot);
        verify(meetingRepository).delete(meeting);
    }

    @Test
    void cancelMeetingRejectedForNonOwner() {
        Meeting meeting = Meeting.schedule(ownerId, UUID.randomUUID(), TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)), "Sync", null);
        when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
        CancelMeetingCommand command = new CancelMeetingCommand(meeting.getId(), UUID.randomUUID());

        assertThatThrownBy(() -> service().cancelMeeting(command)).isInstanceOf(MeetingAccessDeniedException.class);
        verify(meetingRepository, never()).delete(any(Meeting.class));
    }
}
