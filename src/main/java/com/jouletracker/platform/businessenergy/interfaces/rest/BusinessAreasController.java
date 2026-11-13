package com.jouletracker.platform.businessenergy.interfaces.rest;

import com.jouletracker.platform.businessenergy.application.commandservices.BusinessAreaCommandService;
import com.jouletracker.platform.businessenergy.application.queryservices.BusinessAreaQueryService;
import com.jouletracker.platform.businessenergy.domain.model.commands.DeleteBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetAllBusinessAreasQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaMetricsByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessDashboardMetricsQuery;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.BusinessAreaMetricsResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.BusinessAreaResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.BusinessDashboardMetricsResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.CreateBusinessAreaResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.UpdateBusinessAreaResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.transform.BusinessAreaResourceFromEntityAssembler;
import com.jouletracker.platform.businessenergy.interfaces.rest.transform.CreateBusinessAreaCommandFromResourceAssembler;
import com.jouletracker.platform.businessenergy.interfaces.rest.transform.UpdateBusinessAreaCommandFromResourceAssembler;
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
 * REST controller for physical business area management (TS-16, US-31).
 */
@RestController
@RequestMapping(
        value = {"/api/v1/business-areas", "/business-areas", "/areas"},
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Business Areas",
        description = "Physical business area management endpoints for MYPE establishments"
)
public class BusinessAreasController {

    private final BusinessAreaCommandService businessAreaCommandService;
    private final BusinessAreaQueryService businessAreaQueryService;

    public BusinessAreasController(
            BusinessAreaCommandService businessAreaCommandService,
            BusinessAreaQueryService businessAreaQueryService) {
        this.businessAreaCommandService = businessAreaCommandService;
        this.businessAreaQueryService = businessAreaQueryService;
    }

    /**
     * Registers a physical area belonging to a business (US-31).
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Register business area",
            description = "Creates a new physical area for business energy management (TS-16, US-31)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Business area created successfully",
                    content = @Content(schema = @Schema(implementation = BusinessAreaResource.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid input"),
            @ApiResponse(responseCode = "409", description = "A business area with this name already exists")
    })
    public ResponseEntity<?> createBusinessArea(
            @Valid @RequestBody CreateBusinessAreaResource resource,
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = userId != null ? userId : (resource.userId() != null ? resource.userId() : 1L);

        var command = CreateBusinessAreaCommandFromResourceAssembler
                .toCommandFromResource(resource, resolvedUserId);

        var result = businessAreaCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                BusinessAreaResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieves all business areas belonging to a user.
     */
    @GetMapping
    @Operation(
            summary = "Get business areas",
            description = "Retrieves all physical areas registered by the specified user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business areas retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BusinessAreaResource.class))))
    })
    public ResponseEntity<List<BusinessAreaResource>> getBusinessAreas(
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        var query = new GetAllBusinessAreasQuery(userId);
        var businessAreas = businessAreaQueryService.handle(query);

        var resources = businessAreas.stream()
                .map(BusinessAreaResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves aggregated dashboard energy metrics across all business areas (TS-17).
     */
    @GetMapping("/metrics")
    @Operation(
            summary = "Get business dashboard energy metrics",
            description = "Retrieves combined consumption totals and per-area metrics for a MYPE establishment (TS-17)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business dashboard metrics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BusinessDashboardMetricsResource.class)))
    })
    public ResponseEntity<BusinessDashboardMetricsResource> getBusinessDashboardMetrics(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam(required = false, defaultValue = "daily") String period) {

        var query = new GetBusinessDashboardMetricsQuery(userId, period);
        var metrics = businessAreaQueryService.handle(query);
        return ResponseEntity.ok(BusinessAreaResourceFromEntityAssembler.toDashboardResourceFromEntity(metrics));
    }

    /**
     * Retrieves energy metrics for a single physical business area (TS-17).
     */
    @GetMapping("/{id}/metrics")
    @Operation(
            summary = "Get business area energy metrics",
            description = "Retrieves real-time and period energy consumption metrics for a specific business area (TS-17)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business area metrics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BusinessAreaMetricsResource.class))),
            @ApiResponse(responseCode = "404", description = "Business area not found")
    })
    public ResponseEntity<BusinessAreaMetricsResource> getBusinessAreaMetricsById(
            @PathVariable @Parameter(description = "Business area identifier", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "daily") String period) {

        var query = new GetBusinessAreaMetricsByIdQuery(id, period);
        return businessAreaQueryService.handle(query)
                .map(BusinessAreaResourceFromEntityAssembler::toMetricsResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a business area by its unique identifier.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get business area by ID",
            description = "Retrieves a specific business area by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business area found",
                    content = @Content(schema = @Schema(implementation = BusinessAreaResource.class))),
            @ApiResponse(responseCode = "404", description = "Business area not found")
    })
    public ResponseEntity<BusinessAreaResource> getBusinessAreaById(
            @PathVariable @Parameter(description = "Business area identifier", example = "1") Long id) {

        var query = new GetBusinessAreaByIdQuery(id);
        var areaOpt = businessAreaQueryService.handle(query);

        return areaOpt.map(area -> ResponseEntity.ok(BusinessAreaResourceFromEntityAssembler.toResourceFromEntity(area)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing business area (US-31 Escenario 3).
     */
    @RequestMapping(
            value = "/{id}",
            method = {RequestMethod.PUT, RequestMethod.PATCH},
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Update business area",
            description = "Updates the editable details of a business area (US-31 Escenario 3)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business area updated successfully",
                    content = @Content(schema = @Schema(implementation = BusinessAreaResource.class))),
            @ApiResponse(responseCode = "404", description = "Business area not found"),
            @ApiResponse(responseCode = "409", description = "A business area with this name already exists")
    })
    public ResponseEntity<?> updateBusinessArea(
            @PathVariable @Parameter(description = "Business area identifier", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody UpdateBusinessAreaResource resource) {

        var command = UpdateBusinessAreaCommandFromResourceAssembler.toCommandFromResource(id, userId, resource);
        var result = businessAreaCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                BusinessAreaResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    /**
     * Deletes a physical business area.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete business area",
            description = "Deletes a physical business area from the establishment."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Business area deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Business area not found")
    })
    public ResponseEntity<?> deleteBusinessArea(
            @PathVariable @Parameter(description = "Business area identifier", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        var command = new DeleteBusinessAreaCommand(id, userId);
        var result = businessAreaCommandService.handle(command);

        return switch (result) {
            case Result.Success<Void, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<Void, ApplicationError> failure ->
                    ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
        };
    }
}
