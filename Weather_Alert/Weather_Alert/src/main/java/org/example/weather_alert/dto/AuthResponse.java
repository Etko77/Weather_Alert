package org.example.weather_alert.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing authentication token and user details")
public class AuthResponse {

    @Schema(description = "JWT access token for API authentication",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration time in milliseconds", example = "86400000")
    private Long expiresIn;

    @Schema(description = "Authenticated username", example = "admin")
    private String username;

    @Schema(description = "User's email address", example = "admin@weatheralert.com")
    private String email;

    @Schema(description = "User's roles", example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]")
    private Set<String> roles;
}
