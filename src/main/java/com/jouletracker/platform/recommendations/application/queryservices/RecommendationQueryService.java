package com.jouletracker.platform.recommendations.application.queryservices;

import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.domain.model.queries.GetRecommendationsByUserIdQuery;
import java.util.List;

public interface RecommendationQueryService {
    List<Recommendation> handle(GetRecommendationsByUserIdQuery query);
}
