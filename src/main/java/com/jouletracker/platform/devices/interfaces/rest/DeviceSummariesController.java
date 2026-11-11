package com.jouletracker.platform.devices.interfaces.rest;

import com.jouletracker.platform.devices.application.queryservices.DeviceQueryService;
import com.jouletracker.platform.devices.domain.model.queries.GetDeviceSummaryByUserIdQuery;
import com.jouletracker.platform.devices.interfaces.rest.resources.DeviceSummaryResource;
import com.jouletracker.platform.devices.interfaces.rest.transform.DeviceSummaryResourceFromEntityAssembler;
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
 * REST controller for device KPI summaries.
 */
@RestController
@RequestMapping(value = {"/api/v1/device-summaries", "/device-summaries"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Device Summaries", description = "Aggregated KPI metrics for devices")
public class DeviceSummariesController {

    private final DeviceQueryService deviceQueryService;

    public DeviceSummariesController(DeviceQueryService deviceQueryService) {
        this.deviceQueryService = deviceQueryService;
    }

    /**
     * Retrieves aggregated device metrics as a collection (supporting JSON-server query format).
     */
    @GetMapping
    @Operation(summary = "Get devices summary", description = "Retrieves aggregated device KPIs for the specified user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceSummaryResource.class))))
    })
    public ResponseEntity<List<DeviceSummaryResource>> getDeviceSummaries(
            @RequestParam(required = false, defaultValue = "1") Long userId
    ) {
        var query = new GetDeviceSummaryByUserIdQuery(userId);
        var summary = deviceQueryService.handle(query);
        var resource = DeviceSummaryResourceFromEntityAssembler.toResourceFromEntity(summary);
        return ResponseEntity.ok(List.of(resource));
    }

    /**
     * Retrieves aggregated device metrics by identifier or user identifier.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get device summary by ID", description = "Retrieves aggregated device KPIs by summary ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DeviceSummaryResource.class)))
    })
    public ResponseEntity<DeviceSummaryResource> getDeviceSummaryById(@PathVariable Long id) {
        var query = new GetDeviceSummaryByUserIdQuery(id);
        var summary = deviceQueryService.handle(query);
        var resource = DeviceSummaryResourceFromEntityAssembler.toResourceFromEntity(summary);
        return ResponseEntity.ok(resource);
    }
}
