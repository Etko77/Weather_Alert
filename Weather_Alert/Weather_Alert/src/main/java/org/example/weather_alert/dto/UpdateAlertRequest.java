package org.example.weather_alert.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.weather_alert.enums.SeverityLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing weather alert")
public class UpdateAlertRequest {

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    @Schema(description = "Updated description of the weather alert",
            example = "Updated: Heavy snowfall and strong winds expected.")
    private String description;

    @Size(min = 2, max = 255, message = "Location name must be between 2 and 255 characters")
    @Schema(description = "Updated location name (will trigger new geo-tagging)",
            example = "Berlin Central Park")
    private String locationName;

    @Schema(description = "Updated severity level",
            example = "MEDIUM")
    private SeverityLevel severityLevel;
}
