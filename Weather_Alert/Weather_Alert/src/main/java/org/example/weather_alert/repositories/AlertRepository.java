package org.example.weather_alert.repositories;

import org.example.weather_alert.entities.Alert;
import org.example.weather_alert.enums.GeoTaggingStatus;
import org.example.weather_alert.enums.SeverityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findBySeverityLevel(SeverityLevel severityLevel);

    List<Alert> findByGeoTaggingStatus(GeoTaggingStatus status);

    @Query("SELECT a FROM Alert a WHERE LOWER(a.locationName) LIKE LOWER(CONCAT('%', :locationName, '%'))")
    List<Alert> findByLocationNameContainingIgnoreCase(@Param("locationName") String locationName);

    List<Alert> findByCreatedById(Long userId);

    @Query("SELECT a FROM Alert a WHERE a.latitude IS NOT NULL AND a.longitude IS NOT NULL")
    List<Alert> findGeoTaggedAlerts();

    default List<Alert> findPendingGeoTagging() {
        return findByGeoTaggingStatus(GeoTaggingStatus.PENDING);
    }
}
