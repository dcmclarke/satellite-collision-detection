package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
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
    private CollisionDetectionService collisionService;

    @Autowired
    private SatelliteRepository satelliteRepository;

    @Autowired
    private CollisionPredictionRepository collisionRepository;

    @Autowired
    AlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        collisionRepository.deleteAll();
        satelliteRepository.deleteAll();
    }

    //TEST 1: Sanity check
    @Test
    void testBasicMath() {
        //testing testing working
        int result = 2 + 2;
        assertEquals(4, result);
    }

    // TEST 2: detect close satellites
    @Test
    void testDetectCollisions_FindsCloseApproach() {
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

    //TEST 3: empty db
    @Test
    void testDetectCollisions_EmptyDatabase() {

        List<CollisionPrediction> predictions = collisionService.detectCollisions();

        assertEquals(0, predictions.size(), "Empty databse should fine no collisions");
    }

    //TEST 4: two far sats should not trigger collisions
    @Test
    void testDetectCollisions_TwoFarSatellites() {

        //create satellites on opposite side of earth
        Satellite sat1 = new Satellite("SAT1", "1", 0.0, 0.0, 400.0);
        Satellite sat2 = new Satellite("SAT2", "2", 90.0, 180.0, 500.0);

        satelliteRepository.save(sat1);
        satelliteRepository.save(sat2);

        List<CollisionPrediction> predictions = collisionService.detectCollisions();

        assertEquals(0, predictions.size(), "Far satellites shouldn't trigger collision warning");
    }

    //TEST 5: risk level critical for satellites less than 2km apart
    @Test
    void testRiskLevel_VeryClose() {

        //create satellites extreme close as <2km equals critical
        Satellite sat1 = new Satellite("SAT1", "1", 0.0, 0.0, 400.0);
        Satellite sat2 = new Satellite("SAT2", "2", 0.005, 0.005, 400.2);

        satelliteRepository.save(sat1);
        satelliteRepository.save(sat2);

        List<CollisionPrediction> predictions = collisionService.detectCollisions();

        //should fine at leat one prediction
        assertTrue(predictions.size() > 0, "Should detect collisions");
        //should classify as critical
        boolean hasCritical = predictions.stream().anyMatch(p -> "CRITICAL".equals(p.getRiskLevel()));
        assertTrue(hasCritical, "Very close satellites should be CRITICAL risk");
    }

    //TESt 6: risk level warning for satellites 2-3.5km apart
    @Test
    void testRiskLevel_ModerateDistance_ReturnsWarning() {
        Satellite sat1 = new Satellite("SAT1", "1", 0.0, 0.0, 400.0);
        Satellite sat2 = new Satellite("SAT2", "2", 0.02, 0.02, 401.0);

        //create satellites 2 -3.5km apart equals warning
        satelliteRepository.save(sat1);
        satelliteRepository.save(sat2);

        List<CollisionPrediction> predictions = collisionService.detectCollisions();

        //should fine collision
        assertTrue(predictions.size() > 0, "Should detect collision");

        //should have at least one warning
        boolean hasWarning = predictions.stream().anyMatch(p -> "WARNING".equals(p.getRiskLevel()));
        assertTrue(hasWarning, "Moderately close satellites should be WARNING risk");
    }

    //TEST 7: multiple satellites
    @Test
    void testDetectCollisions_ThreeSatellites() {
        Satellite sat1 = new Satellite("SAT1", "1", 0.0, 0.0, 400.0);
        Satellite sat2 = new Satellite("SAT2", "2", 0.01, 0.01, 400.0);
        Satellite sat3 = new Satellite("SAT3", "3", 50.0, 50.0, 500.0);

        satelliteRepository.save(sat1);
        satelliteRepository.save(sat2);
        satelliteRepository.save(sat3);

        List<CollisionPrediction> predictions = collisionService.detectCollisions();

        assertTrue(predictions.size() >= 1, "Should detect at least one collision");
        assertTrue(predictions.size() <= 3, "Should not exceed possible pairs");
    }

    //TEST 8: clears old predictions
    @Test
    void testDetectCollisions_ClearsOldPredictions() {
        Satellite sat1 = new Satellite("SAT1", "1", 0.0, 0.0, 400.0);
        Satellite sat2 = new Satellite("SAT2", "2", 0.01, 0.01, 400.5);

        satelliteRepository.save(sat1);
        satelliteRepository.save(sat2);

        collisionService.detectCollisions();
        long firstCount = collisionRepository.count();

        collisionService.detectCollisions();
        long secondCount = collisionRepository.count();

        assertEquals(firstCount, secondCount, "Should not accumulate duplicates");
    }
}
