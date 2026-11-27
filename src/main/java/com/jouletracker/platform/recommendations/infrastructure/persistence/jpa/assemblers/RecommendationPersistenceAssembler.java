package com.jouletracker.platform.recommendations.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.infrastructure.persistence.jpa.entities.RecommendationPersistenceEntity;

public final class RecommendationPersistenceAssembler {
    private RecommendationPersistenceAssembler() { }

    public static Recommendation toDomainFromPersistence(RecommendationPersistenceEntity entity) {
        if (entity == null) return null;
        return new Recommendation(entity.getId(), entity.getUserId(), entity.getTitle(), entity.getDescription(),
                entity.getEstimatedSavingKwh(), entity.getEstimatedSavingAmount(), entity.getCategory(),
                entity.isCompleted(), entity.getCreatedAt());
    }

    public static RecommendationPersistenceEntity toPersistenceFromDomain(Recommendation domain) {
        if (domain == null) return null;
        RecommendationPersistenceEntity entity = new RecommendationPersistenceEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setEstimatedSavingKwh(domain.getEstimatedSavingKwh());
        entity.setEstimatedSavingAmount(domain.getEstimatedSavingAmount());
        entity.setCategory(domain.getCategory());
        entity.setCompleted(domain.isCompleted());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
