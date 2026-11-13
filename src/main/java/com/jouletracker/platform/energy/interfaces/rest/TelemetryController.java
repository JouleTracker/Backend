package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.commandservices.EnergyMeasurementCommandService;
import com.jouletracker.platform.energy.application.queryservices.EnergyReadingQueryService;
import com.jouletracker.platform.energy.domain.model.queries.GetLatestReadingBySensorIdQuery;
import com.jouletracker.platform.energy.interfaces.rest.resources.RegisterTelemetryReadingResource;
import com.jouletracker.platform.energy.interfaces.rest.resources.TelemetryReadingResource;
import com.jouletracker.platform.energy.interfaces.rest.transform.EnergyMeasurementResourceAssembler;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for IoT telemetry ingestion (TS-07) and latest sensor reading retrieval (TS-10).
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Telemetry", description = "IoT telemetry ingestion and real-time sensor reading endpoints")
public class TelemetryController {

    private final EnergyMeasurementCommandService energyMeasurementCommandService;
    private final EnergyReadingQueryService energyReadingQueryService;

    public TelemetryController(EnergyMeasurementCommandService energyMeasurementCommandService,
                               EnergyReadingQueryService energyReadingQueryService) {
        this.energyMeasurementCommandService = energyMeasurementCommandService;
        this.energyReadingQueryService = energyReadingQueryService;
    }

    /**
     * Ingests a new electrical telemetry reading from an IoT sensor (TS-07).
     *
     * @param resource telemetry reading payload (kW, kWh, voltage, current)
     * @param userId   optional userId query override
     * @return 201 Created with TelemetryReadingResource, or RFC 9457 error
     */
    @PostMapping(value = "/telemetry/readings", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Receive energy reading", description = "Ingests and persists electrical telemetry readings (kW, kWh, voltage, current) from IoT sensors.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Telemetry reading ingested successfully",
                    content = @Content(schema = @Schema(implementation = TelemetryReadingResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid telemetry payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Referenced sensor or device not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> receiveEnergyReading(
            @Valid @RequestBody RegisterTelemetryReadingResource resource,
            @RequestParam(required = false) Long userId
    ) {
        var command = EnergyMeasurementResourceAssembler.toCommandFromTelemetryResource(resource, userId);
        var result = energyMeasurementCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                EnergyMeasurementResourceAssembler::toTelemetryResourceFromAggregate,
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieves the latest electrical measurement recorded by a given IoT sensor (TS-10).
     *
     * @param id the IoT sensor ID
     * @return 200 OK with TelemetryReadingResource, or 404 if sensor or reading not found
     */
    @GetMapping("/sensors/{id}/readings/latest")
    @Operation(summary = "Get latest sensor reading", description = "Retrieves the most recent telemetry measurement recorded by a specific IoT sensor for real-time dashboard monitoring.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Latest reading retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TelemetryReadingResource.class))),
            @ApiResponse(responseCode = "404", description = "Sensor or telemetry reading not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> getLatestSensorReading(
            @PathVariable @Parameter(description = "IoT Sensor ID", example = "1") Long id
    ) {
        var result = energyReadingQueryService.handle(new GetLatestReadingBySensorIdQuery(id));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                EnergyMeasurementResourceAssembler::toTelemetryResourceFromAggregate,
                HttpStatus.OK
        );
    }
}
