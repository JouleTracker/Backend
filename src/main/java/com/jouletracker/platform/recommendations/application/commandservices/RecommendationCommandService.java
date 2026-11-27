package com.jouletracker.platform.recommendations.application.commandservices;

import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.domain.model.commands.CreateRecommendationCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

public interface RecommendationCommandService {
    Result<Recommendation, ApplicationError> handle(CreateRecommendationCommand command);
}
