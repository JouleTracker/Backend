package com.jouletracker.platform.recommendations.application.internal.queryservices;

import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.domain.model.queries.GetRecommendationsByUserIdQuery;
import com.jouletracker.platform.recommendations.domain.repositories.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationQueryServiceImplTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private RecommendationQueryServiceImpl queryService;

    @Test
    void handleShouldReturnUserRecommendations() {
        Recommendation rec = new Recommendation(
                1L, 1L, "Switch to LED", "Replace halogen bulbs", 8.0, 5.6, "Lighting", false, LocalDateTime.now()
        );
        when(recommendationRepository.findAllByUserId(1L)).thenReturn(List.of(rec));

        List<Recommendation> results = queryService.handle(new GetRecommendationsByUserIdQuery(1L));

        assertEquals(1, results.size());
        assertEquals("Switch to LED", results.getFirst().getTitle());
    }

    @Test
    void handleShouldReturnEmptyListWhenUserIdIsNull() {
        List<Recommendation> results = queryService.handle(new GetRecommendationsByUserIdQuery(null));

        assertTrue(results.isEmpty());
        verifyNoInteractions(recommendationRepository);
    }
}
