package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.model.Alert;
import com.satellitesystem.collisiondetection.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService service;

    @GetMapping
    public List<Alert> getAllAlerts() {
        return service.getAllAlerts();
    }

    @GetMapping("/unacknowledged")
    public List<Alert> getUnacknowledgedAlerts() {
        return service.getUnacknowledgedAlerts();
    }

    @GetMapping("/recent")
    public List<Alert> getRecentAlerts() {
        return service.getRecentAlerts();
    }

    @PostMapping
    public Alert createAlert(@RequestBody Alert alert) {
        return service.saveAlert(alert);
    }

    @PostMapping("/{id}/acknowledge")
    public Alert acknowledgeAlert(@PathVariable Long id) {
        return service.acknowledgeAlert(id);
    }

    //get in memory alerts (for demo)
    @GetMapping("/in-memory")
    public List<Alert> getInMemoryAlerts() {
        return service.getInMemoryAlerts();
    }
}
