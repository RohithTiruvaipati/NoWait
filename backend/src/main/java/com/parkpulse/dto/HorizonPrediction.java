package com.parkpulse.dto;

public record HorizonPrediction(
    int minutesAhead,
    Integer predictedWait,
    Integer expectedChange
) {}

