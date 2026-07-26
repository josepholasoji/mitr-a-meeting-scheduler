package com.doodle.challenge.repository;

import com.doodle.challenge.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// findDistinctBy...: a single paginated DB query for "owns or is invited to" - in-memory dedup can't paginate
// correctly (LIMIT/OFFSET before dedup drops/duplicates rows near a page boundary)
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByOwnerId(UUID ownerId);

    Optional<Meeting> findByTimeSlotId(UUID timeSlotId);

    Page<Meeting> findDistinctByOwnerIdOrParticipantsUserIdOrderByRangeStartAsc(UUID ownerId, UUID userId, Pageable pageable);
}
