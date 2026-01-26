package org.example.weather_alert.exception;

public class GeocodingException extends RuntimeException {

    private final String locationName;

    public GeocodingException(String message) {
        super(message);
        this.locationName = null;
    }

    public GeocodingException(String message, String locationName) {
        super(message);
        this.locationName = locationName;
    }

    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
        this.locationName = null;
    }

    public GeocodingException(String message, String locationName, Throwable cause) {
        super(message, cause);
        this.locationName = locationName;
    }

    public String getLocationName() {
        return locationName;
    }
}
