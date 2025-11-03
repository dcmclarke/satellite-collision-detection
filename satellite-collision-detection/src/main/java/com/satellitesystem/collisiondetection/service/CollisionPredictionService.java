package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CollisionPredictionService {

    @Autowired
    private CollisionPredictionRepository repository;

    public List<CollisionPrediction> getAllPredictions() {
        return repository.findAll();
    }

    public List<CollisionPrediction> getActivePredictions() {
        return repository.findByStatus("ACTIVE");
    }

    public List<CollisionPrediction> getCriticalPredictions() {
        return repository.findByRiskLevel("CRITICAL");
    }

    public CollisionPrediction savePrediction(CollisionPrediction prediction) {
        return repository.save(prediction);
    }
}
