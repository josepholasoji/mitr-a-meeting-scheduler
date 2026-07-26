package com.doodle.challenge.repository;

import com.doodle.challenge.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// read-only: all mutations (invite/accept/decline/remove) go through the owning Meeting
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    List<Participant> findByUserId(UUID userId);
}
