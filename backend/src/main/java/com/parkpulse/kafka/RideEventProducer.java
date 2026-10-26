package com.parkpulse.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RideEventProducer {

    private static final Logger log = LoggerFactory.getLogger(RideEventProducer.class);
    public static final String TOPIC = "ride-updates";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public RideEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a ride update JSON string to Kafka.
     * Uses rideId as the key to guarantee partitioned order for each attraction.
     */
    public void publishRideUpdate(String rideId, String json) {
        log.info("[PRODUCER] Publishing to {}: key={}", TOPIC, rideId);
        kafkaTemplate.send(TOPIC, rideId, json);
    }
}
