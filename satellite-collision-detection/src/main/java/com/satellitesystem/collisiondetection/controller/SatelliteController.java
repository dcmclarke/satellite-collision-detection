package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.service.CollisionDetectionService;
import com.satellitesystem.collisiondetection.service.NasaApiService;
import com.satellitesystem.collisiondetection.service.SatelliteService;
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

    //trigger collision detection for all satellites
    //POST http://localhost:8080/api/satellites/detection-collisions
    @PostMapping("/detect-collisions")
    public String detectCollisions() {
        List<CollisionPrediction> predictions = collisionDetectionService.detectCollisions();
        return "Collision detection complete! Found " + predictions.size() + " potential collisions. "
                + "Total satellites: " + collisionDetectionService.getCollisionCount();

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
    *needs valid credentials and network
    *fails due to auth issues
    *POST http://localhost:8080/api/satellites/fetch-nasa-data
    */
    @PostMapping("/fetch-nasa-data")
    public String fetchNasaData() {
        String result = nasaApiService.fetchAndStoreSatellites();
        long totalCount = nasaApiService.getSatelliteCount();
        return result + " Total satellites in database: " + totalCount;
    }

    /*ALTERNATIVE METHOD (I want to work): loads authentic NASA TLE data from hardcoded dataset
    *data includes VANGUARD satellites, ISS, Hubble, Starlink with real orbital parameters
    *used for reliable demo - eliminates API authentication dependencies
    *POST http://localhost:8080/api/satellites/load-real-data
    */
    @PostMapping("/load-real-data")
    public String loadRealData() {
        String result = nasaApiService.loadRealNasaData();
        long totalCount = nasaApiService.getSatelliteCount();
        return result + " Total satellites in database: " + totalCount;
    }
}
