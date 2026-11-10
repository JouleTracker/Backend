package com.jouletracker.platform.devices.domain.model.valueobjects;

/**
 * Represents the connectivity and telemetry transmission state of an IoT sensor.
 */
public enum SensorNetworkStatus {
    ONLINE("En línea"),
    OFFLINE("Desconectado");

    private final String label;

    SensorNetworkStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static SensorNetworkStatus fromString(String text) {
        if (text == null || text.isBlank()) {
            return OFFLINE;
        }
        for (SensorNetworkStatus status : SensorNetworkStatus.values()) {
            if (status.name().equalsIgnoreCase(text) || status.label.equalsIgnoreCase(text)) {
                return status;
            }
        }
        return OFFLINE;
    }
}
