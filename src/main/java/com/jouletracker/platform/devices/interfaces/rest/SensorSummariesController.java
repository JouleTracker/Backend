package com.jouletracker.platform.devices.interfaces.rest;

import com.jouletracker.platform.devices.application.queryservices.SensorQueryService;
import com.jouletracker.platform.devices.domain.model.queries.GetSensorSummaryByUserIdQuery;
import com.jouletracker.platform.devices.interfaces.rest.resources.SensorSummaryResource;
import com.jouletracker.platform.devices.interfaces.rest.transform.SensorSummaryResourceFromEntityAssembler;
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
 * REST controller for IoT sensor KPI summaries.
 */
@RestController
@RequestMapping(value = {"/api/v1/sensor-summaries", "/sensor-summaries"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sensor Summaries", description = "Aggregated KPI metrics for IoT sensors")
public class SensorSummariesController {

    private final SensorQueryService sensorQueryService;

    public SensorSummariesController(SensorQueryService sensorQueryService) {
        this.sensorQueryService = sensorQueryService;
    }

    /**
     * Retrieves aggregated sensor metrics as a collection (supporting JSON-server query format).
     */
    @GetMapping
    @Operation(summary = "Get sensors summary", description = "Retrieves aggregated sensor KPIs for the specified user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SensorSummaryResource.class))))
    })
    public ResponseEntity<List<SensorSummaryResource>> getSensorSummaries(
            @RequestParam(required = false, defaultValue = "1") Long userId
    ) {
        var query = new GetSensorSummaryByUserIdQuery(userId);
        var summary = sensorQueryService.handle(query);
        var resource = SensorSummaryResourceFromEntityAssembler.toResourceFromEntity(summary);
        return ResponseEntity.ok(List.of(resource));
    }

    /**
     * Retrieves aggregated sensor metrics by identifier or user identifier.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get sensor summary by ID", description = "Retrieves aggregated sensor KPIs by summary ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SensorSummaryResource.class)))
    })
    public ResponseEntity<SensorSummaryResource> getSensorSummaryById(@PathVariable Long id) {
        var query = new GetSensorSummaryByUserIdQuery(id);
        var summary = sensorQueryService.handle(query);
        var resource = SensorSummaryResourceFromEntityAssembler.toResourceFromEntity(summary);
        return ResponseEntity.ok(resource);
    }
}
