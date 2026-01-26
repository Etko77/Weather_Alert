package org.example.weather_alert.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user authentication")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username for authentication",
            example = "admin",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password for authentication",
            example = "admin123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
