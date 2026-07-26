package com.doodle.challenge.entity;

import com.doodle.challenge.event.*;
import com.doodle.challenge.exception.DuplicateParticipantException;
import com.doodle.challenge.exception.ParticipantNotFoundException;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meetings")
public class Meeting extends BaseEntity<Meeting> {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "time_slot_id", nullable = false)
    private UUID timeSlotId;

    @Embedded
    private TimeRange range;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<Participant> participants = new ArrayList<>();

    protected Meeting() {
        // required by JPA
    }

    private Meeting(UUID id, UUID ownerId, UUID timeSlotId, TimeRange range, String title, String description) {
        super(id);
        this.ownerId = ownerId;
        this.timeSlotId = timeSlotId;
        this.range = range;
        this.title = requireTitle(title);
        this.description = description;
    }

    // doesn't touch the TimeSlot - MeetingScheduler marks it BUSY as part of the same use case
    public static Meeting schedule(UUID ownerId, UUID timeSlotId, TimeRange range, String title, String description) {
        Meeting meeting = new Meeting(UUID.randomUUID(), ownerId, timeSlotId, range, title, description);
        meeting.participants.add(Participant.owner(meeting, ownerId));
        meeting.registerEvent(new MeetingScheduled(meeting.getId(), timeSlotId, ownerId));
        return meeting;
    }

    public void updateDetails(String title, String description) {
        this.title = requireTitle(title);
        this.description = description;
        touch();
        registerEvent(new MeetingUpdated(getId()));
    }

    // only registers the event - caller does the actual delete + frees the slot
    public void cancel() {
        registerEvent(new MeetingCancelled(getId(), timeSlotId));
    }

    public Participant inviteParticipant(UUID userId) {
        if (hasParticipant(userId)) {
            throw new DuplicateParticipantException(getId(), userId);
        }
        Participant participant = Participant.invite(this, userId);
        participants.add(participant);
        touch();
        registerEvent(new ParticipantInvited(getId(), userId));
        return participant;
    }

    public void acceptInvitation(UUID userId) {
        findParticipant(userId).accept();
        touch();
        registerEvent(new ParticipantAcceptedInvitation(getId(), userId));
    }

    public void declineInvitation(UUID userId) {
        findParticipant(userId).decline();
        touch();
        registerEvent(new ParticipantDeclinedInvitation(getId(), userId));
    }

    public void removeParticipant(UUID userId) {
        Participant participant = findParticipant(userId);
        participants.remove(participant);
        touch();
    }

    private boolean hasParticipant(UUID userId) {
        return participants.stream().anyMatch(participant -> participant.getUserId().equals(userId));
    }

    private Participant findParticipant(UUID userId) {
        return participants.stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ParticipantNotFoundException(getId(), userId));
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Meeting title must not be blank");
        }
        return title;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getTimeSlotId() {
        return timeSlotId;
    }

    public TimeRange getRange() {
        return range;
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }
}
