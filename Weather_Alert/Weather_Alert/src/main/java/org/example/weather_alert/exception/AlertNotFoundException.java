package org.example.weather_alert.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(Long id) {
        super("Alert not found with id: " + id);
    }

    public AlertNotFoundException(String message) {
        super(message);
    }
}
