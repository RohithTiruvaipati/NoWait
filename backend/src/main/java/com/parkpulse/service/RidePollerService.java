package com.parkpulse.service;

import tools.jackson.databind.ObjectMapper;
import com.parkpulse.dto.RideResponse;
import com.parkpulse.kafka.RideEvent;
import com.parkpulse.kafka.RideEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RidePollerService {

    private static final Logger log = LoggerFactory.getLogger(RidePollerService.class);

    private final ThemeParkService themeParkService;
    private final RideEventProducer rideEventProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RidePollerService(ThemeParkService themeParkService, RideEventProducer rideEventProducer) {
        this.themeParkService = themeParkService;
        this.rideEventProducer = rideEventProducer;
    }

    /**
     * Polls ThemeParks.wiki every 1 minute (60,000 ms).
     * initialDelay = 5,000 ms allows the application to finish starting up.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void pollAndPublish() {
        log.info("[POLLER] Fetching Magic Kingdom live data from ThemeParks.wiki...");
        try {
            List<RideResponse> rides = themeParkService.getRides();
            log.info("[POLLER] Retrieved {} attractions. Publishing to Kafka...", rides.size());

            for (RideResponse ride : rides) {
                // Parse source lastupdate timestamp into Instant for accurate deduplication
                Instant observedAt;
                try {
                    observedAt = (ride.lastupdate() != null && !ride.lastupdate().isBlank())
                            ? Instant.parse(ride.lastupdate())
                            : Instant.now();
                } catch (Exception ex) {
                    observedAt = Instant.now();
                }

                RideEvent event = new RideEvent(
                    ride.id(),
                    ride.name(),
                    ride.waitTime(),
                    ride.status(),
                    observedAt
                );

                String json = objectMapper.writeValueAsString(event);
                rideEventProducer.publishRideUpdate(event.rideId(), json);
            }
        } catch (Exception e) {
            log.error("[POLLER] Error polling and publishing ride data: {}", e.getMessage(), e);
        }
    }
}
