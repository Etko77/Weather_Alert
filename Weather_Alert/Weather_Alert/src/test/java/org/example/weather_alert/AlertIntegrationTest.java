package org.example.weather_alert;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.weather_alert.dto.AuthResponse;
import org.example.weather_alert.dto.CreateAlertRequest;
import org.example.weather_alert.dto.LoginRequest;
import org.example.weather_alert.enums.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Alert API.
 *
 * These tests verify the entire request flow:
 * Controller -> Service -> Repository -> Database
 *
 * DI IN INTEGRATION TESTS:
 * - @SpringBootTest loads the full application context
 * - @Autowired injects real beans (not mocks)
 * - Tests verify that DI is configured correctly
 * - If DI fails, the application context won't load
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Alert API Integration Tests")
class AlertIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // Get admin token
        adminToken = getAuthToken("admin", "admin123");
        // Get user token
        userToken = getAuthToken("user1", "user123");
    }

    private String getAuthToken(String username, String password) throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        return response.getAccessToken();
    }

    // ==========================================
    // AUTHENTICATION TESTS
    // ==========================================

    @Nested
    @DisplayName("Authentication")
    class AuthenticationTests {

        @Test
        @DisplayName("should login successfully with valid credentials")
        void shouldLoginSuccessfully() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("admin")
                    .password("admin123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.username").value("admin"))
                    .andExpect(jsonPath("$.roles", hasItem("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("should reject invalid credentials")
        void shouldRejectInvalidCredentials() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("admin")
                    .password("wrongpassword")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==========================================
    // GET ALERTS TESTS
    // ==========================================

    @Nested
    @DisplayName("GET /api/alerts")
    class GetAlertsTests {

        @Test
        @DisplayName("should return all alerts for admin")
        void shouldReturnAllAlertsForAdmin() throws Exception {
            mockMvc.perform(get("/api/alerts")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(greaterThan(0)));
        }

        @Test
        @DisplayName("should return all alerts for user (read access)")
        void shouldReturnAllAlertsForUser() throws Exception {
            mockMvc.perform(get("/api/alerts")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("should reject unauthenticated request")
        void shouldRejectUnauthenticatedRequest() throws Exception {
            mockMvc.perform(get("/api/alerts"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==========================================
    // GET ALERT BY ID TESTS
    // ==========================================

    @Nested
    @DisplayName("GET /api/alerts/{id}")
    class GetAlertByIdTests {

        @Test
        @DisplayName("should return alert by ID")
        void shouldReturnAlertById() throws Exception {
            mockMvc.perform(get("/api/alerts/1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.locationName").isNotEmpty())
                    .andExpect(jsonPath("$.severityLevel").isNotEmpty());
        }

        @Test
        @DisplayName("should return 404 for non-existent alert")
        void shouldReturn404ForNonExistent() throws Exception {
            mockMvc.perform(get("/api/alerts/99999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("99999")));
        }
    }

    // ==========================================
    // CREATE ALERT TESTS
    // ==========================================

    @Nested
    @DisplayName("POST /api/alerts")
    class CreateAlertTests {

        @Test
        @DisplayName("should create alert as admin")
        void shouldCreateAlertAsAdmin() throws Exception {
            CreateAlertRequest request = CreateAlertRequest.builder()
                    .description("Test alert for integration test - severe weather expected")
                    .locationName("Test City")
                    .severityLevel(SeverityLevel.HIGH)
                    .build();

            mockMvc.perform(post("/api/alerts")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.locationName").value("Test City"))
                    .andExpect(jsonPath("$.severityLevel").value("HIGH"))
                    .andExpect(jsonPath("$.geoTaggingStatus").value("PENDING"));
        }

        @Test
        @DisplayName("should reject create request from user (not admin)")
        void shouldRejectCreateFromUser() throws Exception {
            CreateAlertRequest request = CreateAlertRequest.builder()
                    .description("Test alert that should be rejected")
                    .locationName("Test City")
                    .severityLevel(SeverityLevel.LOW)
                    .build();

            mockMvc.perform(post("/api/alerts")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should reject invalid request (validation)")
        void shouldRejectInvalidRequest() throws Exception {
            CreateAlertRequest request = CreateAlertRequest.builder()
                    .description("Short")  // Too short
                    .locationName("")  // Empty
                    .severityLevel(null)  // Null
                    .build();

            mockMvc.perform(post("/api/alerts")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").isArray())
                    .andExpect(jsonPath("$.validationErrors.length()").value(greaterThan(0)));
        }
    }

    // ==========================================
    // UPDATE ALERT TESTS
    // ==========================================

    @Nested
    @DisplayName("PUT /api/alerts/{id}")
    class UpdateAlertTests {

        @Test
        @DisplayName("should update alert as admin")
        void shouldUpdateAlertAsAdmin() throws Exception {
            String updateJson = """
                {
                    "description": "Updated description for integration test",
                    "severityLevel": "MEDIUM"
                }
                """;

            mockMvc.perform(put("/api/alerts/1")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("should reject update from user (not admin)")
        void shouldRejectUpdateFromUser() throws Exception {
            String updateJson = """
                {
                    "description": "This update should be rejected"
                }
                """;

            mockMvc.perform(put("/api/alerts/1")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // DELETE ALERT TESTS
    // ==========================================

    @Nested
    @DisplayName("DELETE /api/alerts/{id}")
    class DeleteAlertTests {

        @Test
        @DisplayName("should reject delete from user (not admin)")
        void shouldRejectDeleteFromUser() throws Exception {
            mockMvc.perform(delete("/api/alerts/1")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent alert")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            mockMvc.perform(delete("/api/alerts/99999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
}
