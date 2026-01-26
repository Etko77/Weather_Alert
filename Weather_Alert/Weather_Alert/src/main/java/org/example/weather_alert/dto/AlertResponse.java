package org.example.weather_alert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.weather_alert.enums.GeoTaggingStatus;
import org.example.weather_alert.enums.SeverityLevel;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing weather alert details")
public class AlertResponse {

    @Schema(description = "Unique identifier of the alert", example = "1")
    private Long id;

    @Schema(description = "Detailed description of the alert",
            example = "Heavy snowfall expected. Road conditions may be hazardous.")
    private String description;

    @Schema(description = "Name of the location", example = "Sofia")
    private String locationName;

    @Schema(description = "Latitude coordinate (null if geo-tagging pending/failed)",
            example = "42.6977")
    private Double latitude;

    @Schema(description = "Longitude coordinate (null if geo-tagging pending/failed)",
            example = "23.3219")
    private Double longitude;

    @Schema(description = "Severity level of the alert", example = "HIGH")
    private SeverityLevel severityLevel;

    @Schema(description = "Status of geo-tagging operation", example = "SUCCESS")
    private GeoTaggingStatus geoTaggingStatus;

    @Schema(description = "Error message if geo-tagging failed",
            example = "Location not found")
    private String geoTaggingError;

    @Schema(description = "Username of the alert creator", example = "admin")
    private String createdBy;

    @Schema(description = "Timestamp when the alert was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the alert was last updated")
    private LocalDateTime updatedAt;
}
