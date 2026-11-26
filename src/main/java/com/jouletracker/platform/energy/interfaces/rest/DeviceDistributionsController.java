package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.queryservices.DeviceDistributionQueryService;
import com.jouletracker.platform.energy.domain.model.queries.GetDeviceDistributionQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.interfaces.rest.resources.DeviceDistributionResource;
import com.jouletracker.platform.energy.interfaces.rest.transform.EnergyMeasurementResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for device energy consumption distribution breakdown.
 */
@RestController
@RequestMapping(value = {"/api/v1/device-distributions", "/device-distributions"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Device Distributions", description = "Energy consumption percentage and kWh breakdown per appliance")
public class DeviceDistributionsController {

    private final DeviceDistributionQueryService deviceDistributionQueryService;

    public DeviceDistributionsController(DeviceDistributionQueryService deviceDistributionQueryService) {
        this.deviceDistributionQueryService = deviceDistributionQueryService;
    }

    @GetMapping
    @Operation(summary = "Get device consumption distribution", description = "Retrieves kWh, cost, and share percentage grouped by device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Distribution retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceDistributionResource.class))))
    })
    public ResponseEntity<List<DeviceDistributionResource>> getDeviceDistribution(
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "User ID", example = "1") Long userId,
            @RequestParam(required = false, defaultValue = "daily") @Parameter(description = "Period granularity", example = "daily") String period
    ) {
        var query = new GetDeviceDistributionQuery(userId, ConsumptionPeriod.fromString(period));
        var distributions = deviceDistributionQueryService.handle(query);
        var resources = distributions.stream()
                .map(EnergyMeasurementResourceAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}