package com.jouletracker.platform.recommendations.interfaces.rest;

import com.jouletracker.platform.recommendations.application.commandservices.RecommendationCommandService;
import com.jouletracker.platform.recommendations.application.queryservices.RecommendationQueryService;
import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.domain.model.commands.CreateRecommendationCommand;
import com.jouletracker.platform.recommendations.domain.model.queries.GetRecommendationsByUserIdQuery;
import com.jouletracker.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecommendationsControllerTest {

    @Mock
    private RecommendationCommandService commandService;

    @Mock
    private RecommendationQueryService queryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RecommendationsController(commandService, queryService)).build();
    }

    @Test
    void getRecommendationsShouldReturn200() throws Exception {
        Recommendation rec = new Recommendation(
                1L, 1L, "Unplug phantom loads", "Use smart power strips",
                12.0, 8.4, "Standby", false, LocalDateTime.now()
        );
        when(queryService.handle(any(GetRecommendationsByUserIdQuery.class))).thenReturn(List.of(rec));

        mockMvc.perform(get("/api/v1/recommendations").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Unplug phantom loads"))
                .andExpect(jsonPath("$[0].estimatedSavingKwh").value(12.0));
    }

    @Test
    void createRecommendationShouldReturn201WithFrontendAlias() throws Exception {
        Recommendation rec = new Recommendation(
                2L, 1L, "Clean HVAC filters", "Improves airflow efficiency",
                3.5, 2.45, "HVAC", false, LocalDateTime.now()
        );
        when(commandService.handle(any(CreateRecommendationCommand.class))).thenReturn(Result.success(rec));

        mockMvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "Clean HVAC filters",
                                  "description": "Improves airflow efficiency",
                                  "estimatedSavingsKwh": 3.5,
                                  "category": "HVAC"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.estimatedSavingKwh").value(3.5));

        ArgumentCaptor<CreateRecommendationCommand> captor = ArgumentCaptor.forClass(CreateRecommendationCommand.class);
        verify(commandService).handle(captor.capture());
        assertEquals(3.5, captor.getValue().estimatedSavingKwh());
    }

    @Test
    void createRecommendationShouldReturn400WhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "",
                                  "description": "Body",
                                  "category": "General"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
