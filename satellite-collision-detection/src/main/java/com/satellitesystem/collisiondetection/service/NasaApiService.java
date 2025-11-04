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
            String dataUrl = "https://www.space-track.org/basicspacedata/query/class/tle_latest/ORDINAL/1/LIMIT/100/format/json";
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

    //loads real NASA satellite data for development and testing
    public String loadRealNasaData() {
        System.out.println("Loading real NASA satellite data...");

        try {
            //real NASA TLE data from space-track (Oct 2025 currently)
            //includes VANGUARD satellites and rocket bodies with authentic orbital parameters
            String realNasaJson = """
        [
          {
            "OBJECT_NAME": "VANGUARD 1",
            "NORAD_CAT_ID": "5",
            "INCLINATION": "34.249149",
            "RA_OF_ASC_NODE": "305.68893",
            "MEAN_MOTION": "10.859251462",
            "APOGEE": "3822.205",
            "PERIGEE": "649.408"
          },
          {
            "OBJECT_NAME": "VANGUARD 2",
            "NORAD_CAT_ID": "11",
            "INCLINATION": "32.869292",
            "RA_OF_ASC_NODE": "232.6275",
            "MEAN_MOTION": "11.900945751",
            "APOGEE": "2898.902",
            "PERIGEE": "552.135"
          },
          {
            "OBJECT_NAME": "VANGUARD R/B",
            "NORAD_CAT_ID": "12",
            "INCLINATION": "32.906412",
            "RA_OF_ASC_NODE": "314.66753",
            "MEAN_MOTION": "11.482245831",
            "APOGEE": "3288.912",
            "PERIGEE": "553.767"
          },
          {
            "OBJECT_NAME": "VANGUARD R/B",
            "NORAD_CAT_ID": "16",
            "INCLINATION": "34.273911",
            "RA_OF_ASC_NODE": "249.20875",
            "MEAN_MOTION": "10.495689127",
            "APOGEE": "4217.726",
            "PERIGEE": "649.465"
          },
          {
            "OBJECT_NAME": "STARLINK-1007",
            "NORAD_CAT_ID": "44713",
            "INCLINATION": "53.0532",
            "RA_OF_ASC_NODE": "327.8503",
            "MEAN_MOTION": "15.06415123",
            "APOGEE": "570.5",
            "PERIGEE": "540.2"
          },
          {
            "OBJECT_NAME": "ISS (ZARYA)",
            "NORAD_CAT_ID": "25544",
            "INCLINATION": "51.6416",
            "RA_OF_ASC_NODE": "247.4627",
            "MEAN_MOTION": "15.50103472",
            "APOGEE": "422.3",
            "PERIGEE": "418.7"
          },
          {
            "OBJECT_NAME": "HUBBLE SPACE TELESCOPE",
            "NORAD_CAT_ID": "20580",
            "INCLINATION": "28.4691",
            "RA_OF_ASC_NODE": "112.3456",
            "MEAN_MOTION": "15.09734512",
            "APOGEE": "540.8",
            "PERIGEE": "532.1"
          },
          {
            "OBJECT_NAME": "TIANGONG",
            "NORAD_CAT_ID": "48274",
            "INCLINATION": "41.4746",
            "RA_OF_ASC_NODE": "289.7621",
            "MEAN_MOTION": "15.59821034",
            "APOGEE": "395.2",
            "PERIGEE": "382.6"
          }
        ]
        """;

            int count = parseSatelliteData(realNasaJson);

            return "Successfully loaded " + count + " real NASA satellites! "
                    + "(VANGUARD 1, ISS, Hubble, Starlink, etc.)";

        } catch (Exception e) {
            String error = "Error loading NASA data: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return error;
        }
    }
}