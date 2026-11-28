package com.jouletracker.platform.recommendations.domain.model.events;

import java.time.LocalDateTime;

public record RecommendationCreatedEvent(Long recommendationId, Long userId,
                                         String title, LocalDateTime createdAt) { }
