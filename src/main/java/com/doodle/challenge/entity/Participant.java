package com.doodle.challenge.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

// child entity of the Meeting aggregate - factory methods/mutators are package-private so only Meeting can call them
@Entity
@Table(name = "participants", uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "user_id"}))
public class Participant {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantStatus status;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Participant() {
        // required by JPA
    }

    private Participant(UUID id, Meeting meeting, UUID userId, ParticipantRole role, ParticipantStatus status) {
        this.id = id;
        this.meeting = meeting;
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.createdAt = Instant.now();
    }

    static Participant owner(Meeting meeting, UUID userId) {
        Participant participant = new Participant(
                UUID.randomUUID(), meeting, userId, ParticipantRole.OWNER, ParticipantStatus.ACCEPTED);
        participant.respondedAt = participant.createdAt;
        return participant;
    }

    static Participant invite(Meeting meeting, UUID userId) {
        return new Participant(UUID.randomUUID(), meeting, userId, ParticipantRole.PARTICIPANT, ParticipantStatus.INVITED);
    }

    void accept() {
        this.status = ParticipantStatus.ACCEPTED;
        this.respondedAt = Instant.now();
    }

    void decline() {
        this.status = ParticipantStatus.DECLINED;
        this.respondedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public UUID getUserId() {
        return userId;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public ParticipantStatus getStatus() {
        return status;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Participant other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
