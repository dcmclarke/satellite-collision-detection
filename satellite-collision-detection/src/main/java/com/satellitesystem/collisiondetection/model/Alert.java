package com.satellitesystem.collisiondetection.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CollisionPrediction prediction;

    private String alertLevel; //critical, warning, info
    private String message;
    private String recipientEmail;
    private LocalDateTime sentAt;
    private boolean acknowledged;

    //constructor
    public Alert() {
        this.acknowledged = false;
    }

    //constructor with fields
    private Alert(CollisionPrediction prediction, String alertLevel, String message, String recipientEmail) {
        this.prediction = prediction;
        this.alertLevel = alertLevel;
        this.message = message;
        this.recipientEmail = recipientEmail;
        this.sentAt = LocalDateTime.now();
        this.acknowledged = false;
    }

    //getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CollisionPrediction getPrediction() { return prediction; }
    public void setPrediction(CollisionPrediction prediction) { this.prediction = prediction; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}
