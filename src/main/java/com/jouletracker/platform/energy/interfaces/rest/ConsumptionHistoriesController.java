package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.queryservices.ConsumptionHistoryQueryService;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionHistoryQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.interfaces.rest.resources.ConsumptionHistoryResource;
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
 * REST controller for itemized historical energy consumption records.
 */
@RestController
@RequestMapping(value = {"/api/v1/consumption-histories", "/consumption-histories"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Consumption Histories", description = "Itemized historical energy consumption log endpoints")
public class ConsumptionHistoriesController {

    private final ConsumptionHistoryQueryService consumptionHistoryQueryService;

    public ConsumptionHistoriesController(ConsumptionHistoryQueryService consumptionHistoryQueryService) {
        this.consumptionHistoryQueryService = consumptionHistoryQueryService;
    }

    @GetMapping
    @Operation(summary = "Get consumption history", description = "Retrieves itemized historical consumption records filtered by user, period, and optional device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consumption history retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConsumptionHistoryResource.class))))
    })
    public ResponseEntity<List<ConsumptionHistoryResource>> getConsumptionHistory(
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "User ID", example = "1") Long userId,
            @RequestParam(required = false, defaultValue = "monthly") @Parameter(description = "Period granularity", example = "monthly") String period,
            @RequestParam(required = false) @Parameter(description = "Optional Device ID filter") Long deviceId
    ) {
        var query = new GetConsumptionHistoryQuery(userId, ConsumptionPeriod.fromString(period), deviceId);
        var histories = consumptionHistoryQueryService.handle(query);
        var resources = histories.stream()
                .map(EnergyMeasurementResourceAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}