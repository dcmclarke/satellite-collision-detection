package com.satellitesystem.collisiondetection.model;

import jakarta.persistence.*;

@Entity //tells springboot this is a db table
public class Satellite {

    @Id //primary key for table
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto increment id
    private Long id;

    private String name;

    //NASA NORAD catalog no. uniqueling identifying each satellite
    private String noradId;

    //orbital position in degrees
    private double latitude;
    private double longitude;

    //altitude in km above earth
    private double altitude;

    //TODO: add velocity tracking for better collision prediction

    //constructor for jpa
    public Satellite() {
    }
        //constructor with fields
        public Satellite(String name, String noradId, double latitude, double longitude, double altitude) {
            this.name = name;
            this.noradId = noradId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
        }

        //getters & setters
        public Long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getNoradId() { return noradId; }
        public void setNoradId(String noradId) { this.noradId = noradId; }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public double getAltitude() { return altitude; }
        public void setAltitude(double altitude) { this.altitude = altitude; }
    }
