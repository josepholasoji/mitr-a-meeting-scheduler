package com.doodle.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.doodle.challenge.WebSliceTestConfig;
import com.doodle.challenge.dto.MeetingResponse;
import com.doodle.challenge.dto.PageResponse;
import com.doodle.challenge.dto.ScheduleMeetingRequest;
import com.doodle.challenge.exception.*;
import com.doodle.challenge.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingController.class)
@ContextConfiguration(classes = WebSliceTestConfig.class)
@Import({MeetingController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MeetingService meetingService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID slotId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    void scheduleMeetingSucceeds() throws Exception {
        ScheduleMeetingRequest request = new ScheduleMeetingRequest(slotId, "Sync", "desc", List.of());
        UUID meetingId = UUID.randomUUID();
        when(meetingService.scheduleMeeting(any())).thenReturn(new MeetingResponse(
                meetingId, "Sync", "desc", ownerId, slotId, now, now.plus(1, ChronoUnit.HOURS), List.of(), now, now));

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(meetingId.toString()));
    }

    @Test
    void scheduleMeetingRejectedWhenSlotBusy() throws Exception {
        ScheduleMeetingRequest request = new ScheduleMeetingRequest(slotId, "Sync", null, List.of());
        when(meetingService.scheduleMeeting(any())).thenThrow(new SlotNotFreeException(slotId));

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void scheduleMeetingRejectedOnDuplicateParticipant() throws Exception {
        ScheduleMeetingRequest request = new ScheduleMeetingRequest(slotId, "Sync", null, List.of(ownerId));
        when(meetingService.scheduleMeeting(any())).thenThrow(new DuplicateParticipantException(UUID.randomUUID(), ownerId));

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void scheduleMeetingRejectedWhenSlotMissing() throws Exception {
        ScheduleMeetingRequest request = new ScheduleMeetingRequest(slotId, "Sync", null, List.of());
        when(meetingService.scheduleMeeting(any())).thenThrow(new TimeSlotNotFoundException(slotId));

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMeetingsForUserUsesDefaultPagingAndReturnsPageEnvelope() throws Exception {
        UUID userId = UUID.randomUUID();
        MeetingResponse meeting = new MeetingResponse(
                UUID.randomUUID(), "Sync", "desc", userId, slotId, now, now.plus(1, ChronoUnit.HOURS), List.of(), now, now);
        when(meetingService.getMeetingsForUser(eq(userId), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(meeting), 0, 20, 1, 1));

        mockMvc.perform(get("/users/{userId}/meetings", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(meeting.id().toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        Mockito.verify(meetingService).getMeetingsForUser(userId, PageRequest.of(0, 20));
    }

    @Test
    void cancelMeetingSucceeds() throws Exception {
        UUID meetingId = UUID.randomUUID();

        mockMvc.perform(delete("/meetings/{id}", meetingId).with(authentication(callerToken(ownerId))))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelMeetingRejectedForNonOwner() throws Exception {
        UUID meetingId = UUID.randomUUID();
        Mockito.doThrow(new MeetingAccessDeniedException(meetingId, ownerId))
                .when(meetingService)
                .cancelMeeting(any());

        mockMvc.perform(delete("/meetings/{id}", meetingId).with(authentication(callerToken(ownerId))))
                .andExpect(status().isForbidden());
    }

    private static UsernamePasswordAuthenticationToken callerToken(UUID callerId) {
        return new UsernamePasswordAuthenticationToken(callerId, null, List.of());
    }
}
