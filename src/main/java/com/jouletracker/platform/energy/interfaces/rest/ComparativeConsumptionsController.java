package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.queryservices.ComparativeConsumptionQueryService;
import com.jouletracker.platform.energy.domain.model.queries.GetComparativeConsumptionQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.interfaces.rest.resources.ComparativeConsumptionResource;
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
 * REST controller for period-over-period comparative energy consumption analytics.
 */
@RestController
@RequestMapping(value = {"/api/v1/comparative-consumptions", "/comparative-consumptions"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Comparative Consumptions", description = "Current vs previous period energy consumption comparisons")
public class ComparativeConsumptionsController {

    private final ComparativeConsumptionQueryService comparativeConsumptionQueryService;

    public ComparativeConsumptionsController(ComparativeConsumptionQueryService comparativeConsumptionQueryService) {
        this.comparativeConsumptionQueryService = comparativeConsumptionQueryService;
    }

    @GetMapping
    @Operation(summary = "Get comparative consumption", description = "Compares current period kWh and cost against the previous period across time buckets.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comparative data retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComparativeConsumptionResource.class))))
    })
    public ResponseEntity<List<ComparativeConsumptionResource>> getComparativeConsumption(
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "User ID", example = "1") Long userId,
            @RequestParam(required = false, defaultValue = "weekly") @Parameter(description = "Period granularity", example = "weekly") String period
    ) {
        var query = new GetComparativeConsumptionQuery(userId, ConsumptionPeriod.fromString(period));
        var comparisons = comparativeConsumptionQueryService.handle(query);
        var resources = comparisons.stream()
                .map(EnergyMeasurementResourceAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}