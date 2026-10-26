package com.parkpulse.service;

import com.parkpulse.dto.WaitTimeHistoryResponse;
import com.parkpulse.kafka.RideEvent;
import com.parkpulse.model.Attraction;
import com.parkpulse.model.WaitTimeHistory;
import com.parkpulse.repository.AttractionRepository;
import com.parkpulse.repository.WaitTimeHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RideHistoryService {

    private static final Logger log = LoggerFactory.getLogger(RideHistoryService.class);

    private final AttractionRepository attractionRepository;
    private final WaitTimeHistoryRepository historyRepository;

    public RideHistoryService(AttractionRepository attractionRepository, WaitTimeHistoryRepository historyRepository) {
        this.attractionRepository = attractionRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Ingests a RideEvent from Kafka into PostgreSQL:
     * 1. Finds or creates the Attraction row.
     * 2. Deduplicates against (attraction_id, observed_at).
     * 3. Saves a new record in wait_time_history if unique.
     */
    @Transactional
    public void recordRideUpdate(RideEvent event) {
        // 1. Ensure the Attraction exists
        Attraction attraction = attractionRepository.findById(event.rideId())
                .orElseGet(() -> {
                    log.info("[DB] Inserting new attraction: {} ({})", event.name(), event.rideId());
                    return attractionRepository.save(new Attraction(event.rideId(), event.name()));
                });

        // 2. Check for deduplication
        if (historyRepository.existsByAttractionIdAndObservedAt(event.rideId(), event.observedAt())) {
            log.debug("[DB] Duplicate snapshot skipped for {}: {}", event.name(), event.observedAt());
            return;
        }

        // 3. Save snapshot
        WaitTimeHistory history = new WaitTimeHistory(
                attraction,
                event.waitTime(),
                event.status(),
                event.observedAt()
        );

        historyRepository.save(history);
        log.info("[DB] Saved history: {} | {} min | {}", event.name(), event.waitTime(), event.observedAt());
    }

    /**
     * Retrieves chronological wait time observations for an attraction from PostgreSQL.
     * Returns empty list if no observations exist.
     */
    @Transactional(readOnly = true)
    public List<WaitTimeHistoryResponse> getRideHistory(String attractionId) {
        return historyRepository.findByAttractionIdOrderByObservedAtAsc(attractionId)
                .stream()
                .map(history -> new WaitTimeHistoryResponse(
                        history.getWaitTime(),
                        history.getStatus(),
                        history.getObservedAt()
                ))
                .toList();
    }
}
