package com.doodle.challenge.service;

import com.doodle.challenge.dto.*;
import com.doodle.challenge.entity.Meeting;
import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.entity.TimeSlot;
import com.doodle.challenge.entity.TimeSlotStatus;
import com.doodle.challenge.exception.*;
import com.doodle.challenge.mapper.TimeSlotMapper;
import com.doodle.challenge.repository.MeetingRepository;
import com.doodle.challenge.repository.TimeSlotRepository;
import com.doodle.challenge.repository.UserRepository;
import com.doodle.challenge.validator.SlotOverlapValidator;
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
class CalendarServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MeetingRepository meetingRepository;

    private final SlotOverlapValidator slotOverlapValidator = new SlotOverlapValidator();
    private final TimeSlotMapper timeSlotMapper = new TimeSlotMapper();

    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.now();

    private CalendarService service() {
        return new CalendarService(timeSlotRepository, userRepository, meetingRepository, slotOverlapValidator, timeSlotMapper);
    }

    @Test
    void createTimeSlotSavesWhenNoOverlap() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(timeSlotRepository.findActiveOverlapping(any(UUID.class), any(Instant.class), any(Instant.class))).thenReturn(List.of());
        CreateTimeSlotCommand command = new CreateTimeSlotCommand(userId, now, now.plus(1, ChronoUnit.HOURS));

        TimeSlotResponse response = service().createTimeSlot(command);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo(TimeSlotStatus.FREE);
        verify(timeSlotRepository).save(any(TimeSlot.class));
    }

    @Test
    void createTimeSlotRejectsWhenUserMissing() {
        when(userRepository.existsById(userId)).thenReturn(false);
        CreateTimeSlotCommand command = new CreateTimeSlotCommand(userId, now, now.plus(1, ChronoUnit.HOURS));

        assertThatThrownBy(() -> service().createTimeSlot(command)).isInstanceOf(UserNotFoundException.class);
        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void createTimeSlotRejectsOverlap() {
        when(userRepository.existsById(userId)).thenReturn(true);
        TimeSlot existing = TimeSlot.create(userId, TimeRange.of(now, now.plus(2, ChronoUnit.HOURS)));
        when(timeSlotRepository.findActiveOverlapping(any(UUID.class), any(Instant.class), any(Instant.class))).thenReturn(List.of(existing));
        CreateTimeSlotCommand command = new CreateTimeSlotCommand(userId, now.plus(1, ChronoUnit.HOURS), now.plus(3, ChronoUnit.HOURS));

        assertThatThrownBy(() -> service().createTimeSlot(command)).isInstanceOf(SlotOverlapException.class);
    }

    @Test
    void updateTimeSlotRejectedWhenBusy() {
        TimeSlot slot = TimeSlot.create(userId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        slot.markBusy();
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(timeSlotRepository.findActiveOverlapping(any(UUID.class), any(Instant.class), any(Instant.class))).thenReturn(List.of());
        UpdateTimeSlotCommand command = new UpdateTimeSlotCommand(slot.getId(), now.plus(2, ChronoUnit.HOURS), now.plus(3, ChronoUnit.HOURS));

        assertThatThrownBy(() -> service().updateTimeSlot(command)).isInstanceOf(SlotNotFreeException.class);
    }

    @Test
    void deleteTimeSlotRejectedWhenSlotHasMeeting() {
        TimeSlot slot = TimeSlot.create(userId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        slot.markBusy();
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> service().deleteTimeSlot(slot.getId())).isInstanceOf(SlotHasMeetingException.class);
    }

    @Test
    void deleteTimeSlotThrowsWhenMissing() {
        UUID missingId = UUID.randomUUID();
        when(timeSlotRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().deleteTimeSlot(missingId)).isInstanceOf(TimeSlotNotFoundException.class);
    }

    @Test
    void changeSlotStatusToFreeRejectedWhenMeetingExists() {
        TimeSlot slot = TimeSlot.create(userId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        slot.markBusy();
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        Meeting meeting = Meeting.schedule(userId, slot.getId(), slot.getRange(), "Sync", null);
        when(meetingRepository.findByTimeSlotId(slot.getId())).thenReturn(Optional.of(meeting));
        ChangeTimeSlotStatusCommand command = new ChangeTimeSlotStatusCommand(slot.getId(), TimeSlotStatus.FREE);

        assertThatThrownBy(() -> service().changeSlotStatus(command)).isInstanceOf(SlotHasMeetingException.class);
    }

    @Test
    void getTimeSlotsReturnsMappedPageInOrder() {
        when(userRepository.existsById(userId)).thenReturn(true);
        TimeSlot slot = TimeSlot.create(userId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<TimeSlot> page = new PageImpl<>(List.of(slot), pageRequest, 1);
        when(timeSlotRepository.findByUserIdAndDeletedOnIsNullOrderByRangeStartAsc(userId, pageRequest)).thenReturn(page);

        PageResponse<TimeSlotResponse> response = service().getTimeSlots(userId, pageRequest);

        assertThat(response.content()).extracting(TimeSlotResponse::id).containsExactly(slot.getId());
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
    }

    @Test
    void getTimeSlotsRejectsWhenUserMissing() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> service().getTimeSlots(userId, PageRequest.of(0, 20))).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void changeSlotStatusToBusyDirectlyBlocksSlot() {
        TimeSlot slot = TimeSlot.create(userId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        ChangeTimeSlotStatusCommand command = new ChangeTimeSlotStatusCommand(slot.getId(), TimeSlotStatus.BUSY);

        TimeSlotResponse response = service().changeSlotStatus(command);

        assertThat(response.status()).isEqualTo(TimeSlotStatus.BUSY);
    }
}
