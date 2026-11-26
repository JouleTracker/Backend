package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST response representation of aggregated alert KPIs.
 */
@Schema(description = "Aggregated alert KPI summary")
public record AlertSummaryResource(
        @Schema(description = "Owner user identifier", example = "1")
        Long userId,

        @Schema(description = "Total alerts count", example = "4")
        long totalCount,

        @Schema(description = "Active (unread) alerts count", example = "3")
        long activeCount,

        @Schema(description = "Resolved (read) alerts count", example = "1")
        long resolvedCount
) {
}
