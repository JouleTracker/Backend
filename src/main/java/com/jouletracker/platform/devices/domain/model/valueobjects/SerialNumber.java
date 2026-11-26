package com.jouletracker.platform.devices.domain.model.valueobjects;

import java.util.Locale;

/**
 * Value object representing a unique physical serial number of an IoT sensor.
 */
public record SerialNumber(String value) {

    public SerialNumber {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor serial number cannot be null or empty");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
    }
}
