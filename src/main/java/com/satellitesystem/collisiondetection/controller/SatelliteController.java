package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.repository.AlertRepository;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import com.satellitesystem.collisiondetection.service.CollisionDetectionService;
import com.satellitesystem.collisiondetection.service.NasaApiService;
import com.satellitesystem.collisiondetection.service.SatelliteService;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import com.satellitesystem.collisiondetection.repository.AlertRepository;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/satellites")
public class SatelliteController {

    @Autowired
    private SatelliteService service;

    @Autowired
    private NasaApiService nasaApiService;

    @Autowired
    private CollisionDetectionService collisionDetectionService;

    @Autowired
    private SatelliteRepository satelliteRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private CollisionPredictionRepository collisionPredictionRepository;

    //trigger collision detection for all satellites
    //POST http://localhost:8080/api/satellites/detection-collisions
    @PostMapping("/detect-collisions")
    public String detectCollisions() {
        List<CollisionPrediction> predictions = collisionDetectionService.detectCollisions();
        return "Collision detection complete! Found " + predictions.size() + " potential collisions. "
                + "Total satellites analyzed: " + collisionDetectionService.getSatelliteCount();
    }

    @GetMapping
    public List<Satellite> getAllSatellites() {
        return service.getAllSatellites();
    }

    @PostMapping
    public Satellite createSatellite(@RequestBody Satellite satellite) {
        return service.saveSatellite(satellite);
    }

    @GetMapping("/{id}")
    public Satellite getSatellite(@PathVariable Long id) {
        return service.getSatellite(id);
    }

    /*PRIMARY METHOD: fetches live data from nasa space-track api
    *POST http://localhost:8080/api/satellites/fetch-nasa-data
    */
    @PostMapping("/fetch-nasa-data")
    public String fetchNasaData() {
        String result = nasaApiService.fetchAndStoreSatellites();
        long totalCount = nasaApiService.getSatelliteCount();
        return result + " Total satellites in database: " + totalCount;
    }

    /**
     * BACKUP:loads embedded satellite data for demo if needed
     * Use when Space-Track API is unavailable or offline dev
     * POST http://localhost:8080/api/satellites/load-backup-data
     */

    @PostMapping("/load-backup-data")
    public String loadBackupData() {
        String result = nasaApiService.loadBackupData();
        long totalCount = nasaApiService.getSatelliteCount();
        return result + " Total satellites in database: " + totalCount;
    }

    //fix for dupe satellites - deletes in correct order
    @PostMapping("/clear-all")
    public String clearAll() {
        //delete in order: alerts, collision predictions, sats
        alertRepository.deleteAll();
        collisionPredictionRepository.deleteAll();
        satelliteRepository.deleteAll();
        return "All data cleared from database.";
    }
}
