package com.doodle.challenge.service;

import com.doodle.challenge.dto.AvailabilityQuery;
import com.doodle.challenge.dto.AvailabilityResponse;
import com.doodle.challenge.entity.TimeRange;
import com.doodle.challenge.entity.TimeSlot;
import com.doodle.challenge.entity.TimeSlotStatus;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.mapper.AvailabilityMapper;
import com.doodle.challenge.mapper.TimeSlotMapper;
import com.doodle.challenge.repository.TimeSlotRepository;
import com.doodle.challenge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private UserRepository userRepository;

    private final AvailabilityCalculator availabilityCalculator = new AvailabilityCalculator();
    private final AvailabilityMapper availabilityMapper = new AvailabilityMapper(new TimeSlotMapper());

    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.now();

    private AvailabilityService service() {
        return new AvailabilityService(timeSlotRepository, userRepository, availabilityCalculator, availabilityMapper);
    }

    @Test
    void returnsFreeAndBusySlotsWithinWindow() {
        when(userRepository.existsById(userId)).thenReturn(true);
        TimeSlot free = TimeSlot.create(userId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        TimeSlot busy = TimeSlot.create(userId, TimeRange.of(now.plus(1, ChronoUnit.HOURS), now.plus(2, ChronoUnit.HOURS)));
        busy.markBusy();
        when(timeSlotRepository.findActiveOverlapping(any(UUID.class), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(free, busy));
        AvailabilityQuery query = new AvailabilityQuery(userId, now, now.plus(2, ChronoUnit.HOURS), null);

        AvailabilityResponse response = service().getAvailability(query);

        assertThat(response.freeSlots()).extracting(slot -> slot.id()).containsExactly(free.getId());
        assertThat(response.busySlots()).extracting(slot -> slot.id()).containsExactly(busy.getId());
    }

    @Test
    void throwsWhenUserMissing() {
        when(userRepository.existsById(userId)).thenReturn(false);
        AvailabilityQuery query = new AvailabilityQuery(userId, now, now.plus(1, ChronoUnit.HOURS), null);

        assertThatThrownBy(() -> service().getAvailability(query)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void appliesStatusFilter() {
        when(userRepository.existsById(userId)).thenReturn(true);
        TimeSlot free = TimeSlot.create(userId, TimeRange.of(now, now.plus(1, ChronoUnit.HOURS)));
        when(timeSlotRepository.findActiveOverlapping(any(UUID.class), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(free));
        AvailabilityQuery query = new AvailabilityQuery(userId, now, now.plus(1, ChronoUnit.HOURS), TimeSlotStatus.BUSY);

        AvailabilityResponse response = service().getAvailability(query);

        assertThat(response.freeSlots()).isEmpty();
        assertThat(response.busySlots()).isEmpty();
    }
}
