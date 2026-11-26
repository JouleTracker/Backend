package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.commandservices.AlertCommandService;
import com.jouletracker.platform.alerts.application.queryservices.AlertQueryService;
import com.jouletracker.platform.alerts.domain.model.commands.MarkAlertAsReadCommand;
import com.jouletracker.platform.alerts.domain.model.queries.GetAlertsByUserIdQuery;
import com.jouletracker.platform.alerts.interfaces.rest.resources.AlertResource;
import com.jouletracker.platform.alerts.interfaces.rest.transform.AlertResourceAssembler;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing consumption alert evaluation and acknowledgement endpoints (TS-14).
 */
@RestController
@RequestMapping(value = {"/api/v1/alerts", "/alerts"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Alerts", description = "Automated energy consumption alert evaluation and management endpoints")
public class AlertsController {

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;

    public AlertsController(AlertCommandService alertCommandService,
                            AlertQueryService alertQueryService) {
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
    }

    /**
     * Evaluates active threshold breaches and returns all alerts for the specified user (TS-14).
     */
    @GetMapping
    @Operation(
            summary = "Get user alerts",
            description = "Evaluates current sensor and device consumption limits and returns all consumption alerts for the user (TS-14)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlertResource.class))))
    })
    public ResponseEntity<List<AlertResource>> getAlerts(
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        var alerts = alertQueryService.handle(new GetAlertsByUserIdQuery(userId));
        var resources = alerts.stream()
                .map(AlertResourceAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Marks a specific consumption alert as read/resolved.
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark alert as read", description = "Marks a consumption alert as read and resolved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert marked as read",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> markAlertAsRead(
            @PathVariable @Parameter(description = "Alert ID", example = "1") Long id) {

        var result = alertCommandService.handle(new MarkAlertAsReadCommand(id));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AlertResourceAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    /**
     * Compatibility endpoint for PUT/PATCH /alerts/{id} to mark an alert as read.
     */
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Update alert status", description = "Marks a consumption alert as read via PUT/PATCH compatibility endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert updated successfully",
                    content = @Content(schema = @Schema(implementation = AlertResource.class))),
            @ApiResponse(responseCode = "404", description = "Alert not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> updateAlertAsRead(
            @PathVariable @Parameter(description = "Alert ID", example = "1") Long id) {

        return markAlertAsRead(id);
    }
}
