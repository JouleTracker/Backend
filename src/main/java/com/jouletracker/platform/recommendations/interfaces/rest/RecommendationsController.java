package com.jouletracker.platform.recommendations.interfaces.rest;

import com.jouletracker.platform.recommendations.application.commandservices.RecommendationCommandService;
import com.jouletracker.platform.recommendations.application.queryservices.RecommendationQueryService;
import com.jouletracker.platform.recommendations.domain.model.commands.CreateRecommendationCommand;
import com.jouletracker.platform.recommendations.domain.model.queries.GetRecommendationsByUserIdQuery;
import com.jouletracker.platform.recommendations.interfaces.rest.resources.CreateRecommendationResource;
import com.jouletracker.platform.recommendations.interfaces.rest.resources.RecommendationResource;
import com.jouletracker.platform.recommendations.interfaces.rest.transform.RecommendationResourceAssembler;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/recommendations", "/recommendations", "/api/v1/admin/recommendations", "/admin/recomendaciones"})
@Tag(name = "Recommendations", description = "Energy saving recommendations")
public class RecommendationsController {
    private final RecommendationCommandService commandService;
    private final RecommendationQueryService queryService;

    public RecommendationsController(RecommendationCommandService commandService, RecommendationQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResource>> getRecommendations(@RequestParam(defaultValue = "1") Long userId) {
        var recommendations = queryService.handle(new GetRecommendationsByUserIdQuery(userId)).stream()
                .map(RecommendationResourceAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping
    public ResponseEntity<?> createRecommendation(@Valid @RequestBody CreateRecommendationResource resource) {
        var command = new CreateRecommendationCommand(resource.userId(), resource.title(), resource.description(),
                resource.estimatedSavingKwh(), resource.estimatedSavingAmount(), resource.category());
        var result = commandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, RecommendationResourceAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }
}
