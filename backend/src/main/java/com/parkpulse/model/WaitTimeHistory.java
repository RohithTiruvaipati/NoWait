package com.parkpulse.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "wait_time_history",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_attraction_observed_at", columnNames = {"attraction_id", "observed_at"})
    },
    indexes = {
        @Index(name = "idx_history_attraction_observed", columnList = "attraction_id, observed_at DESC")
    }
)
public class WaitTimeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @Column(name = "wait_time")
    private Integer waitTime;

    @Column(nullable = false)
    private String status;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    public WaitTimeHistory() {
    }

    public WaitTimeHistory(Attraction attraction, Integer waitTime, String status, Instant observedAt) {
        this.attraction = attraction;
        this.waitTime = waitTime;
        this.status = status;
        this.observedAt = observedAt;
    }

    public Long getId() {
        return id;
    }

    public Attraction getAttraction() {
        return attraction;
    }

    public void setAttraction(Attraction attraction) {
        this.attraction = attraction;
    }

    public Integer getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Integer waitTime) {
        this.waitTime = waitTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }
}

