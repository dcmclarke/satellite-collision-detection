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
    }

    public Satellite saveSatellite(Satellite satellite) {
        return repository.save(satellite);
    }

    public Satellite getSatellite(Long id) {
        return repository.findById(id).orElse(null);
    }
}
