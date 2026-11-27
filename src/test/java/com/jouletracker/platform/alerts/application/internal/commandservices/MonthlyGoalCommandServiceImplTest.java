package com.jouletracker.platform.alerts.application.internal.commandservices;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;
import com.jouletracker.platform.alerts.domain.model.commands.CreateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.DeleteMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.UpdateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.repositories.MonthlyGoalRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyGoalCommandServiceImplTest {

    @Mock
    private MonthlyGoalRepository monthlyGoalRepository;

    @InjectMocks
    private MonthlyGoalCommandServiceImpl commandService;

    @Test
    void createShouldSaveGoalWhenValidAndNotDuplicate() {
        when(monthlyGoalRepository.existsByUserIdAndYearAndMonth(1L, 2026, 11)).thenReturn(false);
        when(monthlyGoalRepository.save(any(MonthlyGoal.class))).thenAnswer(inv -> {
            MonthlyGoal g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });

        Result<MonthlyGoal, ApplicationError> result = commandService.handle(
                new CreateMonthlyGoalCommand(1L, 250.0, 2026, 11)
        );

        assertTrue(result.isSuccess());
        MonthlyGoal saved = ((Result.Success<MonthlyGoal, ApplicationError>) result).value();
        assertEquals(1L, saved.getId());
        assertEquals(250.0, saved.getTargetKwh());
    }

    @Test
    void createShouldReturnConflictWhenGoalAlreadyExistsForMonth() {
        when(monthlyGoalRepository.existsByUserIdAndYearAndMonth(1L, 2026, 11)).thenReturn(true);

        Result<MonthlyGoal, ApplicationError> result = commandService.handle(
                new CreateMonthlyGoalCommand(1L, 250.0, 2026, 11)
        );

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<MonthlyGoal, ApplicationError>) result).error();
        assertEquals("MONTHLY_GOAL_CONFLICT", error.code());
        verify(monthlyGoalRepository, never()).save(any());
    }

    @Test
    void createShouldReturnValidationErrorWhenTargetKwhIsNotPositive() {
        Result<MonthlyGoal, ApplicationError> result = commandService.handle(
                new CreateMonthlyGoalCommand(1L, 0.0, 2026, 11)
        );

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<MonthlyGoal, ApplicationError>) result).error();
        assertEquals("VALIDATION_ERROR", error.code());
        verifyNoInteractions(monthlyGoalRepository);
    }

    @Test
    void updateShouldModifyTargetKwhWhenFound() {
        MonthlyGoal existing = new MonthlyGoal(10L, 1L, 200.0, 2026, 11);
        when(monthlyGoalRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(monthlyGoalRepository.save(any(MonthlyGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<MonthlyGoal, ApplicationError> result = commandService.handle(
                new UpdateMonthlyGoalCommand(10L, 1L, 320.0, null, null)
        );

        assertTrue(result.isSuccess());
        MonthlyGoal updated = ((Result.Success<MonthlyGoal, ApplicationError>) result).value();
        assertEquals(320.0, updated.getTargetKwh());
    }

    @Test
    void deleteShouldRemoveGoalWhenFound() {
        MonthlyGoal existing = new MonthlyGoal(10L, 1L, 200.0, 2026, 11);
        when(monthlyGoalRepository.findById(10L)).thenReturn(Optional.of(existing));

        Result<Void, ApplicationError> result = commandService.handle(new DeleteMonthlyGoalCommand(10L, 1L));

        assertTrue(result.isSuccess());
        verify(monthlyGoalRepository).deleteById(10L);
    }

    @Test
    void deleteShouldReturnNotFoundWhenMissing() {
        when(monthlyGoalRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Void, ApplicationError> result = commandService.handle(new DeleteMonthlyGoalCommand(999L, 1L));

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<Void, ApplicationError>) result).error();
        assertEquals("MONTHLY_GOAL_NOT_FOUND", error.code());
    }
}