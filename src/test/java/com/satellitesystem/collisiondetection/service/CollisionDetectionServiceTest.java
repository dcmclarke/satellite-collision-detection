package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CollisionDetectionServiceTest {

    @Autowired
    private CollisionDetectionService collisionDetectionService;

    @Autowired
    private SatelliteRepository satelliteRepository;

    @Autowired
    private CollisionPredictionRepository collisionRepository;

    @Autowired
    private CollisionDetectionService collisionService;

    @Test
    void testBasicMath() {
        //testing testing working
        int result = 2 + 2;
        assertEquals(4, result);
    }

    @Test
    void testDetectCollisions_FindsCloseApproach() {
        //detectCollisions() is public & uses calculateDistance (couldnt use private method calDistance)
        //testing detectCollisions(), testing calDistance at same time

        satelliteRepository.deleteAll();

        //create 2 sats very close together
        Satellite sat1 = new Satellite("SAT1", "1", 0.0, 0.0, 400.0);
        Satellite sat2 = new Satellite("SAT2", "2", 0.01, 0.01, 400.5);

        satelliteRepository.save(sat1);
        satelliteRepository.save(sat2);

        //run collision detect calling calDis internally
        List<CollisionPrediction> predictions = collisionService.detectCollisions();

        //if calDis works, should find collision
        assertTrue(predictions.size() > 0, "Should detect close satellites");
    }
    //TODO: REVISED TEST 1 CODE DONE, CHECK IF WORKS AND NOTES AND CONTINUE
    @Test
    void testDetectCollisions_EmptyDatabase() {
        satelliteRepository.deleteAll();

        List<CollisionPrediction> predictions = collisionService.detectCollisions();

        assertEquals(0, predictions.size(), "Empty databse should fine no collisions");
    }
}