package com.doodle.challenge.service;

import com.doodle.challenge.dto.AvailabilityQuery;
import com.doodle.challenge.dto.AvailabilityResponse;
import com.doodle.challenge.dto.AvailabilitySummary;
import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.entity.TimeSlot;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.mapper.AvailabilityMapper;
import com.doodle.challenge.repository.TimeSlotRepository;
import com.doodle.challenge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final AvailabilityCalculator availabilityCalculator;
    private final AvailabilityMapper availabilityMapper;

    public AvailabilityService(
            TimeSlotRepository timeSlotRepository,
            UserRepository userRepository,
            AvailabilityCalculator availabilityCalculator,
            AvailabilityMapper availabilityMapper) {
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.availabilityCalculator = availabilityCalculator;
        this.availabilityMapper = availabilityMapper;
    }

    public AvailabilityResponse getAvailability(AvailabilityQuery query) {
        if (!userRepository.existsById(query.userId())) {
            throw new UserNotFoundException(query.userId());
        }
        TimeRange window = TimeRange.of(query.from(), query.to());
        List<TimeSlot> slots = timeSlotRepository.findActiveOverlapping(query.userId(), window.start(), window.end());
        AvailabilitySummary summary = availabilityCalculator.calculate(slots, window, query.status());
        return availabilityMapper.toResponse(query.userId(), summary);
    }
}
