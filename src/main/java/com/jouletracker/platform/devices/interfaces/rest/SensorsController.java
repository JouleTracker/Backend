package com.jouletracker.platform.devices.interfaces.rest;

import com.jouletracker.platform.devices.application.commandservices.SensorCommandService;
import com.jouletracker.platform.devices.application.queryservices.SensorQueryService;
import com.jouletracker.platform.devices.domain.model.commands.DeleteSensorCommand;
import com.jouletracker.platform.devices.domain.model.queries.GetAllSensorsByUserIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetSensorByIdQuery;
import com.jouletracker.platform.devices.interfaces.rest.resources.RegisterSensorResource;
import com.jouletracker.platform.devices.interfaces.rest.resources.SensorResource;
import com.jouletracker.platform.devices.interfaces.rest.resources.UpdateSensorResource;
import com.jouletracker.platform.devices.interfaces.rest.transform.RegisterSensorCommandFromResourceAssembler;
import com.jouletracker.platform.devices.interfaces.rest.transform.SensorResourceFromEntityAssembler;
import com.jouletracker.platform.devices.interfaces.rest.transform.UpdateSensorCommandFromResourceAssembler;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for IoT smart meter sensors (TS-05, TS-08, US-11, US-12, US-13, US-14, US-15, US-41, US-43).
 */
@RestController
@RequestMapping(value = {"/api/v1/sensors", "/sensors"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sensors", description = "IoT smart metering sensors endpoints")
public class SensorsController {

    private final SensorCommandService sensorCommandService;
    private final SensorQueryService sensorQueryService;

    public SensorsController(SensorCommandService sensorCommandService, SensorQueryService sensorQueryService) {
        this.sensorCommandService = sensorCommandService;
        this.sensorQueryService = sensorQueryService;
    }

    /**
     * Registers a new IoT smart meter sensor.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register IoT sensor", description = "Registers a new smart meter sensor and optionally links it to an appliance or profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sensor registered successfully",
                    content = @Content(schema = @Schema(implementation = SensorResource.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid input"),
            @ApiResponse(responseCode = "409", description = "Sensor with this serial number already registered")
    })
    public ResponseEntity<?> registerSensor(
            @Valid @RequestBody RegisterSensorResource resource,
            @RequestParam(required = false) Long userId
    ) {
        Long resolvedUserId = userId != null ? userId : (resource.userId() != null ? resource.userId() : 1L);
        var command = RegisterSensorCommandFromResourceAssembler.toCommandFromResource(resource, resolvedUserId);
        var result = sensorCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SensorResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieves all IoT sensors registered by the specified user.
     */
    @GetMapping
    @Operation(summary = "Get user sensors", description = "Retrieves all smart meter sensors owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensors retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SensorResource.class))))
    })
    public ResponseEntity<List<SensorResource>> getSensors(
            @RequestParam(required = false, defaultValue = "1") Long userId
    ) {
        var query = new GetAllSensorsByUserIdQuery(userId);
        var sensors = sensorQueryService.handle(query);
        var resources = sensors.stream()
                .map(SensorResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a sensor by its unique identifier.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get sensor by ID", description = "Retrieves details and status of a specific IoT sensor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensor found",
                    content = @Content(schema = @Schema(implementation = SensorResource.class))),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<SensorResource> getSensorById(@PathVariable @Parameter(description = "Sensor ID", example = "1") Long id) {
        var query = new GetSensorByIdQuery(id);
        var sensor = sensorQueryService.handle(query);
        return sensor.map(s -> ResponseEntity.ok(SensorResourceFromEntityAssembler.toResourceFromEntity(s)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Updates an IoT sensor configuration, status, device assignment, or appliance profile.
     */
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update sensor details", description = "Updates sensor name, model, network status, or appliance assignment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensor updated successfully",
                    content = @Content(schema = @Schema(implementation = SensorResource.class))),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<?> updateSensor(
            @PathVariable @Parameter(description = "Sensor ID", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody UpdateSensorResource resource
    ) {
        var command = UpdateSensorCommandFromResourceAssembler.toCommandFromResource(id, userId, resource);
        var result = sensorCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SensorResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    /**
     * Deletes / unlinks an IoT sensor.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete IoT sensor", description = "Unlinks and deletes an IoT sensor from the user account.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sensor deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<?> deleteSensor(
            @PathVariable @Parameter(description = "Sensor ID", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId
    ) {
        var command = new DeleteSensorCommand(id, userId);
        var result = sensorCommandService.handle(command);
        return switch (result) {
            case Result.Success<Void, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<Void, ApplicationError> failure -> ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
        };
    }
}
