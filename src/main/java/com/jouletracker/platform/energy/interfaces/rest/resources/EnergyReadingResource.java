package com.jouletracker.platform.energy.interfaces.rest.resources;

import java.time.LocalDateTime;

public record EnergyReadingResource(
        Long id,
        String label,
        LocalDateTime timestamp,
        Double kwh,
        Double powerKw,
        Double cost,
        Long deviceId,
        String deviceName
) {
}