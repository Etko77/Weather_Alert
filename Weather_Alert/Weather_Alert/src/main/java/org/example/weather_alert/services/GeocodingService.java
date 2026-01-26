package org.example.weather_alert.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.weather_alert.entities.Alert;
import org.example.weather_alert.enums.GeoTaggingStatus;
import org.example.weather_alert.exception.GeocodingException;
import org.example.weather_alert.repositories.AlertRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    // Dependencies injected via constructor
    private final AlertRepository alertRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    // Configuration from application.yml
    @Value("${geocoding.api.base-url:https://nominatim.openstreetmap.org}")
    private String baseUrl;

    @Value("${geocoding.api.user-agent:WeatherAlertService/1.0}")
    private String userAgent;

    @Value("${geocoding.api.rate-limit-ms:1000}")
    private int rateLimitMs;

    @Async("taskExecutor")  // Use our custom executor from AsyncConfig
    @Transactional
    public void enrichAlertWithCoordinates(Long alertId) {
        log.info("Starting async geo-tagging for alert ID: {}", alertId);

        try {
            // Fetch the alert from database
            Alert alert = alertRepository.findById(alertId)
                    .orElseThrow(() -> new GeocodingException("Alert not found: " + alertId));

            String locationName = alert.getLocationName();
            log.debug("Geocoding location: {}", locationName);

            // Call Nominatim API
            GeocodingResult result = fetchCoordinates(locationName);

            if (result != null) {
                // Update alert with coordinates
                alert.setLatitude(result.latitude());
                alert.setLongitude(result.longitude());
                alert.setGeoTaggingStatus(GeoTaggingStatus.SUCCESS);
                alert.setGeoTaggingError(null);

                alertRepository.save(alert);
                log.info("Geo-tagging successful for alert {}: lat={}, lon={}",
                        alertId, result.latitude(), result.longitude());
            } else {
                // No coordinates found
                handleGeocodingFailure(alert, "No coordinates found for location: " + locationName);
            }

        } catch (GeocodingException e) {
            log.error("Geocoding failed for alert {}: {}", alertId, e.getMessage());
            handleGeocodingFailureById(alertId, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during geo-tagging for alert {}: {}", alertId, e.getMessage(), e);
            handleGeocodingFailureById(alertId, "Unexpected error: " + e.getMessage());
        }
    }

    private GeocodingResult fetchCoordinates(String locationName) {
        try {
            // Rate limiting - Nominatim requires max 1 request per second
            Thread.sleep(rateLimitMs);

            String encodedLocation = URLEncoder.encode(locationName, StandardCharsets.UTF_8);
            String url = String.format("%s/search?q=%s&format=json&limit=1", baseUrl, encodedLocation);

            log.debug("Calling Nominatim API: {}", url);

            // Create WebClient and make request
            WebClient webClient = webClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader("User-Agent", userAgent)
                    .build();

            String response = webClient.get()
                    .uri("/search?q={location}&format=json&limit=1", locationName)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.debug("Nominatim response: {}", response);

            // Parse response
            return parseNominatimResponse(response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeocodingException("Geocoding interrupted", locationName, e);
        } catch (Exception e) {
            throw new GeocodingException("Failed to fetch coordinates: " + e.getMessage(), locationName, e);
        }
    }

    private GeocodingResult parseNominatimResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            if (root.isArray() && !root.isEmpty()) {
                JsonNode firstResult = root.get(0);
                double lat = firstResult.get("lat").asDouble();
                double lon = firstResult.get("lon").asDouble();
                return new GeocodingResult(lat, lon);
            }

            return null;  // No results found

        } catch (Exception e) {
            log.error("Failed to parse Nominatim response: {}", e.getMessage());
            throw new GeocodingException("Failed to parse geocoding response", e);
        }
    }

    private void handleGeocodingFailure(Alert alert, String errorMessage) {
        alert.setGeoTaggingStatus(GeoTaggingStatus.FAILED);
        alert.setGeoTaggingError(truncateMessage(errorMessage, 500));
        alertRepository.save(alert);
        log.warn("Geo-tagging failed for alert {}: {}", alert.getId(), errorMessage);
    }

    private void handleGeocodingFailureById(Long alertId, String errorMessage) {
        alertRepository.findById(alertId).ifPresent(alert ->
                handleGeocodingFailure(alert, errorMessage)
        );
    }

    private String truncateMessage(String message, int maxLength) {
        if (message == null) return null;
        return message.length() > maxLength ? message.substring(0, maxLength) : message;
    }

    private record GeocodingResult(double latitude, double longitude) {}
}