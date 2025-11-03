package com.satellitesystem.collisiondetection.repository;

import com.satellitesystem.collisiondetection.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByAlertLevel(String alertlevel);
    List<Alert> findByAcknowledged(boolean acknowledged);
    List<Alert> findBySentAtAfter(LocalDateTime date);
}
