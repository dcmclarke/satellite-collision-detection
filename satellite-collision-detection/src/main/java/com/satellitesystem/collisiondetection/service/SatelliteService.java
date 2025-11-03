package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SatelliteService {

    @Autowired //dependency injection
    private SatelliteRepository repository;

    public List<Satellite> getAllSatellites() {
        return repository.findAll();
        //TODO: add pagination for large datasets (1k+ sats)
        //TODO: cache frequently accessed sats for better performance
    }

    //TODO: next week add method to fetch from NASA API
    //TODO: add method to filter by altitude range

    public Satellite saveSatellite(Satellite satellite) {
        return repository.save(satellite);
    }

    public Satellite getSatellite(Long id) {
        return repository.findById(id).orElse(null);
    }
}
