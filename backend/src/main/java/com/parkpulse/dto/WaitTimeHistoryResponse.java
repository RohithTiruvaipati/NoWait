package com.parkpulse.dto;

import java.time.Instant;

public record WaitTimeHistoryResponse(
    Integer waitTime,
    String status,
    Instant observedAt
) {}

