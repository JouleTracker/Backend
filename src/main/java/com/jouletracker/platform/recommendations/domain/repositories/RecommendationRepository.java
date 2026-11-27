package com.jouletracker.platform.recommendations.domain.repositories;

import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import java.util.List;
import java.util.Optional;

public interface RecommendationRepository {
    Recommendation save(Recommendation recommendation);
    Optional<Recommendation> findById(Long id);
    List<Recommendation> findAllByUserId(Long userId);
}
