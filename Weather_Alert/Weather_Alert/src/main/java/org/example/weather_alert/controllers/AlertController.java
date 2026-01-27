package org.example.weather_alert.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.weather_alert.dto.AlertResponse;
import org.example.weather_alert.dto.ApiErrorResponse;
import org.example.weather_alert.dto.CreateAlertRequest;
import org.example.weather_alert.dto.UpdateAlertRequest;
import org.example.weather_alert.services.AlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Weather Alerts", description = "CRUD operations for weather alerts")
@SecurityRequirement(name = "bearerAuth")  // Swagger: requires authentication
public class AlertController {

    private final AlertService alertService;

    @Operation(
            summary = "Create a new weather alert",
            description = "Creates a new alert and triggers async geo-tagging. Returns immediately without waiting for coordinates."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Alert created successfully",
                    content = @Content(schema = @Schema(implementation = AlertResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized (requires ADMIN role)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertResponse> createAlert(
            @Valid @RequestBody CreateAlertRequest request) {

        log.info("POST /api/alerts - Creating alert for location: {}", request.getLocationName());

        AlertResponse response = alertService.createAlert(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all weather alerts",
            description = "Returns a list of all weather alerts in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alerts retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlertResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<AlertResponse>> getAllAlerts() {
        log.info("GET /api/alerts - Fetching all alerts");

        List<AlertResponse> alerts = alertService.getAllAlerts();

        return ResponseEntity.ok(alerts);
    }

    @Operation(
            summary = "Get alert by ID",
            description = "Returns a specific weather alert by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alert found",
                    content = @Content(schema = @Schema(implementation = AlertResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<AlertResponse> getAlertById(
            @Parameter(description = "Alert ID", required = true)
            @PathVariable Long id) {

        log.info("GET /api/alerts/{} - Fetching alert", id);

        AlertResponse response = alertService.getAlertById(id);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update an existing alert",
            description = "Updates alert fields. If location changes, geo-tagging is re-triggered."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alert updated successfully",
                    content = @Content(schema = @Schema(implementation = AlertResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized (requires ADMIN role)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertResponse> updateAlert(
            @Parameter(description = "Alert ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateAlertRequest request) {

        log.info("PUT /api/alerts/{} - Updating alert", id);

        AlertResponse response = alertService.updateAlert(id, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete an alert",
            description = "Permanently deletes a weather alert"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Alert deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized (requires ADMIN role)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAlert(
            @Parameter(description = "Alert ID", required = true)
            @PathVariable Long id) {

        log.info("DELETE /api/alerts/{} - Deleting alert", id);

        alertService.deleteAlert(id);

        return ResponseEntity.noContent().build();
    }
}
