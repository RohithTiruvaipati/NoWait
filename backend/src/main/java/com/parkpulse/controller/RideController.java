package com.parkpulse.controller;

import com.parkpulse.dto.PredictionResponse;
import com.parkpulse.dto.RideResponse;
import com.parkpulse.dto.WaitTimeHistoryResponse;
import com.parkpulse.service.PredictionService;
import com.parkpulse.service.RideHistoryService;
import com.parkpulse.service.ThemeParkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final ThemeParkService themeParkService;
    private final RideHistoryService rideHistoryService;
    private final PredictionService predictionService;

    public RideController(
            ThemeParkService themeParkService,
            RideHistoryService rideHistoryService,
            PredictionService predictionService) {
        this.themeParkService = themeParkService;
        this.rideHistoryService = rideHistoryService;
        this.predictionService = predictionService;
    }

    /**
     * GET /api/rides
     * Returns all attractions and their current live wait times.
     */
    @GetMapping
    public List<RideResponse> getAllRides() {
        return themeParkService.getRides();
    }

    /**
     * GET /api/rides/{id}
     * Returns a single attraction by its ID.
     * Returns 404 Not Found if the ride ID does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRideById(@PathVariable String id) {
        return themeParkService.getRideById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/rides/{id}/history
     * Returns chronological historical wait times for a specific attraction from PostgreSQL.
     */
    @GetMapping("/{id}/history")
    public List<WaitTimeHistoryResponse> getRideHistory(@PathVariable String id) {
        return rideHistoryService.getRideHistory(id);
    }

    /**
     * GET /api/rides/{id}/prediction
     * Returns a 30-minute short-term wait time prediction with trend slope and recommendation.
     */
    @GetMapping("/{id}/prediction")
    public PredictionResponse getRidePrediction(@PathVariable String id) {
        return predictionService.predictWaitTime(id);
    }
}
