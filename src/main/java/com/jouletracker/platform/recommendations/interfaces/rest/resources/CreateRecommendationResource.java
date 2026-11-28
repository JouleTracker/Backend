package com.jouletracker.platform.recommendations.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRecommendationResource(
        @NotNull Long userId,
        @NotBlank String title,
        @NotBlank String description,
        @JsonAlias("estimatedSavingsKwh") Double estimatedSavingKwh,
        Double estimatedSavingAmount,
        @NotBlank String category
) { }
