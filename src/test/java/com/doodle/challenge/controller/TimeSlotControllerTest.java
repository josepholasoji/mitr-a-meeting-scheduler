package com.doodle.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.doodle.challenge.WebSliceTestConfig;
import com.doodle.challenge.dto.CreateTimeSlotRequest;
import com.doodle.challenge.dto.PageResponse;
import com.doodle.challenge.dto.TimeSlotResponse;
import com.doodle.challenge.entity.TimeSlotStatus;
import com.doodle.challenge.exception.*;
import com.doodle.challenge.service.CalendarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimeSlotController.class)
@ContextConfiguration(classes = WebSliceTestConfig.class)
@Import({TimeSlotController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class TimeSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CalendarService calendarService;

    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    void createTimeSlotSucceeds() throws Exception {
        CreateTimeSlotRequest request = new CreateTimeSlotRequest(now, now.plus(1, ChronoUnit.HOURS));
        UUID slotId = UUID.randomUUID();
        when(calendarService.createTimeSlot(any())).thenReturn(new TimeSlotResponse(
                slotId, userId, now, now.plus(1, ChronoUnit.HOURS), TimeSlotStatus.FREE, now, now));

        mockMvc.perform(post("/users/{userId}/timeslots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(slotId.toString()))
                .andExpect(jsonPath("$.status").value("FREE"));
    }

    @Test
    void createTimeSlotRejectsOverlap() throws Exception {
        CreateTimeSlotRequest request = new CreateTimeSlotRequest(now, now.plus(1, ChronoUnit.HOURS));
        when(calendarService.createTimeSlot(any())).thenThrow(new SlotOverlapException(userId, null));

        mockMvc.perform(post("/users/{userId}/timeslots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createTimeSlotRejectsMissingRequiredField() throws Exception {
        String malformedJson = "{\"startTime\": null, \"endTime\": \"" + now.plus(1, ChronoUnit.HOURS) + "\"}";

        mockMvc.perform(post("/users/{userId}/timeslots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTimeSlotRejectsInvalidRangeFromDomain() throws Exception {
        CreateTimeSlotRequest request = new CreateTimeSlotRequest(now, now.plus(1, ChronoUnit.HOURS));
        when(calendarService.createTimeSlot(any())).thenThrow(new InvalidTimeRangeException("Start time must be before end time"));

        mockMvc.perform(post("/users/{userId}/timeslots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("before end time")));
    }

    @Test
    void createTimeSlotRejectsMissingUser() throws Exception {
        CreateTimeSlotRequest request = new CreateTimeSlotRequest(now, now.plus(1, ChronoUnit.HOURS));
        when(calendarService.createTimeSlot(any())).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(post("/users/{userId}/timeslots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTimeSlotsUsesDefaultPagingAndReturnsPageEnvelope() throws Exception {
        TimeSlotResponse slot = new TimeSlotResponse(
                UUID.randomUUID(), userId, now, now.plus(1, ChronoUnit.HOURS), TimeSlotStatus.FREE, now, now);
        when(calendarService.getTimeSlots(eq(userId), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(slot), 0, 20, 1, 1));

        mockMvc.perform(get("/users/{userId}/timeslots", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(slot.id().toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));

        org.mockito.Mockito.verify(calendarService).getTimeSlots(userId, PageRequest.of(0, 20));
    }

    @Test
    void getTimeSlotsClampsSizeToTheConfiguredMaximum() throws Exception {
        when(calendarService.getTimeSlots(eq(userId), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 2, 100, 0, 0));

        mockMvc.perform(get("/users/{userId}/timeslots", userId).param("page", "2").param("size", "500"))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(calendarService).getTimeSlots(userId, PageRequest.of(2, 100));
    }

    @Test
    void deleteTimeSlotSucceeds() throws Exception {
        UUID slotId = UUID.randomUUID();

        mockMvc.perform(delete("/timeslots/{id}", slotId)).andExpect(status().isNoContent());
    }

    @Test
    void deleteTimeSlotRejectedWhenSlotHasMeeting() throws Exception {
        UUID slotId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new SlotHasMeetingException(slotId)).when(calendarService).deleteTimeSlot(slotId);

        mockMvc.perform(delete("/timeslots/{id}", slotId)).andExpect(status().isConflict());
    }

    @Test
    void deleteTimeSlotRejectedWhenMissing() throws Exception {
        UUID slotId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new TimeSlotNotFoundException(slotId)).when(calendarService).deleteTimeSlot(slotId);

        mockMvc.perform(delete("/timeslots/{id}", slotId)).andExpect(status().isNotFound());
    }

    @Test
    void changeStatusRejectsMissingBody() throws Exception {
        UUID slotId = UUID.randomUUID();

        mockMvc.perform(patch("/timeslots/{id}/status", slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
