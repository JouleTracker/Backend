package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.commandservices.EnergyMeasurementCommandService;
import com.jouletracker.platform.energy.application.queryservices.EnergyReadingQueryService;
import com.jouletracker.platform.energy.domain.model.commands.RegisterEnergyMeasurementCommand;
import com.jouletracker.platform.energy.domain.model.queries.GetEnergyReadingsQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.interfaces.rest.resources.EnergyReadingResource;
import com.jouletracker.platform.energy.interfaces.rest.transform.EnergyMeasurementResourceAssembler;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for time-series energy telemetry readings and ingestion.
 */
@RestController
@RequestMapping(value = {"/api/v1/energy-readings", "/energy-readings"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Energy Readings", description = "Time-series energy consumption readings and telemetry ingestion endpoints")
public class EnergyReadingsController {

    private final EnergyReadingQueryService energyReadingQueryService;
    private final EnergyMeasurementCommandService energyMeasurementCommandService;

    public EnergyReadingsController(EnergyReadingQueryService energyReadingQueryService,
                                    EnergyMeasurementCommandService energyMeasurementCommandService) {
        this.energyReadingQueryService = energyReadingQueryService;
        this.energyMeasurementCommandService = energyMeasurementCommandService;
    }

    @GetMapping
    @Operation(summary = "Get energy readings", description = "Retrieves time-series energy readings for charts filtered by user, period, and optional device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Readings retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EnergyReadingResource.class))))
    })
    public ResponseEntity<List<EnergyReadingResource>> getEnergyReadings(
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "User ID", example = "1") Long userId,
            @RequestParam(required = false, defaultValue = "daily") @Parameter(description = "Period granularity", example = "daily") String period,
            @RequestParam(required = false) @Parameter(description = "Optional Device ID filter") Long deviceId
    ) {
        var query = new GetEnergyReadingsQuery(userId, ConsumptionPeriod.fromString(period), deviceId);
        var readings = energyReadingQueryService.handle(query);
        var resources = readings.stream()
                .map(EnergyMeasurementResourceAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register energy reading", description = "Ingests a new energy telemetry measurement from an IoT sensor or device.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Energy reading recorded successfully",
                    content = @Content(schema = @Schema(implementation = EnergyReadingResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid telemetry payload"),
            @ApiResponse(responseCode = "404", description = "Referenced sensor or device not found")
    })
    public ResponseEntity<?> registerEnergyReading(
            @RequestBody RegisterReadingPayload payload,
            @RequestParam(required = false) Long userId
    ) {
        Long resolvedUserId = userId != null ? userId : (payload.userId() != null ? payload.userId() : 1L);
        var command = new RegisterEnergyMeasurementCommand(
                resolvedUserId,
                payload.sensorId(),
                payload.deviceId(),
                payload.deviceName(),
                payload.deviceCategory(),
                payload.powerKw() != null ? payload.powerKw() : 0.0,
                payload.energyKwh() != null ? payload.energyKwh() : (payload.kwh() != null ? payload.kwh() : 0.0),
                payload.voltage(),
                payload.currentAmps(),
                payload.measuredAt() != null ? payload.measuredAt() : LocalDateTime.now()
        );
        var result = energyMeasurementCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                EnergyMeasurementResourceAssembler::toReadingResourceFromAggregate,
                HttpStatus.CREATED
        );
    }

    public record RegisterReadingPayload(
            Long userId,
            Long sensorId,
            Long deviceId,
            String deviceName,
            String deviceCategory,
            Double powerKw,
            Double energyKwh,
            Double kwh,
            Double voltage,
            Double currentAmps,
            LocalDateTime measuredAt
    ) {
    }
}