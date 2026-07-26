package com.doodle.challenge.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.doodle.challenge.DoodleApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// end-to-end: full HTTP -> service -> domain -> Postgres round trips, no mocked layers
@SpringBootTest(classes = DoodleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class BookingFlowIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PASSWORD = "correct-horse-battery-staple";

    private JsonNode register(String email) throws Exception {
        String userBody = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Test User", "email", email, "password", PASSWORD))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(userBody);
    }

    @Test
    void fullLifecycle_createSlot_scheduleMeeting_acceptInvite_cancelMeeting_freesSlot() throws Exception {
        String ownerEmail = "owner-" + UUID.randomUUID() + "@example.com";
        String ownerId = register(ownerEmail).get("id").asText();

        String inviteeEmail = "invitee-" + UUID.randomUUID() + "@example.com";
        String inviteeId = register(inviteeEmail).get("id").asText();

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);

        String slotBody = mockMvc.perform(post("/users/{userId}/timeslots", ownerId)
                        .with(httpBasic(ownerEmail, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("startTime", start.toString(), "endTime", end.toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FREE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String slotId = objectMapper.readTree(slotBody).get("id").asText();

        mockMvc.perform(get("/users/{userId}/availability", ownerId)
                        .with(httpBasic(ownerEmail, PASSWORD))
                        .param("from", start.minus(1, ChronoUnit.HOURS).toString())
                        .param("to", end.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSlots[0].id").value(slotId))
                .andExpect(jsonPath("$.busySlots").isEmpty());

        String meetingBody = mockMvc.perform(post("/meetings")
                        .with(httpBasic(ownerEmail, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("timeSlotId", slotId, "title", "Sprint planning", "participantIds", List.of(inviteeId)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participants.length()").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String meetingId = objectMapper.readTree(meetingBody).get("id").asText();

        mockMvc.perform(get("/users/{userId}/availability", ownerId)
                        .with(httpBasic(ownerEmail, PASSWORD))
                        .param("from", start.minus(1, ChronoUnit.HOURS).toString())
                        .param("to", end.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSlots").isEmpty())
                .andExpect(jsonPath("$.busySlots[0].id").value(slotId));

        mockMvc.perform(delete("/timeslots/{id}", slotId).with(httpBasic(ownerEmail, PASSWORD)))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/meetings/{meetingId}/participants/{userId}/status", meetingId, inviteeId)
                        .with(httpBasic(inviteeEmail, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACCEPTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(get("/users/{userId}/meetings", inviteeId).with(httpBasic(inviteeEmail, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(meetingId))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(delete("/meetings/{id}", meetingId).with(httpBasic(inviteeEmail, PASSWORD)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/meetings/{id}", meetingId).with(httpBasic(ownerEmail, PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{userId}/availability", ownerId)
                        .with(httpBasic(ownerEmail, PASSWORD))
                        .param("from", start.minus(1, ChronoUnit.HOURS).toString())
                        .param("to", end.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSlots[0].id").value(slotId))
                .andExpect(jsonPath("$.busySlots").isEmpty());

        mockMvc.perform(delete("/timeslots/{id}", slotId).with(httpBasic(ownerEmail, PASSWORD)))
                .andExpect(status().isNoContent());
    }

    @Test
    void overlappingSlotIsRejectedEndToEnd() throws Exception {
        String ownerEmail = "overlap-" + UUID.randomUUID() + "@example.com";
        String ownerId = register(ownerEmail).get("id").asText();

        Instant start = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        mockMvc.perform(post("/users/{userId}/timeslots", ownerId)
                        .with(httpBasic(ownerEmail, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("startTime", start.toString(), "endTime", end.toString()))))
                .andExpect(status().isCreated());

        Instant overlappingStart = start.plus(30, ChronoUnit.MINUTES);
        Instant overlappingEnd = end.plus(30, ChronoUnit.MINUTES);

        mockMvc.perform(post("/users/{userId}/timeslots", ownerId)
                        .with(httpBasic(ownerEmail, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("startTime", overlappingStart.toString(), "endTime", overlappingEnd.toString()))))
                .andExpect(status().isConflict());
    }
}
