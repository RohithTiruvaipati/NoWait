package com.parkpulse.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.parkpulse.dto.RideResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ThemeParkService {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LIVE_URL =
            "https://api.themeparks.wiki/v1/entity/75ea578a-adc8-4116-a54d-dccb60765ef9/live";

    /**
     * Fetches live data from ThemeParks.wiki and extracts all attraction wait times.
     */
    public List<RideResponse> getRides() {
        String jsonResponse = restClient.get()
                .uri(LIVE_URL)
                .retrieve()
                .body(String.class);

        List<RideResponse> rides = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode liveDataArray = root.path("liveData");

            if (liveDataArray.isArray()) {
                for (JsonNode item : liveDataArray) {
                    // Filter: Only extract attractions (exclude SHOW, RESTAURANT, etc.)
                    String entityType = item.path("entityType").asText("");
                    if (!"ATTRACTION".equalsIgnoreCase(entityType)) {
                        continue;
                    }

                    String id = item.path("id").asText();
                    String name = item.path("name").asText();
                    String status = item.path("status").asText();
                    String lastUpdate = item.path("lastUpdated").asText();

                    // Standby wait time is nested under queue -> STANDBY -> waitTime
                    Integer waitTime = null;
                    JsonNode standbyNode = item.path("queue").path("STANDBY");
                    if (standbyNode.has("waitTime") && !standbyNode.get("waitTime").isNull()) {
                        waitTime = standbyNode.get("waitTime").asInt();
                    }

                    rides.add(new RideResponse(id, name, waitTime, status, lastUpdate));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse live ride data from ThemeParks.wiki", e);
        }

        return rides;
    }

    /**
     * Finds a single attraction by its ID.
     */
    public Optional<RideResponse> getRideById(String id) {
        return getRides().stream()
                .filter(ride -> ride.id().equalsIgnoreCase(id))
                .findFirst();
    }
}
