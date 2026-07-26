package com.doodle.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.doodle.challenge.WebSliceTestConfig;
import com.doodle.challenge.dto.InviteParticipantRequest;
import com.doodle.challenge.dto.ParticipantResponse;
import com.doodle.challenge.dto.RespondToInvitationRequest;
import com.doodle.challenge.entity.ParticipantRole;
import com.doodle.challenge.entity.ParticipantStatus;
import com.doodle.challenge.exception.DuplicateParticipantException;
import com.doodle.challenge.exception.GlobalExceptionHandler;
import com.doodle.challenge.service.ParticipantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipantController.class)
@ContextConfiguration(classes = WebSliceTestConfig.class)
@Import({ParticipantController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ParticipantService participantService;

    private final UUID meetingId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final UUID inviteeId = UUID.randomUUID();

    @Test
    void inviteParticipantSucceeds() throws Exception {
        InviteParticipantRequest request = new InviteParticipantRequest(inviteeId);
        when(participantService.inviteParticipant(any()))
                .thenReturn(new ParticipantResponse(inviteeId, ParticipantRole.PARTICIPANT, ParticipantStatus.INVITED, null));

        mockMvc.perform(post("/meetings/{meetingId}/participants", meetingId)
                        .with(authentication(callerToken(ownerId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(inviteeId.toString()))
                .andExpect(jsonPath("$.status").value("INVITED"));
    }

    @Test
    void inviteParticipantRejectsDuplicate() throws Exception {
        InviteParticipantRequest request = new InviteParticipantRequest(inviteeId);
        when(participantService.inviteParticipant(any())).thenThrow(new DuplicateParticipantException(meetingId, inviteeId));

        mockMvc.perform(post("/meetings/{meetingId}/participants", meetingId)
                        .with(authentication(callerToken(ownerId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void acceptInvitationSucceeds() throws Exception {
        RespondToInvitationRequest request = new RespondToInvitationRequest(ParticipantStatus.ACCEPTED);
        when(participantService.respondToInvitation(any())).thenReturn(
                new ParticipantResponse(inviteeId, ParticipantRole.PARTICIPANT, ParticipantStatus.ACCEPTED, Instant.now()));

        mockMvc.perform(patch("/meetings/{meetingId}/participants/{userId}/status", meetingId, inviteeId)
                        .with(authentication(callerToken(inviteeId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void declineInvitationSucceeds() throws Exception {
        RespondToInvitationRequest request = new RespondToInvitationRequest(ParticipantStatus.DECLINED);
        when(participantService.respondToInvitation(any())).thenReturn(
                new ParticipantResponse(inviteeId, ParticipantRole.PARTICIPANT, ParticipantStatus.DECLINED, Instant.now()));

        mockMvc.perform(patch("/meetings/{meetingId}/participants/{userId}/status", meetingId, inviteeId)
                        .with(authentication(callerToken(inviteeId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    private static UsernamePasswordAuthenticationToken callerToken(UUID callerId) {
        return new UsernamePasswordAuthenticationToken(callerId, null, List.of());
    }
}
