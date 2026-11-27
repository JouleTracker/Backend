package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.commandservices.MonthlyGoalCommandService;
import com.jouletracker.platform.alerts.application.queryservices.MonthlyGoalQueryService;
import com.jouletracker.platform.alerts.domain.model.commands.DeleteMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.queries.GetCurrentMonthlyGoalQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetMonthlyGoalByIdQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetMonthlyGoalsByUserIdQuery;
import com.jouletracker.platform.alerts.interfaces.rest.resources.CreateMonthlyGoalResource;
import com.jouletracker.platform.alerts.interfaces.rest.resources.MonthlyGoalResource;
import com.jouletracker.platform.alerts.interfaces.rest.resources.UpdateMonthlyGoalResource;
import com.jouletracker.platform.alerts.interfaces.rest.transform.MonthlyGoalResourceAssembler;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing CRUD endpoints for monthly energy consumption goals (US-28).
 */
@RestController
@RequestMapping(value = {"/api/v1/monthly-goals", "/monthly-goals"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Monthly Goals", description = "Monthly energy consumption goal (kWh) CRUD and progress endpoints")
public class MonthlyGoalsController {

    private final MonthlyGoalCommandService monthlyGoalCommandService;
    private final MonthlyGoalQueryService monthlyGoalQueryService;

    public MonthlyGoalsController(MonthlyGoalCommandService monthlyGoalCommandService,
                                  MonthlyGoalQueryService monthlyGoalQueryService) {
        this.monthlyGoalCommandService = monthlyGoalCommandService;
        this.monthlyGoalQueryService = monthlyGoalQueryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create monthly consumption goal", description = "Creates a monthly energy target in kWh for the user (US-28).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Monthly goal created successfully",
                    content = @Content(schema = @Schema(implementation = MonthlyGoalResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Goal already exists for this month",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> createMonthlyGoal(
            @Valid @RequestBody CreateMonthlyGoalResource resource,
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = userId != null ? userId : (resource.userId() != null ? resource.userId() : 1L);
        var command = MonthlyGoalResourceAssembler.toCreateCommandFromResource(resource, resolvedUserId);
        var result = monthlyGoalCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                goal -> MonthlyGoalResourceAssembler.toResourceFromProgress(monthlyGoalQueryService.toProgress(goal)),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get user monthly goals", description = "Retrieves all monthly consumption goals and progress for the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly goals retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MonthlyGoalResource.class))))
    })
    public ResponseEntity<List<MonthlyGoalResource>> getMonthlyGoals(
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        var goals = monthlyGoalQueryService.handle(new GetMonthlyGoalsByUserIdQuery(userId));
        var resources = goals.stream()
                .map(MonthlyGoalResourceAssembler::toResourceFromProgress)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/current")
    @Operation(summary = "Get current month goal", description = "Retrieves the active monthly consumption goal and progress for the current or specified month.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current monthly goal retrieved",
                    content = @Content(schema = @Schema(implementation = MonthlyGoalResource.class))),
            @ApiResponse(responseCode = "404", description = "No monthly goal configured for this period")
    })
    public ResponseEntity<MonthlyGoalResource> getCurrentMonthlyGoal(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        return monthlyGoalQueryService.handle(new GetCurrentMonthlyGoalQuery(userId, year, month))
                .map(MonthlyGoalResourceAssembler::toResourceFromProgress)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get monthly goal by ID", description = "Retrieves a specific monthly consumption goal by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly goal found",
                    content = @Content(schema = @Schema(implementation = MonthlyGoalResource.class))),
            @ApiResponse(responseCode = "404", description = "Monthly goal not found")
    })
    public ResponseEntity<MonthlyGoalResource> getMonthlyGoalById(
            @PathVariable @Parameter(description = "Monthly goal ID", example = "1") Long id) {

        return monthlyGoalQueryService.handle(new GetMonthlyGoalByIdQuery(id))
                .map(MonthlyGoalResourceAssembler::toResourceFromProgress)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update monthly goal", description = "Updates the target kWh or period of an existing monthly consumption goal (US-28).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly goal updated successfully",
                    content = @Content(schema = @Schema(implementation = MonthlyGoalResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Monthly goal not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> updateMonthlyGoal(
            @PathVariable @Parameter(description = "Monthly goal ID", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody UpdateMonthlyGoalResource resource) {

        var command = MonthlyGoalResourceAssembler.toUpdateCommandFromResource(id, userId, resource);
        var result = monthlyGoalCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                goal -> MonthlyGoalResourceAssembler.toResourceFromProgress(monthlyGoalQueryService.toProgress(goal)),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete monthly goal", description = "Deletes a monthly consumption goal.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Monthly goal deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Monthly goal not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> deleteMonthlyGoal(
            @PathVariable @Parameter(description = "Monthly goal ID", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        var result = monthlyGoalCommandService.handle(new DeleteMonthlyGoalCommand(id, userId));
        return switch (result) {
            case Result.Success<Void, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<Void, ApplicationError> failure ->
                    ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
        };
    }
}