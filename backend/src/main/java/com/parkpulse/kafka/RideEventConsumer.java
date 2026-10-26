package com.parkpulse.kafka;

import tools.jackson.databind.ObjectMapper;
import com.parkpulse.service.RideHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RideEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RideEventConsumer.class);

    private final RideHistoryService rideHistoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RideEventConsumer(RideHistoryService rideHistoryService) {
        this.rideHistoryService = rideHistoryService;
    }

    @KafkaListener(topics = "ride-updates", groupId = "parkpulse")
    public void consume(String message) {
        try {
            RideEvent event = objectMapper.readValue(message, RideEvent.class);
            log.info("[CONSUMER] Received event: {} ({} min, {})", event.name(), event.waitTime(), event.status());
            rideHistoryService.recordRideUpdate(event);
        } catch (Exception e) {
            log.error("[CONSUMER] Error processing Kafka event: {}", e.getMessage(), e);
        }
    }
}
