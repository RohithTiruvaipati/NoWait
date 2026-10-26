package com.parkpulse.dto;

import java.util.List;

public record PredictionResponse(
    String rideId,
    String name,
    Integer currentWait,
    Integer predictedWait,
    int predictionMinutesAhead,
    Integer expectedChange,
    String recommendation,
    String model,
    String message,
    List<HorizonPrediction> horizons
) {}
