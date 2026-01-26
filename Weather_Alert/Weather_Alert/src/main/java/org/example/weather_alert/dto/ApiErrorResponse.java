package org.example.weather_alert.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response object")
public class ApiErrorResponse {

    @Schema(description = "Timestamp when the error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type/category", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message",
            example = "Validation failed for one or more fields")
    private String message;

    @Schema(description = "API path where the error occurred",
            example = "/api/alerts")
    private String path;

    @Schema(description = "List of validation errors (for 400 Bad Request)")
    private List<ValidationError> validationErrors;

    @Schema(description = "Additional error details")
    private Map<String, Object> details;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Field validation error details")
    public static class ValidationError {

        @Schema(description = "Name of the field with error", example = "description")
        private String field;

        @Schema(description = "Rejected value", example = "")
        private Object rejectedValue;

        @Schema(description = "Validation error message",
                example = "Description is required")
        private String message;
    }

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
