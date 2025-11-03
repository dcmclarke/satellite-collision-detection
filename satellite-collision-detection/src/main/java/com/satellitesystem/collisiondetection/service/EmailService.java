package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${alert.recipient.email}")
    private String recipientEmail;

    //sends email alert for collision prediction
    public void sendCollisionAlert(CollisionPrediction prediction) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("COLLISION ALERT: " + prediction.getRiskLevel());
            message.setText(buildAlertMessage(prediction));

            mailSender.send(message);
            System.out.println("Email alert sent for collision: " + prediction.getSatellite1().getName() + " - " + prediction.getSatellite2().getName());
        } catch (Exception e) {
            System.err.println("Failed to send email alert: " + e.getMessage());
        }
    }

    //builds email message content
    private String buildAlertMessage(CollisionPrediction prediction) {
        StringBuilder message = new StringBuilder();

        message.append("SATELLITE COLLISION ALERT\n");
        message.append("Risk Level: ").append(prediction.getRiskLevel()).append("\n");
        message.append("Probability: ").append(prediction.getProbabilityScore()).append("%\n\n");

        message.append("Satellites Involved:\n");
        message.append("- ").append(prediction.getSatellite1().getName())
                .append(" (NORAD: ").append(prediction.getSatellite1().getNoradId()).append(")\n");
        message.append("- ").append(prediction.getSatellite2().getName())
                .append(" (NORAD: ").append(prediction.getSatellite2().getNoradId()).append(")\n\n");

        message.append("Minimum Distance: ")
                .append(String.format("%.2f", prediction.getMinimumDistance()))
                .append(" km\n");
        message.append("Detection Time: ").append(prediction.getPredictedTime()).append("\n\n");

        message.append("Action Required: Please review collision details in system dashboard\n");

        return message.toString();
    }
}

