package com.parkpulse.kafka;

import java.time.Instant;

/**
 * Event representing a snapshot of an attraction's wait time and status.
 */
public record RideEvent(
    String rideId,
    String name,
    Integer waitTime,
    String status,
    Instant observedAt
) {}