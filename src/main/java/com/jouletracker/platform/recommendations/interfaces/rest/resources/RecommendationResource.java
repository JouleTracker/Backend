package com.jouletracker.platform.recommendations.interfaces.rest.resources;

import java.time.LocalDateTime;

public record RecommendationResource(Long id, Long userId, String title, String description,
                                     Double estimatedSavingKwh, Double estimatedSavingAmount,
                                     String category, boolean completed, LocalDateTime createdAt) { }
