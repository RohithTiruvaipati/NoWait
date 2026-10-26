package com.parkpulse.service;

import com.parkpulse.dto.HorizonPrediction;
import com.parkpulse.dto.PredictionResponse;
import com.parkpulse.model.Attraction;
import com.parkpulse.model.WaitTimeHistory;
import com.parkpulse.repository.AttractionRepository;
import com.parkpulse.repository.WaitTimeHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);
    private static final int DEFAULT_HORIZON_MINUTES = 30;
    private static final int[] MULTI_HORIZONS = {15, 30, 45};
    private static final int LOOKBACK_WINDOW_MINUTES = 60;
    private static final int MIN_OBSERVATIONS = 3;

    private final WaitTimeHistoryRepository historyRepository;
    private final AttractionRepository attractionRepository;

    public PredictionService(WaitTimeHistoryRepository historyRepository, AttractionRepository attractionRepository) {
        this.historyRepository = historyRepository;
        this.attractionRepository = attractionRepository;
    }

    /**
     * Short-term linear trend predictor across multiple horizons (15, 30, 45 minutes)
     * based on recent observations from the last 60 minutes.
     */
    public PredictionResponse predictWaitTime(String rideId) {
        Attraction attraction = attractionRepository.findById(rideId).orElse(null);
        String rideName = (attraction != null) ? attraction.getName() : "Unknown Ride";

        // 1. Fetch observations from PostgreSQL ordered chronologically
        List<WaitTimeHistory> allHistory = historyRepository.findByAttractionIdOrderByObservedAtAsc(rideId);

        // Filter valid operating observations with non-null wait times
        List<WaitTimeHistory> validHistory = allHistory.stream()
                .filter(h -> h.getWaitTime() != null && "OPERATING".equalsIgnoreCase(h.getStatus()))
                .toList();

        if (validHistory.isEmpty()) {
            List<HorizonPrediction> emptyHorizons = new ArrayList<>();
            for (int h : MULTI_HORIZONS) {
                emptyHorizons.add(new HorizonPrediction(h, null, null));
            }
            return new PredictionResponse(
                    rideId,
                    rideName,
                    null,
                    null,
                    DEFAULT_HORIZON_MINUTES,
                    null,
                    "NO_DATA",
                    "LINEAR_TREND",
                    "No valid operating observations found for this ride.",
                    emptyHorizons
            );
        }

        // Current wait is the most recent valid observation
        WaitTimeHistory latestRecord = validHistory.get(validHistory.size() - 1);
        int currentWait = latestRecord.getWaitTime();
        Instant latestTime = latestRecord.getObservedAt();

        // 2. Filter observations within the last 60 minutes relative to the latest record
        Instant windowStart = latestTime.minus(Duration.ofMinutes(LOOKBACK_WINDOW_MINUTES));
        List<WaitTimeHistory> windowHistory = validHistory.stream()
                .filter(h -> !h.getObservedAt().isBefore(windowStart))
                .toList();

        // If fewer than MIN_OBSERVATIONS in the window, fallback to baseline (current wait for all horizons)
        if (windowHistory.size() < MIN_OBSERVATIONS) {
            List<HorizonPrediction> baselineHorizons = new ArrayList<>();
            for (int h : MULTI_HORIZONS) {
                baselineHorizons.add(new HorizonPrediction(h, currentWait, 0));
            }
            return new PredictionResponse(
                    rideId,
                    rideName,
                    currentWait,
                    currentWait,
                    DEFAULT_HORIZON_MINUTES,
                    0,
                    "STABLE",
                    "BASELINE",
                    "Insufficient data for linear regression (requires at least " + MIN_OBSERVATIONS + " points in 60m). Returning current wait baseline.",
                    baselineHorizons
            );
        }

        // 3. Simple Linear Regression: y = m * x + b
        // x = elapsed minutes from windowStart, y = waitTime
        int n = windowHistory.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (WaitTimeHistory h : windowHistory) {
            double x = Duration.between(windowStart, h.getObservedAt()).toSeconds() / 60.0;
            double y = h.getWaitTime();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = (n * sumX2 - sumX * sumX);
        double slope = 0.0; // rate of change (minutes of wait per minute of elapsed time)
        if (Math.abs(denominator) > 0.0001) {
            slope = (n * sumXY - sumX * sumY) / denominator;
        }

        // Calculate predictions for 15, 30, and 45 minutes
        List<HorizonPrediction> horizons = new ArrayList<>();
        Integer waitAt30 = null;
        Integer changeAt30 = null;

        for (int minutesAhead : MULTI_HORIZONS) {
            int delta = (int) Math.round(slope * minutesAhead);
            int predicted = Math.max(0, Math.min(240, currentWait + delta));
            int change = predicted - currentWait;
            horizons.add(new HorizonPrediction(minutesAhead, predicted, change));

            if (minutesAhead == DEFAULT_HORIZON_MINUTES) {
                waitAt30 = predicted;
                changeAt30 = change;
            }
        }

        if (waitAt30 == null) {
            waitAt30 = currentWait;
            changeAt30 = 0;
        }

        // Determine recommendation based on 30m horizon
        String recommendation;
        if (changeAt30 >= 10) {
            recommendation = "RIDE_NOW"; // Wait time expected to spike soon!
        } else if (changeAt30 <= -10) {
            recommendation = "CONSIDER_WAITING"; // Wait time expected to drop
        } else {
            recommendation = "STABLE"; // Wait time expected to remain steady
        }

        return new PredictionResponse(
                rideId,
                rideName,
                currentWait,
                waitAt30,
                DEFAULT_HORIZON_MINUTES,
                changeAt30,
                recommendation,
                "LINEAR_TREND",
                String.format("Calculated trend over %d points (slope: %.2f min/min).", n, slope),
                horizons
        );
    }
}
