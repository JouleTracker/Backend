package com.jouletracker.platform.recommendations.interfaces.rest.transform;

import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.interfaces.rest.resources.RecommendationResource;

public final class RecommendationResourceAssembler {
    private RecommendationResourceAssembler() { }
    public static RecommendationResource toResourceFromEntity(Recommendation entity) {
        if (entity == null) return null;
        return new RecommendationResource(entity.getId(), entity.getUserId(), entity.getTitle(),
                entity.getDescription(), entity.getEstimatedSavingKwh(), entity.getEstimatedSavingAmount(),
                entity.getCategory(), entity.isCompleted(), entity.getCreatedAt());
    }
}
