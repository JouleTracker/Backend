package com.jouletracker.platform.alerts.interfaces.rest.transform;

import com.jouletracker.platform.alerts.domain.model.commands.CreateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.UpdateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.entities.MonthlyGoalProgress;
import com.jouletracker.platform.alerts.interfaces.rest.resources.CreateMonthlyGoalResource;
import com.jouletracker.platform.alerts.interfaces.rest.resources.MonthlyGoalResource;
import com.jouletracker.platform.alerts.interfaces.rest.resources.UpdateMonthlyGoalResource;

/**
 * Assembler translating MonthlyGoal commands and progress read-models into REST resources (US-28).
 */
public final class MonthlyGoalResourceAssembler {

    private MonthlyGoalResourceAssembler() {
    }

    public static CreateMonthlyGoalCommand toCreateCommandFromResource(CreateMonthlyGoalResource resource, Long resolvedUserId) {
        return new CreateMonthlyGoalCommand(
                resolvedUserId,
                resource.targetKwh(),
                resource.year(),
                resource.month()
        );
    }

    public static UpdateMonthlyGoalCommand toUpdateCommandFromResource(Long id, Long resolvedUserId, UpdateMonthlyGoalResource resource) {
        return new UpdateMonthlyGoalCommand(
                id,
                resolvedUserId,
                resource.targetKwh(),
                resource.year(),
                resource.month()
        );
    }

    public static MonthlyGoalResource toResourceFromProgress(MonthlyGoalProgress progress) {
        if (progress == null) {
            return null;
        }
        return new MonthlyGoalResource(
                progress.id(),
                progress.userId(),
                progress.targetKwh(),
                progress.year(),
                progress.month(),
                progress.currentKwh(),
                progress.progressPercentage(),
                progress.remainingKwh(),
                progress.exceeded()
        );
    }
}