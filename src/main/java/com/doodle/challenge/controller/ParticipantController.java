package com.doodle.challenge.controller;

import com.doodle.challenge.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Participants")
public class ParticipantController {

    public ParticipantController() {
    }

    @PostMapping("/meetings/{meetingId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Invite a user to a meeting (owner only)")
    public ParticipantResponse inviteParticipant(
            @PathVariable UUID meetingId,
            @AuthenticationPrincipal UUID requesterId,
            @Valid @RequestBody InviteParticipantRequest request) {
        throw new NotImplementedException("Not implemented");    // Not implemented
    }

    @DeleteMapping("/meetings/{meetingId}/participants/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a participant from a meeting (owner only)")
    public void removeParticipant(
            @PathVariable UUID meetingId, @PathVariable UUID userId, @AuthenticationPrincipal UUID requesterId) {
         throw new NotImplementedException("Not implemented");    // Not implemented
   }

    @PatchMapping("/meetings/{meetingId}/participants/{userId}/status")
    @Operation(summary = "Accept or decline your own invitation")
    public ParticipantResponse respondToInvitation(
            @PathVariable UUID meetingId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UUID requesterId,
            @Valid @RequestBody RespondToInvitationRequest request) {
       throw new NotImplementedException("Not implemented");    // Not implemented
     }
}
