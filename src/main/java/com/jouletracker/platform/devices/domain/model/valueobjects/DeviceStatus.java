package com.jouletracker.platform.devices.domain.model.valueobjects;

/**
 * Represents the operational state of an electrical device.
 */
public enum DeviceStatus {
    ONLINE("En línea"),
    STANDBY("En espera"),
    OFF("Apagado");

    private final String label;

    DeviceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static DeviceStatus fromString(String text) {
        if (text == null || text.isBlank()) {
            return OFF;
        }
        for (DeviceStatus status : DeviceStatus.values()) {
            if (status.name().equalsIgnoreCase(text) || status.label.equalsIgnoreCase(text)) {
                return status;
            }
        }
        if (text.equalsIgnoreCase("Encendido")) {
            return ONLINE;
        }
        if (text.equalsIgnoreCase("Standby")) {
            return STANDBY;
        }
        return OFF;
    }
}
