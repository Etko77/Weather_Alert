package org.example.weather_alert;

import org.example.weather_alert.dto.AlertResponse;
import org.example.weather_alert.dto.CreateAlertRequest;
import org.example.weather_alert.dto.UpdateAlertRequest;
import org.example.weather_alert.entities.Alert;
import org.example.weather_alert.entities.User;
import org.example.weather_alert.enums.GeoTaggingStatus;
import org.example.weather_alert.enums.SeverityLevel;
import org.example.weather_alert.exception.AlertNotFoundException;
import org.example.weather_alert.mapper.AlertMapper;
import org.example.weather_alert.repositories.AlertRepository;
import org.example.weather_alert.services.AlertService;
import org.example.weather_alert.services.GeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService Unit Tests")
class AlertServiceTest {

    // Mocked dependencies (these would normally be injected by Spring)
    @Mock
    private AlertRepository alertRepository;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private AlertMapper alertMapper;

    // Class under test - mocks are injected here
    @InjectMocks
    private AlertService alertService;

    // Test data
    private Alert testAlert;
    private AlertResponse testAlertResponse;
    private CreateAlertRequest createRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@test.com")
                .build();

        // Set up test alert
        testAlert = Alert.builder()
                .id(1L)
                .description("Test alert description")
                .locationName("Sofia")
                .severityLevel(SeverityLevel.HIGH)
                .geoTaggingStatus(GeoTaggingStatus.PENDING)
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Set up test response
        testAlertResponse = AlertResponse.builder()
                .id(1L)
                .description("Test alert description")
                .locationName("Sofia")
                .severityLevel(SeverityLevel.HIGH)
                .geoTaggingStatus(GeoTaggingStatus.PENDING)
                .createdBy("admin")
                .build();

        // Set up create request
        createRequest = CreateAlertRequest.builder()
                .description("Test alert description")
                .locationName("Sofia")
                .severityLevel(SeverityLevel.HIGH)
                .build();
    }

    // ==========================================
    // CREATE ALERT TESTS
    // ==========================================

    @Nested
    @DisplayName("createAlert")
    class CreateAlertTests {

        @BeforeEach
        void setUpSecurityContext() {
            // Mock security context for getting current user
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            SecurityContextHolder.setContext(securityContext);
        }

        @Test
        @DisplayName("should create alert and trigger async geo-tagging")
        void shouldCreateAlertSuccessfully() {
            // Arrange
            when(alertMapper.toEntity(any(CreateAlertRequest.class), any(User.class)))
                    .thenReturn(testAlert);
            when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);
            when(alertMapper.toResponse(any(Alert.class))).thenReturn(testAlertResponse);
            doNothing().when(geocodingService).enrichAlertWithCoordinates(anyLong());

            // Act
            AlertResponse result = alertService.createAlert(createRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getLocationName()).isEqualTo("Sofia");

            // Verify interactions
            verify(alertRepository, times(1)).save(any(Alert.class));
            verify(geocodingService, times(1)).enrichAlertWithCoordinates(1L);
        }

        @Test
        @DisplayName("should save alert with PENDING geo-tagging status")
        void shouldSaveWithPendingStatus() {
            // Arrange
            when(alertMapper.toEntity(any(CreateAlertRequest.class), any(User.class)))
                    .thenReturn(testAlert);
            when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);
            when(alertMapper.toResponse(any(Alert.class))).thenReturn(testAlertResponse);

            // Act
            AlertResponse result = alertService.createAlert(createRequest);

            // Assert
            assertThat(result.getGeoTaggingStatus()).isEqualTo(GeoTaggingStatus.PENDING);
        }
    }

    // ==========================================
    // GET ALL ALERTS TESTS
    // ==========================================

    @Nested
    @DisplayName("getAllAlerts")
    class GetAllAlertsTests {

        @Test
        @DisplayName("should return all alerts")
        void shouldReturnAllAlerts() {
            // Arrange
            Alert alert2 = Alert.builder()
                    .id(2L)
                    .description("Second alert")
                    .locationName("Berlin")
                    .severityLevel(SeverityLevel.LOW)
                    .build();

            AlertResponse response2 = AlertResponse.builder()
                    .id(2L)
                    .description("Second alert")
                    .locationName("Berlin")
                    .severityLevel(SeverityLevel.LOW)
                    .build();

            when(alertRepository.findAll()).thenReturn(Arrays.asList(testAlert, alert2));
            when(alertMapper.toResponse(testAlert)).thenReturn(testAlertResponse);
            when(alertMapper.toResponse(alert2)).thenReturn(response2);

            // Act
            List<AlertResponse> results = alertService.getAllAlerts();

            // Assert
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getLocationName()).isEqualTo("Sofia");
            assertThat(results.get(1).getLocationName()).isEqualTo("Berlin");
        }

        @Test
        @DisplayName("should return empty list when no alerts exist")
        void shouldReturnEmptyList() {
            // Arrange
            when(alertRepository.findAll()).thenReturn(List.of());

            // Act
            List<AlertResponse> results = alertService.getAllAlerts();

            // Assert
            assertThat(results).isEmpty();
        }
    }

    // ==========================================
    // GET ALERT BY ID TESTS
    // ==========================================

    @Nested
    @DisplayName("getAlertById")
    class GetAlertByIdTests {

        @Test
        @DisplayName("should return alert when found")
        void shouldReturnAlertWhenFound() {
            // Arrange
            when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
            when(alertMapper.toResponse(testAlert)).thenReturn(testAlertResponse);

            // Act
            AlertResponse result = alertService.getAlertById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getLocationName()).isEqualTo("Sofia");
        }

        @Test
        @DisplayName("should throw AlertNotFoundException when not found")
        void shouldThrowExceptionWhenNotFound() {
            // Arrange
            when(alertRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> alertService.getAlertById(999L))
                    .isInstanceOf(AlertNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // ==========================================
    // UPDATE ALERT TESTS
    // ==========================================

    @Nested
    @DisplayName("updateAlert")
    class UpdateAlertTests {

        @Test
        @DisplayName("should update alert without re-triggering geo-tagging when location unchanged")
        void shouldUpdateWithoutGeoTagging() {
            // Arrange
            UpdateAlertRequest updateRequest = UpdateAlertRequest.builder()
                    .description("Updated description")
                    .severityLevel(SeverityLevel.MEDIUM)
                    .build();

            when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
            when(alertMapper.updateEntity(any(Alert.class), any(UpdateAlertRequest.class)))
                    .thenReturn(false); // Location not changed
            when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);
            when(alertMapper.toResponse(any(Alert.class))).thenReturn(testAlertResponse);

            // Act
            AlertResponse result = alertService.updateAlert(1L, updateRequest);

            // Assert
            assertThat(result).isNotNull();
            verify(geocodingService, never()).enrichAlertWithCoordinates(anyLong());
        }

        @Test
        @DisplayName("should re-trigger geo-tagging when location changed")
        void shouldRetriggerGeoTaggingWhenLocationChanged() {
            // Arrange
            UpdateAlertRequest updateRequest = UpdateAlertRequest.builder()
                    .locationName("New Location")
                    .build();

            when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
            when(alertMapper.updateEntity(any(Alert.class), any(UpdateAlertRequest.class)))
                    .thenReturn(true); // Location changed
            when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);
            when(alertMapper.toResponse(any(Alert.class))).thenReturn(testAlertResponse);

            // Act
            alertService.updateAlert(1L, updateRequest);

            // Assert
            verify(geocodingService, times(1)).enrichAlertWithCoordinates(1L);
        }

        @Test
        @DisplayName("should throw AlertNotFoundException when updating non-existent alert")
        void shouldThrowExceptionWhenUpdatingNonExistent() {
            // Arrange
            UpdateAlertRequest updateRequest = UpdateAlertRequest.builder()
                    .description("Updated")
                    .build();
            when(alertRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> alertService.updateAlert(999L, updateRequest))
                    .isInstanceOf(AlertNotFoundException.class);
        }
    }

    // ==========================================
    // DELETE ALERT TESTS
    // ==========================================

    @Nested
    @DisplayName("deleteAlert")
    class DeleteAlertTests {

        @Test
        @DisplayName("should delete alert when exists")
        void shouldDeleteAlertWhenExists() {
            // Arrange
            when(alertRepository.existsById(1L)).thenReturn(true);
            doNothing().when(alertRepository).deleteById(1L);

            // Act
            alertService.deleteAlert(1L);

            // Assert
            verify(alertRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("should throw AlertNotFoundException when deleting non-existent alert")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // Arrange
            when(alertRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> alertService.deleteAlert(999L))
                    .isInstanceOf(AlertNotFoundException.class);

            verify(alertRepository, never()).deleteById(anyLong());
        }
    }
}
