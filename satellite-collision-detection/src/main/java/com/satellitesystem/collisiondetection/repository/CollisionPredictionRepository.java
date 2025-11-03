package com.satellitesystem.collisiondetection.repository;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CollisionPredictionRepository extends JpaRepository<CollisionPrediction, Long> {
    List<CollisionPrediction> findByStatus(String status);
    List<CollisionPrediction> findByRiskLevel(String riskLevel);
}
