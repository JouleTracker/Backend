package com.jouletracker.platform.devices.interfaces.rest;

import com.jouletracker.platform.devices.application.commandservices.DeviceCommandService;
import com.jouletracker.platform.devices.application.queryservices.DeviceQueryService;
import com.jouletracker.platform.devices.domain.model.commands.DeleteDeviceCommand;
import com.jouletracker.platform.devices.domain.model.queries.GetAllDevicesByUserIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetDeviceByIdQuery;
import com.jouletracker.platform.devices.interfaces.rest.resources.CreateDeviceResource;
import com.jouletracker.platform.devices.interfaces.rest.resources.DeviceResource;
import com.jouletracker.platform.devices.interfaces.rest.resources.UpdateDeviceResource;
import com.jouletracker.platform.devices.interfaces.rest.transform.CreateDeviceCommandFromResourceAssembler;
import com.jouletracker.platform.devices.interfaces.rest.transform.DeviceResourceFromEntityAssembler;
import com.jouletracker.platform.devices.interfaces.rest.transform.UpdateDeviceCommandFromResourceAssembler;
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
 * REST controller for electrical appliance management (US-40, US-42, US-44).
 */
@RestController
@RequestMapping(value = {"/api/v1/devices", "/devices"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Devices", description = "Monitored electrical appliances management endpoints")
public class DevicesController {

    private final DeviceCommandService deviceCommandService;
    private final DeviceQueryService deviceQueryService;

    public DevicesController(DeviceCommandService deviceCommandService, DeviceQueryService deviceQueryService) {
        this.deviceCommandService = deviceCommandService;
        this.deviceQueryService = deviceQueryService;
    }

    /**
     * Registers a new monitored domestic device.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register electrical device", description = "Creates a new monitored electrical appliance for the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device registered successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResource.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid input"),
            @ApiResponse(responseCode = "409", description = "Device with identical name already exists in same location")
    })
    public ResponseEntity<?> createDevice(@Valid @RequestBody CreateDeviceResource resource,
                                         @RequestParam(required = false) Long userId) {
        Long resolvedUserId = userId != null ? userId : (resource.userId() != null ? resource.userId() : 1L);
        var command = CreateDeviceCommandFromResourceAssembler.toCommandFromResource(resource, resolvedUserId);
        var result = deviceCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DeviceResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieves all devices registered by the specified user.
     */
    @GetMapping
    @Operation(summary = "Get user devices", description = "Retrieves all electrical appliances registered by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceResource.class))))
    })
    public ResponseEntity<List<DeviceResource>> getDevices(@RequestParam(required = false, defaultValue = "1") Long userId) {
        var query = new GetAllDevicesByUserIdQuery(userId);
        var devices = deviceQueryService.handle(query);
        var resources = devices.stream()
                .map(DeviceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a device by its unique identifier.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieves details of a specific electrical appliance.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = @Content(schema = @Schema(implementation = DeviceResource.class))),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<DeviceResource> getDeviceById(@PathVariable @Parameter(description = "Device ID", example = "1") Long id) {
        var query = new GetDeviceByIdQuery(id);
        var device = deviceQueryService.handle(query);
        return device.map(d -> ResponseEntity.ok(DeviceResourceFromEntityAssembler.toResourceFromEntity(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Updates device details (supports both PUT and PATCH).
     */
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update device details", description = "Updates editable properties of an existing appliance.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResource.class))),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Conflict with an existing device name")
    })
    public ResponseEntity<?> updateDevice(
            @PathVariable @Parameter(description = "Device ID", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody UpdateDeviceResource resource
    ) {
        var command = UpdateDeviceCommandFromResourceAssembler.toCommandFromResource(id, userId, resource);
        var result = deviceCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DeviceResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    /**
     * Deletes an electrical appliance and releases any assigned IoT sensors.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete electrical device", description = "Deletes an appliance and automatically releases assigned IoT sensors (US-44).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<?> deleteDevice(
            @PathVariable @Parameter(description = "Device ID", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId
    ) {
        var command = new DeleteDeviceCommand(id, userId);
        var result = deviceCommandService.handle(command);
        return switch (result) {
            case Result.Success<Void, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<Void, ApplicationError> failure -> ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
        };
    }
}
