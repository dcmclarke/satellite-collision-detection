package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class NasaApiService {

    @Autowired
    private SatelliteRepository satelliteRepository;

    @Value("${nasa.api.username}")
    private String username;

    @Value("${nasa.api.password}")
    private String password;

    @Value("${nasa.api.url}")
    private String apiUrl;

    //fetches sat data from Space-Track.org api, gets latest 100 sats for testing
    public String fetchAndStoreSatellites() {
        System.out.println("Starting Space-Track API fetch please wait...");

        try {
            //create cookie manager
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL);
            java.net.CookieHandler.setDefault(cookieManager);

            //create HTTP client
            HttpClient client = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            //step 1: login using /ajaxauth/login (like the Python client does)
            String loginUrl = "https://www.space-track.org/ajaxauth/login";
            System.out.println("Logging in to Space-Track...");

            String loginBody = "identity=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(loginUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                    .build();

            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Login response: " + loginResponse.statusCode());
            System.out.println("Login body: " + loginResponse.body());
            System.out.println("Cookies stored: " + cookieManager.getCookieStore().getCookies());

            //check if login succeeded
            if (loginResponse.body().contains("\"Login\":\"Failed\"")) {
                return "Login failed - check your username and password";
            }

            System.out.println("Login successful!");

            //step 2: fetch satellite data
            //get 500 active satellites (updated in last 30 days)
            String dataUrl = "https://www.space-track.org/basicspacedata/query/class/tle_latest/ORDINAL/1/EPOCH/%3Enow-30/LIMIT/500/format/json";
            System.out.println("Fetching satellite data...");

            HttpRequest dataRequest = HttpRequest.newBuilder()
                    .uri(URI.create(dataUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> dataResponse = client.send(dataRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Data response: " + dataResponse.statusCode());

            if (dataResponse.statusCode() != 200) {
                return "Data fetch failed with status: " + dataResponse.statusCode()
                        + " - Response: " + dataResponse.body();
            }

            System.out.println("Data received! Parsing...");

            int count = parseSatelliteData(dataResponse.body());

            String result = "Successfully fetched " + count + " satellites from Space-Track!";
            System.out.println(result);
            return result;

        } catch (IOException | InterruptedException e) {
            String error = "Error fetching Space-Track data: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return error;
        }
    }

    //parses JSON from NASA & converts to sat objects
    private int parseSatelliteData(String jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonData);

            List<Satellite> satellites = new ArrayList<>();

            //loop through each sat in json array
            for (JsonNode node : rootNode) {
                Satellite satellite = new Satellite();

                //extract data from json
                satellite.setName(node.get("OBJECT_NAME").asText());
                satellite.setNoradId(node.get("NORAD_CAT_ID").asText());

                //orbital elements
                satellite.setLatitude(node.get("INCLINATION").asDouble());
                satellite.setLongitude(node.get("RA_OF_ASC_NODE").asDouble());
                satellite.setAltitude(node.get("MEAN_MOTION").asDouble() * 100);

                satellites.add(satellite);

                //print first sat as example
                if (satellites.size() == 1) {
                    System.out.println("Example satellite: " + satellite.getName());
                }
            }

            //save all to db at once
            satelliteRepository.saveAll(satellites);
            System.out.println("Saved " + satellites.size() + " satellites to database");

            return satellites.size();
        } catch (Exception e) {
            System.err.println("Error parsing satellite data: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    //get count of sats in db
    public long getSatelliteCount() {
        return satelliteRepository.count();
    }

    /**
     * Fallback data source for demo
     * Has sats in similar LEO orbits to show collision detection works
     * Used when Space Track API is unavailable (network outage, rate limits, downtime etc.)
     *
     * TLE data snapshot: October 2024, sourced from Space-Track.org
     */
    public String loadBackupData() {
        System.out.println("Loading backup satellite data for demonstration...");

        try {
            //leo satellites with known proximity for collision detection demo
            String backupData = """
        [
          {"OBJECT_NAME": "ISS (ZARYA)", "NORAD_CAT_ID": "25544", 
           "INCLINATION": "51.6416", "RA_OF_ASC_NODE": "247.4627", "MEAN_MOTION": "15.50103472"},
          {"OBJECT_NAME": "STARLINK-1007", "NORAD_CAT_ID": "44713",
           "INCLINATION": "53.0532", "RA_OF_ASC_NODE": "327.8503", "MEAN_MOTION": "15.06415123"},
          {"OBJECT_NAME": "STARLINK-1020", "NORAD_CAT_ID": "44726",
           "INCLINATION": "53.0510", "RA_OF_ASC_NODE": "327.8600", "MEAN_MOTION": "15.06420000"},
          {"OBJECT_NAME": "STARLINK-1033", "NORAD_CAT_ID": "44739",
           "INCLINATION": "53.0520", "RA_OF_ASC_NODE": "327.8700", "MEAN_MOTION": "15.06418000"}
        ]
        """;

            int count = parseSatelliteData(backupData);
            return "Loaded " + count + " satellites from backup dataset (demo mode)";

        } catch (Exception e) {
            System.err.println("Backup data loading failed: " + e.getMessage());
            return "Error loading backup data";
        }
    }
}