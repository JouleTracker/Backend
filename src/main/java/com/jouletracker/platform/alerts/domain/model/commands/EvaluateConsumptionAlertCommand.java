package com.jouletracker.platform.alerts.domain.model.commands;

/**
 * Command to evaluate a telemetry measurement against configured consumption and power limits (TS-14).
 */
public record EvaluateConsumptionAlertCommand(
        Long userId,
        Long sensorId,
        Long deviceId,
        String deviceName,
        Double powerKw,
        Double energyKwh
) {
}
