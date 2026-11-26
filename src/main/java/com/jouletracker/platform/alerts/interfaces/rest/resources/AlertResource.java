package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST response representation of an energy consumption alert (TS-14).
 */
@Schema(description = "Energy consumption alert resource")
public record AlertResource(
        @Schema(description = "Unique alert identifier", example = "1")
        Long id,

        @Schema(description = "Owner user identifier", example = "1")
        Long userId,

        @Schema(description = "Related IoT sensor identifier", example = "10")
        Long sensorId,

        @Schema(description = "Related electrical device identifier", example = "5")
        Long deviceId,

        @Schema(description = "Alert title", example = "Límite diario superado: Sensor Refrigeradora")
        String title,

        @Schema(description = "Detailed description of the consumption alert")
        String description,

        @Schema(description = "Alias message for frontend compatibility")
        String message,

        @Schema(description = "Human-readable relative time label", example = "Ahora")
        String timeAgo,

        @Schema(description = "ISO-8601 timestamp when the alert was triggered")
        String timestamp,

        @Schema(description = "Alert severity code (info, warning, critical)", example = "warning")
        String severity,

        @Schema(description = "Target route path in frontend", example = "/dispositivos")
        String actionUrl,

        @Schema(description = "Whether the alert has been marked as read", example = "false")
        boolean read,

        @Schema(description = "Alert status label (Activa or Leída)", example = "Activa")
        String status
) {
}
