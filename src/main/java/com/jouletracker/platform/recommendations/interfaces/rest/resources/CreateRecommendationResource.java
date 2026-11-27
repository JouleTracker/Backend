package com.jouletracker.platform.recommendations.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRecommendationResource(@NotNull Long userId, @NotBlank String title,
                                           @NotBlank String description, Double estimatedSavingKwh,
                                           Double estimatedSavingAmount, @NotBlank String category) { }
