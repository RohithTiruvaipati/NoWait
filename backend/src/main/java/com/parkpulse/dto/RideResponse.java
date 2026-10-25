package com.parkpulse.dto;

public record RideResponse(
    String id,
    String name,
    Integer waitTime,
    String status,
    String lastupdate
) {}
