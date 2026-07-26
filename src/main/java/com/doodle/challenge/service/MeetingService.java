package com.doodle.challenge.service;

import com.doodle.challenge.dto.*;
import com.doodle.challenge.entity.Meeting;
import com.doodle.challenge.entity.TimeSlot;
import com.doodle.challenge.exception.MeetingNotFoundException;
import com.doodle.challenge.exception.TimeSlotNotFoundException;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.mapper.MeetingMapper;
import com.doodle.challenge.repository.MeetingRepository;
import com.doodle.challenge.repository.TimeSlotRepository;
import com.doodle.challenge.repository.UserRepository;
import com.doodle.challenge.validator.MeetingPolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MeetingService {

    private final TimeSlotRepository timeSlotRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingScheduler meetingScheduler;
    private final MeetingPolicy meetingPolicy;
    private final MeetingMapper meetingMapper;

    public MeetingService(
            TimeSlotRepository timeSlotRepository,
            MeetingRepository meetingRepository,
            UserRepository userRepository,
            MeetingScheduler meetingScheduler,
            MeetingPolicy meetingPolicy,
            MeetingMapper meetingMapper) {
        this.timeSlotRepository = timeSlotRepository;
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.meetingScheduler = meetingScheduler;
        this.meetingPolicy = meetingPolicy;
        this.meetingMapper = meetingMapper;
    }

    @Transactional
    public MeetingResponse scheduleMeeting(ScheduleMeetingCommand command) {
        TimeSlot slot = timeSlotRepository.findById(command.timeSlotId())
                .orElseThrow(() -> new TimeSlotNotFoundException(command.timeSlotId()));

        Meeting meeting = meetingScheduler.schedule(slot, slot.getUserId(), command.title(), command.description());

        List<UUID> invitees = command.participantIds() == null ? List.of() : command.participantIds();
        for (UUID inviteeId : invitees) {
            if (!userRepository.existsById(inviteeId)) {
                throw new UserNotFoundException(inviteeId);
            }
            meeting.inviteParticipant(inviteeId);
        }

        timeSlotRepository.save(slot);
        meetingRepository.save(meeting);
        return meetingMapper.toResponse(meeting);
    }

    public MeetingResponse getMeeting(UUID meetingId) {
        return meetingMapper.toResponse(requireMeeting(meetingId));
    }

    public PageResponse<MeetingResponse> getMeetingsForUser(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return PageResponse.from(
                meetingRepository.findDistinctByOwnerIdOrParticipantsUserIdOrderByRangeStartAsc(userId, userId, pageable)
                        .map(meetingMapper::toResponse));
    }

    @Transactional
    public MeetingResponse updateMeeting(UpdateMeetingCommand command) {
        Meeting meeting = requireMeeting(command.meetingId());
        meetingPolicy.ensureCanModify(meeting, command.requesterId());
        meeting.updateDetails(command.title(), command.description());
        meetingRepository.save(meeting);
        return meetingMapper.toResponse(meeting);
    }

    @Transactional
    public void cancelMeeting(CancelMeetingCommand command) {
        Meeting meeting = requireMeeting(command.meetingId());
        meetingPolicy.ensureCanCancel(meeting, command.requesterId());
        TimeSlot slot = timeSlotRepository.findById(meeting.getTimeSlotId())
                .orElseThrow(() -> new TimeSlotNotFoundException(meeting.getTimeSlotId()));

        meetingScheduler.cancel(meeting, slot);

        timeSlotRepository.save(slot);
        meetingRepository.delete(meeting);
    }

    private Meeting requireMeeting(UUID meetingId) {
        return meetingRepository.findById(meetingId).orElseThrow(() -> new MeetingNotFoundException(meetingId));
    }
}
