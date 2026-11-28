package com.jouletracker.platform.recommendations.interfaces.acl;

import com.jouletracker.platform.recommendations.application.queryservices.RecommendationQueryService;
import com.jouletracker.platform.recommendations.domain.model.queries.GetRecommendationsByUserIdQuery;
import com.jouletracker.platform.recommendations.interfaces.rest.resources.RecommendationResource;
import com.jouletracker.platform.recommendations.interfaces.rest.transform.RecommendationResourceAssembler;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RecommendationsContextFacade {
    private final RecommendationQueryService queryService;
    public RecommendationsContextFacade(RecommendationQueryService queryService) { this.queryService = queryService; }
    public List<RecommendationResource> fetchRecommendationsByUserId(Long userId) {
        return queryService.handle(new GetRecommendationsByUserIdQuery(userId)).stream()
                .map(RecommendationResourceAssembler::toResourceFromEntity).toList();
    }
}
