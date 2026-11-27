
package com.jouletracker.platform.recommendations.interfaces.rest;

import com.jouletracker.platform.recommendations.application.commandservices.RecommendationCommandService;
import com.jouletracker.platform.recommendations.application.queryservices.RecommendationQueryService;
import com.jouletracker.platform.recommendations.domain.model.commands.CreateRecommendationCommand;
import com.jouletracker.platform.recommendations.domain.model.queries.GetRecommendationsByUserIdQuery;
import com.jouletracker.platform.recommendations.interfaces.rest.resources.CreateRecommendationResource;
import com.jouletracker.platform.recommendations.interfaces.rest.resources.RecommendationResource;
import com.jouletracker.platform.recommendations.interfaces.rest.transform.RecommendationResourceAssembler;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping({"/recommendations", "/api/v1/recommendations"})
@Tag(
        name = "Recommendations",
        description = "Energy saving recommendations"
)
public class RecommendationsController {

    private final RecommendationCommandService commandService;
    private final RecommendationQueryService queryService;

    public RecommendationsController(
            RecommendationCommandService commandService,
            RecommendationQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(
            summary = "Get recommendations",
            description = "Retrieves all energy saving recommendations belonging to a user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<RecommendationResource>> getRecommendations(
            @Parameter(
                    description = "ID of the user whose recommendations will be retrieved",
                    example = "1",
                    required = true
            )
            @RequestParam @Positive Long userId) {

        var recommendations = queryService.handle(
                        new GetRecommendationsByUserIdQuery(userId)
                ).stream()
                .map(RecommendationResourceAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(recommendations);
    }

    @PostMapping
    @Operation(
            summary = "Create recommendation",
            description = "Creates a new energy saving recommendation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recommendation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid recommendation data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createRecommendation(
            @Valid @RequestBody CreateRecommendationResource resource) {

        var command = new CreateRecommendationCommand(
                resource.userId(),
                resource.title(),
                resource.description(),
                resource.estimatedSavingKwh(),
                resource.estimatedSavingAmount(),
                resource.category()
        );

        var result = commandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RecommendationResourceAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }
}
