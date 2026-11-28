package com.jouletracker.platform.recommendations.application.internal.commandservices;

import com.jouletracker.platform.recommendations.application.commandservices.RecommendationCommandService;
import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.domain.model.commands.CreateRecommendationCommand;
import com.jouletracker.platform.recommendations.domain.repositories.RecommendationRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationCommandServiceImpl implements RecommendationCommandService {
    private final RecommendationRepository recommendationRepository;

    public RecommendationCommandServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    @Transactional
    public Result<Recommendation, ApplicationError> handle(CreateRecommendationCommand command) {
        if (command.userId() == null || command.userId() <= 0) {
            return Result.failure(ApplicationError.validationError("userId", "User ID is required"));
        }
        if (command.title() == null || command.title().isBlank()) {
            return Result.failure(ApplicationError.validationError("title", "Title is required"));
        }
        if (command.description() == null || command.description().isBlank()) {
            return Result.failure(ApplicationError.validationError("description", "Description is required"));
        }
        if (command.category() == null || command.category().isBlank()) {
            return Result.failure(ApplicationError.validationError("category", "Category is required"));
        }
        if (command.estimatedSavingKwh() != null && command.estimatedSavingKwh() < 0) {
            return Result.failure(ApplicationError.validationError("estimatedSavingKwh", "Estimated saving must be non-negative"));
        }
        if (command.estimatedSavingAmount() != null && command.estimatedSavingAmount() < 0) {
            return Result.failure(ApplicationError.validationError("estimatedSavingAmount", "Estimated saving must be non-negative"));
        }

        Recommendation recommendation = new Recommendation(command);
        return Result.success(recommendationRepository.save(recommendation));
    }
}
