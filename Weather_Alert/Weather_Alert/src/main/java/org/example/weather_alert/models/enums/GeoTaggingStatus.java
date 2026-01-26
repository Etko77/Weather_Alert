package org.example.weather_alert.models.enums;

public enum GeoTaggingStatus {
    /**
     * Geo-tagging request has been submitted but not yet processed
     */
    PENDING,

    /**
     * Coordinates were successfully retrieved from the geocoding service
     */
    SUCCESS,

    /**
     * Geo-tagging failed (location not found, API error, etc.)
     */
    FAILED
}
