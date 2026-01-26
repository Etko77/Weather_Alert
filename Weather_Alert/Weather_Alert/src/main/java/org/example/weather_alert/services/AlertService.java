package org.example.weather_alert.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.weather_alert.dto.AlertResponse;
import org.example.weather_alert.dto.CreateAlertRequest;
import org.example.weather_alert.dto.UpdateAlertRequest;
import org.example.weather_alert.entities.Alert;
import org.example.weather_alert.entities.User;
import org.example.weather_alert.exception.AlertNotFoundException;
import org.example.weather_alert.mapper.AlertMapper;
import org.example.weather_alert.repositories.AlertRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    private final GeocodingService geocodingService;

    private final AlertMapper alertMapper;

    @Transactional
    public AlertResponse createAlert(CreateAlertRequest request) {
        log.info("Creating new alert for location: {}", request.getLocationName());

        // Get current authenticated user
        User currentUser = getCurrentUser();

        // Map request to entity
        Alert alert = alertMapper.toEntity(request, currentUser);

        // Save alert with PENDING geo-tagging status
        Alert savedAlert = alertRepository.save(alert);
        log.debug("Alert saved with ID: {}", savedAlert.getId());

        // Trigger async geo-tagging (non-blocking)
        // This call returns immediately - geo-tagging happens in background
        geocodingService.enrichAlertWithCoordinates(savedAlert.getId());
        log.debug("Async geo-tagging triggered for alert ID: {}", savedAlert.getId());

        // Return response immediately (don't wait for geo-tagging)
        return alertMapper.toResponse(savedAlert);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAllAlerts() {
        log.debug("Fetching all alerts");

        return alertRepository.findAll()
                .stream()
                .map(alertMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlertResponse getAlertById(Long id) {
        log.debug("Fetching alert by ID: {}", id);

        Alert alert = findAlertOrThrow(id);
        return alertMapper.toResponse(alert);
    }

    @Transactional
    public AlertResponse updateAlert(Long id, UpdateAlertRequest request) {
        log.info("Updating alert ID: {}", id);

        Alert alert = findAlertOrThrow(id);

        // Update entity and check if location changed
        boolean locationChanged = alertMapper.updateEntity(alert, request);

        // Save updated alert
        Alert updatedAlert = alertRepository.save(alert);

        // Re-trigger geo-tagging if location changed
        if (locationChanged) {
            log.debug("Location changed, re-triggering geo-tagging for alert ID: {}", id);
            geocodingService.enrichAlertWithCoordinates(updatedAlert.getId());
        }

        return alertMapper.toResponse(updatedAlert);
    }

    @Transactional
    public void deleteAlert(Long id) {
        log.info("Deleting alert ID: {}", id);

        if (!alertRepository.existsById(id)) {
            throw new AlertNotFoundException(id);
        }

        alertRepository.deleteById(id);
        log.debug("Alert deleted: {}", id);
    }

    private Alert findAlertOrThrow(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException(id));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }

        log.warn("No authenticated user found");
        return null;
    }
}
