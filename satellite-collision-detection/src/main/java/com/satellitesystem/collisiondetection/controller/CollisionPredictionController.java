package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.service.CollisionPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collisions")
public class CollisionPredictionController {

    @Autowired
    private CollisionPredictionService service;

    //get all collision predictions
    @GetMapping
    public List<CollisionPrediction> getAllPredictions() {
        return service.getAllPredictions();
    }

    //get only active predictions (status=active)
    @GetMapping("/active")
    public List<CollisionPrediction> getActivePredictions() {
        return service.getActivePredictions();
    }

    //get only critical risk predictions
    @GetMapping("/critical")
    public List<CollisionPrediction> getCriticalPredictions() {
        return service.getCriticalPredictions();
    }
}
