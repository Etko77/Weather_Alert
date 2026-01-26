package org.example.weather_alert.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request object for creating a new weather alert")
public class CreateAlertRequest {

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    @Schema(description = "Detailed description of the weather alert",
            example = "Heavy snowfall expected. Road conditions may be hazardous.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotBlank(message = "Location name is required")
    @Size(min = 2, max = 255, message = "Location name must be between 2 and 255 characters")
    @Schema(description = "Name of the location for the alert",
            example = "Sofia",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String locationName;

    @NotNull(message = "Severity level is required")
    @Schema(description = "Severity level of the alert",
            example = "HIGH",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private SeverityLevel severityLevel;
}

