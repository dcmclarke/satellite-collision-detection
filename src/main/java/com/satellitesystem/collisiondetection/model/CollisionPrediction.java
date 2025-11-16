package com.satellitesystem.collisiondetection.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CollisionPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Satellite satellite1;

    @ManyToOne
    private Satellite satellite2;

    private LocalDateTime predictedTime;
    private double minimumDistance;
    private int probabilityScore;
    private String riskLevel; //critical, warning, info
    private String status; //active, resolved, false_aram
    private LocalDateTime createdAt;

    //constructor
    public CollisionPrediction() {
        this.createdAt = LocalDateTime.now();
    }

    //getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Satellite getSatellite1() { return satellite1; }
    public void setSatellite1(Satellite satellite1) { this.satellite1 = satellite1; }

    public Satellite getSatellite2() { return satellite2; }
    public void setSatellite2(Satellite satellite2) { this.satellite2 = satellite2; }

    public LocalDateTime getPredictedTime() { return predictedTime; }
    public void setPredictedTime(LocalDateTime predictedTime) { this.predictedTime = predictedTime; }

    public double getMinimumDistance() { return minimumDistance; }
    public void setMinimumDistance(double minimumDistance) { this.minimumDistance = minimumDistance; }

    public int getProbabilityScore() { return probabilityScore; }
    public void setProbabilityScore(int probabilityScore) { this.probabilityScore = probabilityScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
