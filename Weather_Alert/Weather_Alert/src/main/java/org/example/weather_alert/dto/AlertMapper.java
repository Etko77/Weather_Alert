package org.example.weather_alert.dto;

import org.example.weather_alert.enums.GeoTaggingStatus;
import org.example.weather_alert.models.Alert;
import org.example.weather_alert.models.User;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public Alert toEntity(CreateAlertRequest request, User createdBy) {
        return Alert.builder()
                .description(request.getDescription())
                .locationName(request.getLocationName())
                .severityLevel(request.getSeverityLevel())
                .geoTaggingStatus(GeoTaggingStatus.PENDING)
                .createdBy(createdBy)
                .build();
    }


    public AlertResponse toResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .description(alert.getDescription())
                .locationName(alert.getLocationName())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .severityLevel(alert.getSeverityLevel())
                .geoTaggingStatus(alert.getGeoTaggingStatus())
                .geoTaggingError(alert.getGeoTaggingError())
                .createdBy(alert.getCreatedBy() != null ? alert.getCreatedBy().getUsername() : null)
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }

    public boolean updateEntity(Alert alert, UpdateAlertRequest request) {
        boolean locationChanged = false;

        if (request.getDescription() != null) {
            alert.setDescription(request.getDescription());
        }

        if (request.getLocationName() != null &&
                !request.getLocationName().equals(alert.getLocationName())) {
            alert.setLocationName(request.getLocationName());
            alert.setGeoTaggingStatus(GeoTaggingStatus.PENDING);
            alert.setLatitude(null);
            alert.setLongitude(null);
            alert.setGeoTaggingError(null);
            locationChanged = true;
        }

        if (request.getSeverityLevel() != null) {
            alert.setSeverityLevel(request.getSeverityLevel());
        }

        return locationChanged;
    }
}
