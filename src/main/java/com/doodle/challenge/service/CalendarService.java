package com.doodle.challenge.service;

import com.doodle.challenge.dto.*;
import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.entity.TimeSlot;
import com.doodle.challenge.entity.TimeSlotStatus;
import com.doodle.challenge.exception.SlotHasMeetingException;
import com.doodle.challenge.exception.TimeSlotNotFoundException;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.mapper.TimeSlotMapper;
import com.doodle.challenge.repository.MeetingRepository;
import com.doodle.challenge.repository.TimeSlotRepository;
import com.doodle.challenge.repository.UserRepository;
import com.doodle.challenge.validator.SlotOverlapValidator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final SlotOverlapValidator slotOverlapValidator;
    private final TimeSlotMapper timeSlotMapper;

    public CalendarService(
            TimeSlotRepository timeSlotRepository,
            UserRepository userRepository,
            MeetingRepository meetingRepository,
            SlotOverlapValidator slotOverlapValidator,
            TimeSlotMapper timeSlotMapper) {
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.meetingRepository = meetingRepository;
        this.slotOverlapValidator = slotOverlapValidator;
        this.timeSlotMapper = timeSlotMapper;
    }

    @Transactional
    public TimeSlotResponse createTimeSlot(CreateTimeSlotCommand command) {
        requireUserExists(command.userId());
        TimeRange range = TimeRange.of(command.startTime(), command.endTime());

        List<TimeSlot> candidates = timeSlotRepository.findActiveOverlapping(command.userId(), range.start(), range.end());
        slotOverlapValidator.validate(command.userId(), range, candidates);

        TimeSlot slot = TimeSlot.create(command.userId(), range);
        timeSlotRepository.save(slot);
        return timeSlotMapper.toResponse(slot);
    }

    public PageResponse<TimeSlotResponse> getTimeSlots(UUID userId, Pageable pageable) {
        requireUserExists(userId);
        return PageResponse.from(
                timeSlotRepository.findByUserIdAndDeletedOnIsNullOrderByRangeStartAsc(userId, pageable)
                        .map(timeSlotMapper::toResponse));
    }

    @Transactional
    public TimeSlotResponse updateTimeSlot(UpdateTimeSlotCommand command) {
        TimeSlot slot = requireSlot(command.slotId());
        TimeRange range = TimeRange.of(command.startTime(), command.endTime());

        List<TimeSlot> candidates = timeSlotRepository.findActiveOverlapping(slot.getUserId(), range.start(), range.end())
                .stream()
                .filter(candidate -> !candidate.getId().equals(slot.getId()))
                .toList();
        slotOverlapValidator.validate(slot.getUserId(), range, candidates);

        slot.reschedule(range);
        timeSlotRepository.save(slot);
        return timeSlotMapper.toResponse(slot);
    }

    @Transactional
    public void deleteTimeSlot(UUID slotId) {
        TimeSlot slot = requireSlot(slotId);
        slot.delete();
        timeSlotRepository.save(slot);
    }

    @Transactional
    public TimeSlotResponse changeSlotStatus(ChangeTimeSlotStatusCommand command) {
        TimeSlot slot = requireSlot(command.slotId());
        if (command.status() == TimeSlotStatus.FREE) {
            if (meetingRepository.findByTimeSlotId(slot.getId()).isPresent()) {
                throw new SlotHasMeetingException(slot.getId());
            }
            slot.markFree();
        } else {
            slot.markBusy();
        }
        timeSlotRepository.save(slot);
        return timeSlotMapper.toResponse(slot);
    }

    private TimeSlot requireSlot(UUID slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId).orElseThrow(() -> new TimeSlotNotFoundException(slotId));
        if (slot.isDeleted()) {
            throw new TimeSlotNotFoundException(slotId);
        }
        return slot;
    }

    private void requireUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }
}
