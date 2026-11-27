package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.commandservices.MonthlyGoalCommandService;
import com.jouletracker.platform.alerts.application.queryservices.MonthlyGoalQueryService;
import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;
import com.jouletracker.platform.alerts.domain.model.commands.CreateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.DeleteMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.UpdateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.entities.MonthlyGoalProgress;
import com.jouletracker.platform.alerts.domain.model.queries.GetCurrentMonthlyGoalQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetMonthlyGoalsByUserIdQuery;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MonthlyGoalsControllerTest {

    @Mock
    private MonthlyGoalCommandService commandService;

    @Mock
    private MonthlyGoalQueryService queryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MonthlyGoalsController controller = new MonthlyGoalsController(commandService, queryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createMonthlyGoalShouldReturn201Created() throws Exception {
        MonthlyGoal saved = new MonthlyGoal(1L, 1L, 250.0, 2026, 11);
        MonthlyGoalProgress progress = new MonthlyGoalProgress(1L, 1L, 250.0, 2026, 11, 50.0, 20.0, 200.0, false);

        when(commandService.handle(any(CreateMonthlyGoalCommand.class))).thenReturn(Result.success(saved));
        when(queryService.toProgress(saved)).thenReturn(progress);

        String payload = """
                {
                  "userId": 1,
                  "targetKwh": 250.0,
                  "year": 2026,
                  "month": 11
                }
                """;

        mockMvc.perform(post("/api/v1/monthly-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.targetKwh").value(250.0))
                .andExpect(jsonPath("$.progressPercentage").value(20.0));
    }

    @Test
    void createMonthlyGoalShouldReturn409ConflictWhenDuplicate() throws Exception {
        when(commandService.handle(any(CreateMonthlyGoalCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("MONTHLY_GOAL", "Already exists")));

        String payload = """
                {
                  "userId": 1,
                  "targetKwh": 250.0,
                  "year": 2026,
                  "month": 11
                }
                """;

        mockMvc.perform(post("/api/v1/monthly-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getMonthlyGoalsShouldReturn200WithProgressList() throws Exception {
        MonthlyGoalProgress progress = new MonthlyGoalProgress(1L, 1L, 300.0, 2026, 11, 150.0, 50.0, 150.0, false);
        when(queryService.handle(any(GetMonthlyGoalsByUserIdQuery.class))).thenReturn(List.of(progress));

        mockMvc.perform(get("/api/v1/monthly-goals").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].currentKwh").value(150.0))
                .andExpect(jsonPath("$[0].remainingKwh").value(150.0));
    }

    @Test
    void getCurrentMonthlyGoalShouldReturn200WhenFound() throws Exception {
        MonthlyGoalProgress progress = new MonthlyGoalProgress(1L, 1L, 250.0, 2026, 11, 100.0, 40.0, 150.0, false);
        when(queryService.handle(any(GetCurrentMonthlyGoalQuery.class))).thenReturn(Optional.of(progress));

        mockMvc.perform(get("/api/v1/monthly-goals/current").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetKwh").value(250.0))
                .andExpect(jsonPath("$.exceeded").value(false));
    }

    @Test
    void getCurrentMonthlyGoalShouldReturn404WhenNotConfigured() throws Exception {
        when(queryService.handle(any(GetCurrentMonthlyGoalQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/monthly-goals/current").param("userId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMonthlyGoalShouldReturn200WhenFound() throws Exception {
        MonthlyGoal updated = new MonthlyGoal(1L, 1L, 350.0, 2026, 11);
        MonthlyGoalProgress progress = new MonthlyGoalProgress(1L, 1L, 350.0, 2026, 11, 100.0, 28.57, 250.0, false);

        when(commandService.handle(any(UpdateMonthlyGoalCommand.class))).thenReturn(Result.success(updated));
        when(queryService.toProgress(updated)).thenReturn(progress);

        mockMvc.perform(put("/api/v1/monthly-goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetKwh\": 350.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetKwh").value(350.0));
    }

    @Test
    void deleteMonthlyGoalShouldReturn204WhenFound() throws Exception {
        when(commandService.handle(any(DeleteMonthlyGoalCommand.class))).thenReturn(Result.success(null));

        mockMvc.perform(delete("/api/v1/monthly-goals/1"))
                .andExpect(status().isNoContent());
    }
}