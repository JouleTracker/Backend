package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.queryservices.ConsumptionSummaryQueryService;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionSummaryQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.interfaces.rest.resources.ConsumptionSummaryResource;
import com.jouletracker.platform.energy.interfaces.rest.transform.EnergyMeasurementResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * REST controller for aggregated energy consumption KPIs.
 */
@RestController
@RequestMapping(value = {"/api/v1/consumption-summaries", "/consumption-summaries"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Consumption Summaries", description = "Aggregated energy consumption KPIs and avoided emissions endpoints")
public class ConsumptionSummariesController {

    private final ConsumptionSummaryQueryService consumptionSummaryQueryService;

    public ConsumptionSummariesController(ConsumptionSummaryQueryService consumptionSummaryQueryService) {
        this.consumptionSummaryQueryService = consumptionSummaryQueryService;
    }

    @GetMapping
    @Operation(summary = "Get consumption summary", description = "Retrieves total kWh, cost, peak power, and avoided CO2 emissions for a user and period.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ConsumptionSummaryResource.class)))
    })
    public ResponseEntity<ConsumptionSummaryResource> getConsumptionSummary(
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "User ID", example = "1") Long userId,
            @RequestParam(required = false, defaultValue = "daily") @Parameter(description = "Period granularity (daily, weekly, monthly, yearly)", example = "daily") String period
    ) {
        var query = new GetConsumptionSummaryQuery(userId, ConsumptionPeriod.fromString(period));
        var summary = consumptionSummaryQueryService.handle(query);
        return ResponseEntity.ok(EnergyMeasurementResourceAssembler.toResourceFromEntity(summary));
    }
}