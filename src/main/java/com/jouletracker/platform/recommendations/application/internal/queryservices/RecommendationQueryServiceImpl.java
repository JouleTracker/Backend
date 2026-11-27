package com.jouletracker.platform.recommendations.application.internal.queryservices;

import com.jouletracker.platform.recommendations.application.queryservices.RecommendationQueryService;
import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.domain.model.queries.GetRecommendationsByUserIdQuery;
import com.jouletracker.platform.recommendations.domain.repositories.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RecommendationQueryServiceImpl implements RecommendationQueryService {
    private final RecommendationRepository recommendationRepository;

    public RecommendationQueryServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> handle(GetRecommendationsByUserIdQuery query) {
        if (query.userId() == null) return List.of();
        return recommendationRepository.findAllByUserId(query.userId());
    }
}
