package com.doodle.challenge.controller;

import com.doodle.challenge.WebSliceTestConfig;
import com.doodle.challenge.dto.AvailabilityResponse;
import com.doodle.challenge.dto.TimeSlotResponse;
import com.doodle.challenge.entity.TimeSlotStatus;
import com.doodle.challenge.exception.GlobalExceptionHandler;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.service.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
@ContextConfiguration(classes = WebSliceTestConfig.class)
@Import({AvailabilityController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    void returnsFreeSlots() throws Exception {
        TimeSlotResponse free = new TimeSlotResponse(
                UUID.randomUUID(), userId, now, now.plus(1, ChronoUnit.HOURS), TimeSlotStatus.FREE, now, now);
        when(availabilityService.getAvailability(any()))
                .thenReturn(new AvailabilityResponse(userId, now, now.plus(2, ChronoUnit.HOURS), List.of(free), List.of()));

        mockMvc.perform(get("/users/{userId}/availability", userId)
                        .param("from", now.toString())
                        .param("to", now.plus(2, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSlots.length()").value(1))
                .andExpect(jsonPath("$.busySlots.length()").value(0));
    }

    @Test
    void returnsBusySlots() throws Exception {
        TimeSlotResponse busy = new TimeSlotResponse(
                UUID.randomUUID(), userId, now, now.plus(1, ChronoUnit.HOURS), TimeSlotStatus.BUSY, now, now);
        when(availabilityService.getAvailability(any()))
                .thenReturn(new AvailabilityResponse(userId, now, now.plus(2, ChronoUnit.HOURS), List.of(), List.of(busy)));

        mockMvc.perform(get("/users/{userId}/availability", userId)
                        .param("from", now.toString())
                        .param("to", now.plus(2, ChronoUnit.HOURS).toString())
                        .param("status", "BUSY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.busySlots.length()").value(1))
                .andExpect(jsonPath("$.freeSlots.length()").value(0));
    }

    @Test
    void returnsEmptyWhenNoSlotsInWindow() throws Exception {
        when(availabilityService.getAvailability(any()))
                .thenReturn(new AvailabilityResponse(userId, now, now.plus(1, ChronoUnit.HOURS), List.of(), List.of()));

        mockMvc.perform(get("/users/{userId}/availability", userId)
                        .param("from", now.toString())
                        .param("to", now.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSlots.length()").value(0))
                .andExpect(jsonPath("$.busySlots.length()").value(0));
    }

    @Test
    void rejectsMissingUser() throws Exception {
        when(availabilityService.getAvailability(any())).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/users/{userId}/availability", userId)
                        .param("from", now.toString())
                        .param("to", now.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isNotFound());
    }
}
