package com.parkpulse.repository;

import com.parkpulse.model.WaitTimeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface WaitTimeHistoryRepository extends JpaRepository<WaitTimeHistory, Long> {

    /**
     * Checks if a snapshot for this attraction at this specific timestamp already exists.
     * Prevents duplicate rows when the source API has not updated its timestamp.
     */
    boolean existsByAttractionIdAndObservedAt(String attractionId, Instant observedAt);

    /**
     * Retrieves the chronological wait time history for a specific attraction.
     */
    List<WaitTimeHistory> findByAttractionIdOrderByObservedAtAsc(String attractionId);
}

