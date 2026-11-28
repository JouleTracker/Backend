package com.jouletracker.platform.recommendations.application.internal.commandservices;

import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.domain.model.commands.CreateRecommendationCommand;
import com.jouletracker.platform.recommendations.domain.repositories.RecommendationRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationCommandServiceImplTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private RecommendationCommandServiceImpl commandService;

    @Test
    void handleShouldSaveAndReturnRecommendationWhenValid() {
        when(recommendationRepository.save(any(Recommendation.class))).thenAnswer(inv -> {
            Recommendation rec = inv.getArgument(0);
            rec.setId(5L);
            return rec;
        });

        var command = new CreateRecommendationCommand(
                1L,
                "Adjust HVAC schedule",
                "Raise thermostat setpoint by 1C during off-hours.",
                14.5,
                10.15,
                "HVAC"
        );

        Result<Recommendation, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isSuccess());
        Recommendation saved = ((Result.Success<Recommendation, ApplicationError>) result).value();
        assertEquals(5L, saved.getId());
        assertEquals(1L, saved.getUserId());
        assertEquals("Adjust HVAC schedule", saved.getTitle());
        assertEquals(14.5, saved.getEstimatedSavingKwh());
    }

    @Test
    void handleShouldFailWhenUserIdIsInvalid() {
        var command = new CreateRecommendationCommand(0L, "Title", "Description", 5.0, 3.5, "Lighting");

        Result<Recommendation, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<Recommendation, ApplicationError>) result).error();
        assertEquals("VALIDATION_ERROR", error.code());
        verifyNoInteractions(recommendationRepository);
    }

    @Test
    void handleShouldFailWhenEstimatedSavingKwhIsNegative() {
        var command = new CreateRecommendationCommand(1L, "Title", "Description", -2.0, 3.5, "Lighting");

        Result<Recommendation, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<Recommendation, ApplicationError>) result).error();
        assertEquals("VALIDATION_ERROR", error.code());
        verifyNoInteractions(recommendationRepository);
    }

    @Test
    void handleShouldFailWhenTitleIsBlank() {
        var command = new CreateRecommendationCommand(1L, "  ", "Description", 2.0, 1.0, "General");

        Result<Recommendation, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isFailure());
        verifyNoInteractions(recommendationRepository);
    }
}
