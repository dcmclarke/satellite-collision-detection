package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import com.satellitesystem.collisiondetection.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//service for detecting potential satellite collisions using distance based screening. Conjunction detectio methods based on Burgis et al. (2022) & operational thresholds Lechtenberg (2019)

@Service
public class CollisionDetectionService {
   //distance thresholds (km)
    private static final double EARTH_RADIUS = 6371.8;  //earth's radius in kms (standard reference value)
    private static final double COLLISION_THRESHOLD = 5.0;
    private static final double CRITICAL_DISTANCE = 2.0; // <2km = critical
    private static final double WARNING_DISTANCE = 3.5; // <3.5km = warning

    //probability score constants
    private static final int PROBABILITY_CRITICAL = 90;
    private static final int PROBABILITY_WARNING = 60;
    private static final int PROBABILITY_INFO = 30;


    @Autowired
    private EmailService emailService;

    @Autowired
    private AlertService alertService;

    @Value("${alert.email.enabled:false}")
    private boolean emailEnabled;

    @Autowired
    private SatelliteRepository satelliteRepository;

    @Autowired
    private CollisionPredictionRepository collisionRepository;

    //main method detecitn all potential collisions in satellite population
    //checks every pair of satellites for proximity within collision threshold
    public List<CollisionPrediction> detectCollisions() {
        System.out.println("Starting collision detection...");

        //get all satellites from db
        List<Satellite> satellites = satelliteRepository.findAll();
        List<CollisionPrediction> predictions = new ArrayList<>();

        System.out.println("Analysing " + satellites.size() + " satellites...");

        //check every unique pair of satellites
        //currently using nested loop for 11 satellites, checking 55 pairs (n*(n-1)/2)
        int pairsChecked = 0;
        for (int i = 0; i < satellites.size(); i++) {
            for (int j = i + 1; j <satellites.size(); j++) {
                pairsChecked++;

                Satellite sat1 = satellites.get(i);
                Satellite sat2 = satellites.get(j);


                //TODO: could add velocity vectors for kinetic energy assessment (from lechtenberg paper)
                //would enable catastrophic vs non catastrophic classification based on
                //catastrophic if: (M_secondary * V_rel²) / (2 * M_primary) > 40,000 J/kg
                //but would no use for it yet.

                //calculate 3d distance between two satellites
                double distance = calculateDistance(sat1, sat2);

                //if satellites within collision threshold create prediction
                if (distance < COLLISION_THRESHOLD) {
                    CollisionPrediction prediction = createPrediction(sat1, sat2, distance);
                    predictions.add(prediction);

                    System.out.println("COLLISION RISK: " + sat1.getName() + " and " + sat2.getName() + " are " + String.format("%.2f", distance) + " km apart!");
                }
            }
        }

        System.out.println("Checked " + pairsChecked + " satellite pairs");
        System.out.println("Found " + predictions.size() + " potential collisions");

        //save all predictions to db
        if (!predictions.isEmpty()) {
            collisionRepository.saveAll(predictions);
            System.out.println("Saved " + predictions.size() + " collision prediction to database");
        }
        return predictions;
    }

    //now calculating 3D Euclidean distance between two satellites
    //1. Convert both satellite's geodetic coordinates to cartesian (XYZ)
    //2. Apply pythagorean theorem in 3D space: distance = sqrt((x2-x1)^2 + (y2-yq)^2 + (z2-z1)^2)
    private double calculateDistance(Satellite sat1, Satellite sat2) {
        //convert both satellites to 3D cartesian coords
        double[] pos1 = latLonAltToXYZ(sat1);
        double[] pos2 = latLonAltToXYZ(sat2);

        //calculating diff in each dimension
        double dx = pos2[0] - pos1[0];
        double dy = pos2[1] - pos1[1];
        double dz = pos2[2] - pos1[2];

        //pythag theorem in 3D (adapted from Baeldung java distance calculations)
        //gives straight line distance through 3D space
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    //converting satellite position from geodetic coords (lat/long/alt) to 3D cartesian coords (x,y,z)
    //uses speherical to cartesian transformation:
    //- X = (R + altitude) * cos(latitude) * cos(longitude)
    //- Y = (R + altitude) * cos(latitude) * sin(longitude)
    //- Z = (R + altitude) * sin(latitude)
    //where R is Earth's radius at sea level (6371 km)

    private double[] latLonAltToXYZ(Satellite sat) {
        //convert degrees to radians (java trig functions use radians)
        double latRad = Math.toRadians(sat.getLatitude());
        double lonRad = Math.toRadians(sat.getLongitude());

        //distnace from earth's center equals earth radisu plus altitude above sea level
        double r = EARTH_RADIUS + sat.getAltitude();

        //apply speherical to cartesian coordinate transformation
        //which converts from (lat,lon,alt) to (x,y,z)
        double x = r * Math.cos(latRad) * Math.cos(lonRad);
        double y = r * Math.cos(latRad) * Math.sin(lonRad);
        double z = r * Math.sin(latRad);

        return new double[]{x, y, z};
    }

    //creating CollisionPrediction object with risk assessment
    //risk levels based on distance:
    //CRITICAL (<2km): High prob of collision
    //WARNING (<2-3.5km): Moderate risk, requires monitoring
    //INFO (3.5-5km): Low risk, informational alert

    private CollisionPrediction createPrediction(Satellite sat1, Satellite sat2, double distance) {
        CollisionPrediction prediction = new CollisionPrediction();

        //set satellite references
        prediction.setSatellite1(sat1);
        prediction.setSatellite2(sat2);

        //set distance & time
        prediction.setMinimumDistance(distance);
        prediction.setPredictedTime(LocalDateTime.now()); //current time for prediction

        //assign risk level & probability score based on distance
        if (distance < CRITICAL_DISTANCE) {
            prediction.setRiskLevel("CRITICAL");
            prediction.setProbabilityScore(PROBABILITY_CRITICAL);

            //send email for critical risk
            emailService.sendCollisionAlert(prediction);

        } else if (distance < WARNING_DISTANCE) {
            prediction.setRiskLevel("WARNING");
            prediction.setProbabilityScore(PROBABILITY_WARNING);
        } else {
            prediction.setRiskLevel("INFO");
            prediction.setProbabilityScore(PROBABILITY_INFO);
        }

        //set status as active
        prediction.setStatus("ACTIVE");

        //create & save alert
        Alert alert = createAlert(prediction);
        alertService.saveAlert(alert);

        if (emailEnabled && distance < CRITICAL_DISTANCE) {
            try {
                emailService.sendCollisionAlert(prediction);
            } catch (Exception e) {
                System.err.println("Email sending failed, but alert saved: " + e.getMessage());
            }
        }
        return prediction;
    }

    //helper to create alert from prediction
    private Alert createAlert(CollisionPrediction prediction) {
        Alert alert = new Alert();
        alert.setPrediction(prediction);
        alert.setAlertLevel(prediction.getRiskLevel());
        alert.setMessage(buildAlertMessage(prediction));
        alert.setRecipientEmail("operator@example.com"); //palceholder
        alert.setSentAt(LocalDateTime.now());
        alert.setAcknowledged(false);
        return alert;
    }

    private String buildAlertMessage(CollisionPrediction prediction) {
        return String.format("COLLISION ALERT: % and %s are %.2f km apart (Risk: %s)",
                prediction.getSatellite1().getName(),
                prediction.getSatellite2().getName(),
                prediction.getMinimumDistance(),
                prediction.getRiskLevel()
        );
    }

    //get count of satellites currently in db
    public long getSatelliteCount() {
        return satelliteRepository.count();
    }

    //get count of active collision predictions
    public long getCollisionCount() {
        return collisionRepository.findByStatus("ACTIVE").size();
    }
}
