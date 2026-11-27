
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
import com.jouletracker.platform.iam.infrastructure.security.authentication.AuthenticatedUserDetails;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * REST controller for physical business area management
 * (TS-16, US-31).
 *
 * US-32: Supports shared notification recipients
 * and validates authenticated business area ownership.
 */
@RestController
@RequestMapping(
        value = {
                "/api/v1/business-areas",
                "/business-areas",
                "/areas"
        },
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
     * Registers a physical area belonging to a business.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Register business area",
            description = "Creates a physical business area with optional shared notification recipients."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Business area created successfully",
                    content = @Content(schema = @Schema(
                            implementation = BusinessAreaResource.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Validation error or invalid input"),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied"),
            @ApiResponse(responseCode = "409",
                    description = "A business area with this name already exists")
    })
    public ResponseEntity<?> createBusinessArea(
            @Valid @RequestBody CreateBusinessAreaResource resource,
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = resolveAuthenticatedUserId(userId);

        if (resource.userId() != null
                && !Objects.equals(resource.userId(), resolvedUserId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot create a business area for another user"
            );
        }

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
     * Retrieves all business areas belonging to the authenticated user.
     */
    @GetMapping
    @Operation(
            summary = "Get business areas",
            description = "Retrieves physical areas registered by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business areas retrieved successfully",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(
                                    implementation = BusinessAreaResource.class)))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied")
    })
    public ResponseEntity<List<BusinessAreaResource>> getBusinessAreas(
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = resolveAuthenticatedUserId(userId);

        var query = new GetAllBusinessAreasQuery(resolvedUserId);

        var businessAreas = businessAreaQueryService.handle(query);

        var resources = businessAreas.stream()
                .filter(area -> Objects.equals(
                        area.getUserId(), resolvedUserId))
                .map(BusinessAreaResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves aggregated dashboard energy metrics.
     */
    @GetMapping("/metrics")
    @Operation(
            summary = "Get business dashboard energy metrics",
            description = "Retrieves combined consumption totals and per-area metrics."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business dashboard metrics retrieved successfully",
                    content = @Content(schema = @Schema(
                            implementation = BusinessDashboardMetricsResource.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied")
    })
    public ResponseEntity<BusinessDashboardMetricsResource>
    getBusinessDashboardMetrics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "daily")
            String period) {

        Long resolvedUserId = resolveAuthenticatedUserId(userId);

        var query = new GetBusinessDashboardMetricsQuery(
                resolvedUserId,
                period
        );

        var metrics = businessAreaQueryService.handle(query);

        return ResponseEntity.ok(
                BusinessAreaResourceFromEntityAssembler
                        .toDashboardResourceFromEntity(metrics)
        );
    }

    /**
     * Retrieves energy metrics for a single business area.
     */
    @GetMapping("/{id}/metrics")
    @Operation(
            summary = "Get business area energy metrics",
            description = "Retrieves consumption metrics for an owned business area."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business area metrics retrieved successfully",
                    content = @Content(schema = @Schema(
                            implementation = BusinessAreaMetricsResource.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "404",
                    description = "Business area not found")
    })
    public ResponseEntity<BusinessAreaMetricsResource>
    getBusinessAreaMetricsById(
            @PathVariable
            @Parameter(
                    description = "Business area identifier",
                    example = "1")
            Long id,
            @RequestParam(required = false, defaultValue = "daily")
            String period) {

        Long resolvedUserId = resolveAuthenticatedUserId(null);

        var query = new GetBusinessAreaMetricsByIdQuery(id, period);

        return businessAreaQueryService.handle(query)
                .filter(metrics -> Objects.equals(
                        metrics.userId(), resolvedUserId))
                .map(BusinessAreaResourceFromEntityAssembler::toMetricsResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves an owned business area by its identifier.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get business area by ID",
            description = "Retrieves an owned business area and its notification recipients."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business area found",
                    content = @Content(schema = @Schema(
                            implementation = BusinessAreaResource.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "404",
                    description = "Business area not found")
    })
    public ResponseEntity<BusinessAreaResource> getBusinessAreaById(
            @PathVariable
            @Parameter(
                    description = "Business area identifier",
                    example = "1")
            Long id) {

        Long resolvedUserId = resolveAuthenticatedUserId(null);

        var query = new GetBusinessAreaByIdQuery(id);

        return businessAreaQueryService.handle(query)
                .filter(area -> Objects.equals(
                        area.getUserId(), resolvedUserId))
                .map(BusinessAreaResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing business area and its recipients.
     */
    @RequestMapping(
            value = "/{id}",
            method = {RequestMethod.PUT, RequestMethod.PATCH},
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Update business area",
            description = "Updates business area details and shared notification recipients."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business area updated successfully",
                    content = @Content(schema = @Schema(
                            implementation = BusinessAreaResource.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Validation error"),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied"),
            @ApiResponse(responseCode = "404",
                    description = "Business area not found"),
            @ApiResponse(responseCode = "409",
                    description = "A business area with this name already exists")
    })
    public ResponseEntity<?> updateBusinessArea(
            @PathVariable
            @Parameter(
                    description = "Business area identifier",
                    example = "1")
            Long id,
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody UpdateBusinessAreaResource resource) {

        Long resolvedUserId = resolveAuthenticatedUserId(userId);

        var command =
                UpdateBusinessAreaCommandFromResourceAssembler
                        .toCommandFromResource(
                                id,
                                resolvedUserId,
                                resource
                        );

        var result = businessAreaCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                BusinessAreaResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    /**
     * Deletes an owned business area.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete business area",
            description = "Deletes a physical business area owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Business area deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied"),
            @ApiResponse(responseCode = "404",
                    description = "Business area not found")
    })
    public ResponseEntity<?> deleteBusinessArea(
            @PathVariable
            @Parameter(
                    description = "Business area identifier",
                    example = "1")
            Long id,
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = resolveAuthenticatedUserId(userId);

        var areaOpt = businessAreaQueryService.handle(
                new GetBusinessAreaByIdQuery(id)
        );

        if (areaOpt.isEmpty()
                || !Objects.equals(
                areaOpt.get().getUserId(),
                resolvedUserId)) {

            return ResponseEntity.notFound().build();
        }

        var command = new DeleteBusinessAreaCommand(
                id,
                resolvedUserId
        );

        var result = businessAreaCommandService.handle(command);

        return switch (result) {

            case Result.Success<Void, ApplicationError> ignored ->
                    ResponseEntity.noContent().build();

            case Result.Failure<Void, ApplicationError> failure ->
                    ProblemDetailFromApplicationErrorAssembler
                            .toResponseFromApplicationError(failure.error());
        };
    }

    /**
     * Resolves the authenticated user from the Spring Security context.
     * Prevents impersonation through request parameters.
     */
    private Long resolveAuthenticatedUserId(Long requestedUserId) {

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof AuthenticatedUserDetails principal)
                || principal.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        Long authenticatedUserId = principal.getId();

        if (requestedUserId != null
                && !Objects.equals(
                requestedUserId,
                authenticatedUserId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot access another user's business areas"
            );
        }

        return authenticatedUserId;
    }
}
