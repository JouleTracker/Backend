package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.queryservices.AlertQueryService;
import com.jouletracker.platform.alerts.domain.model.queries.GetAlertSummaryByUserIdQuery;
import com.jouletracker.platform.alerts.interfaces.rest.resources.AlertSummaryResource;
import com.jouletracker.platform.alerts.interfaces.rest.transform.AlertResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for aggregated alert KPI summaries.
 */
@RestController
@RequestMapping(value = {"/api/v1/alert-summaries", "/alert-summaries"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Alert Summaries", description = "Aggregated alert KPIs for dashboard widgets")
public class AlertSummariesController {

    private final AlertQueryService alertQueryService;

    public AlertSummariesController(AlertQueryService alertQueryService) {
        this.alertQueryService = alertQueryService;
    }

    @GetMapping
    @Operation(summary = "Get alert summaries", description = "Retrieves aggregated alert KPIs for the specified user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert summary retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlertSummaryResource.class))))
    })
    public ResponseEntity<List<AlertSummaryResource>> getAlertSummaries(
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        var summary = alertQueryService.handle(new GetAlertSummaryByUserIdQuery(userId));
        return ResponseEntity.ok(List.of(AlertResourceAssembler.toSummaryResourceFromEntity(summary)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert summary by user ID", description = "Retrieves aggregated alert KPIs by user identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AlertSummaryResource.class)))
    })
    public ResponseEntity<AlertSummaryResource> getAlertSummaryById(@PathVariable Long id) {
        var summary = alertQueryService.handle(new GetAlertSummaryByUserIdQuery(id));
        return ResponseEntity.ok(AlertResourceAssembler.toSummaryResourceFromEntity(summary));
    }
}
