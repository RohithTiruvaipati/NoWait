package com.parkpulse.controller;

import com.parkpulse.dto.RideResponse;
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

    public RideController(ThemeParkService themeParkService) {
        this.themeParkService = themeParkService;
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
}
