package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Alert;
import com.satellitesystem.collisiondetection.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class AlertService {

    @Autowired
    private AlertRepository repository;

    private final List<Alert>recentAlerts = new ArrayList<>();
    private final int MAX_RECENT_ALERTS = 100;

    public List<Alert> getAllAlerts() {
        return repository.findAll();
    }

    public List<Alert> getUnacknowledgedAlerts() {
        return repository.findByAcknowledged(false);
    }

    public List<Alert> getRecentAlerts() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return repository.findBySentAtAfter(oneDayAgo);
    }

    public Alert saveAlert(Alert alert) {
        //save to db
        Alert saved = repository.save(alert);
        //and add to in memory list for quick access
        addToRecentAlerts(saved);

        return saved;
    }

    private void addToRecentAlerts(Alert alert) {
        recentAlerts.add(0, alert);
        if (recentAlerts.size() > MAX_RECENT_ALERTS) {
            recentAlerts.remove(recentAlerts.size() - 1);
        }
    }

    public List<Alert> getInMemoryAlerts() {
        return new ArrayList<>(recentAlerts);
    }

    public Alert acknowledgeAlert(Long id) {
        Alert alert = repository.findById(id).orElse(null);
        if (alert != null) {
            alert.setAcknowledged(true);
            return repository.save(alert);
        }
        return null;
    }
}
