package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.queryservices.ConsumptionHistoryQueryService;
import com.jouletracker.platform.energy.domain.model.entities.ConsumptionHistory;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionHistoryQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.interfaces.rest.resources.ConsumptionHistoryResource;
import com.jouletracker.platform.energy.interfaces.rest.transform.EnergyMeasurementResourceAssembler;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * REST controller for itemized historical energy consumption records and CSV report exports (US-23, US-24, US-47).
 */
@RestController
@RequestMapping(value = {"/api/v1/consumption-histories", "/consumption-histories"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Consumption Histories", description = "Itemized historical energy consumption log and CSV export endpoints")
public class ConsumptionHistoriesController {

    private final ConsumptionHistoryQueryService consumptionHistoryQueryService;

    public ConsumptionHistoriesController(ConsumptionHistoryQueryService consumptionHistoryQueryService) {
        this.consumptionHistoryQueryService = consumptionHistoryQueryService;
    }

    @GetMapping
    @Operation(
            summary = "Get consumption history",
            description = "Retrieves itemized historical consumption records filtered by user, period, optional device, and optional ISO date range (from/to)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consumption history retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConsumptionHistoryResource.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid date filter parameter",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> getConsumptionHistory(
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "User ID", example = "1") Long userId,
            @RequestParam(required = false, defaultValue = "monthly") @Parameter(description = "Period granularity", example = "monthly") String period,
            @RequestParam(required = false) @Parameter(description = "Optional Device ID filter") Long deviceId,
            @RequestParam(required = false) @Parameter(description = "Optional start date or ISO datetime (e.g. 2026-11-01)", example = "2026-11-01") String from,
            @RequestParam(required = false) @Parameter(description = "Optional end date or ISO datetime (e.g. 2026-11-30)", example = "2026-11-30") String to
    ) {
        LocalDateTime fromDateTime;
        LocalDateTime toDateTime;
        try {
            fromDateTime = parseDateParam(from, false);
            toDateTime = parseDateParam(to, true);
        } catch (IllegalArgumentException ex) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.validationError("dateRange", ex.getMessage())
            );
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.validationError("from", "'from' must not be after 'to'")
            );
        }

        var query = new GetConsumptionHistoryQuery(userId, ConsumptionPeriod.fromString(period), deviceId, fromDateTime, toDateTime);
        var histories = consumptionHistoryQueryService.handle(query);
        var resources = histories.stream()
                .map(EnergyMeasurementResourceAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(
            summary = "Export consumption history CSV",
            description = "Exports itemized historical consumption records as a downloadable CSV report (US-47)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV report exported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date filter parameter",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> exportConsumptionHistoryCsv(
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "User ID", example = "1") Long userId,
            @RequestParam(required = false, defaultValue = "monthly") @Parameter(description = "Period granularity", example = "monthly") String period,
            @RequestParam(required = false) @Parameter(description = "Optional Device ID filter") Long deviceId,
            @RequestParam(required = false) @Parameter(description = "Optional start date or ISO datetime", example = "2026-11-01") String from,
            @RequestParam(required = false) @Parameter(description = "Optional end date or ISO datetime", example = "2026-11-30") String to
    ) {
        LocalDateTime fromDateTime;
        LocalDateTime toDateTime;
        try {
            fromDateTime = parseDateParam(from, false);
            toDateTime = parseDateParam(to, true);
        } catch (IllegalArgumentException ex) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.validationError("dateRange", ex.getMessage())
            );
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.validationError("from", "'from' must not be after 'to'")
            );
        }

        var query = new GetConsumptionHistoryQuery(userId, ConsumptionPeriod.fromString(period), deviceId, fromDateTime, toDateTime);
        List<ConsumptionHistory> histories = consumptionHistoryQueryService.handle(query);

        StringBuilder csv = new StringBuilder();
        csv.append("id,deviceId,deviceName,deviceCategory,powerKw,energyKwh,cost,status,measuredAt\n");
        for (ConsumptionHistory entry : histories) {
            csv.append(escapeCsv(entry.id())).append(',')
                    .append(escapeCsv(entry.deviceId())).append(',')
                    .append(escapeCsv(entry.deviceName())).append(',')
                    .append(escapeCsv(entry.category())).append(',')
                    .append(escapeCsv(entry.powerKw())).append(',')
                    .append(escapeCsv(entry.energyKwh())).append(',')
                    .append(escapeCsv(entry.cost())).append(',')
                    .append(escapeCsv(entry.status())).append(',')
                    .append(escapeCsv(entry.recordedAt() != null
                            ? entry.recordedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            : ""))
                    .append('\n');
        }

        byte[] payload = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"consumption-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(payload);
    }

    private LocalDateTime parseDateParam(String raw, boolean endOfDay) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        try {
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            try {
                LocalDate date = LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE);
                return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid ISO date/datetime value: " + trimmed);
            }
        }
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String str = String.valueOf(value);
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}