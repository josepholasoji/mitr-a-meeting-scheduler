package com.doodle.challenge.repository;

import com.doodle.challenge.entity.TimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// findActiveOverlapping pushes the range-intersection filter to the DB (indexed) instead of loading the whole calendar
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    Page<TimeSlot> findByUserIdAndDeletedOnIsNullOrderByRangeStartAsc(UUID userId, Pageable pageable);

    @Query("""
            SELECT s FROM TimeSlot s
            WHERE s.userId = :userId
              AND s.deletedOn IS NULL
              AND s.range.start < :end
              AND s.range.end > :start
            ORDER BY s.range.start ASC
            """)
    List<TimeSlot> findActiveOverlapping(@Param("userId") UUID userId, @Param("start") Instant start, @Param("end") Instant end);
}
