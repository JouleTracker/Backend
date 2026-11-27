package com.jouletracker.platform.alerts.domain.model.aggregates;

import com.jouletracker.platform.alerts.domain.model.commands.CreateMonthlyGoalCommand;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Aggregate Root representing a user's monthly energy consumption goal in kWh (US-28).
 */
@Getter
@Setter
public class MonthlyGoal extends AbstractDomainAggregateRoot<MonthlyGoal> {

    private Long id;
    private Long userId;
    private Double targetKwh;
    private Integer year;
    private Integer month;

    public MonthlyGoal() {
        LocalDate now = LocalDate.now();
        this.year = now.getYear();
        this.month = now.getMonthValue();
        this.targetKwh = 0.0;
    }

    public MonthlyGoal(Long id, Long userId, Double targetKwh, Integer year, Integer month) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        if (targetKwh == null || targetKwh <= 0) {
            throw new IllegalArgumentException("targetKwh must be greater than zero");
        }
        LocalDate now = LocalDate.now();
        int resolvedYear = year != null ? year : now.getYear();
        int resolvedMonth = month != null ? month : now.getMonthValue();
        if (resolvedMonth < 1 || resolvedMonth > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        this.targetKwh = targetKwh;
        this.year = resolvedYear;
        this.month = resolvedMonth;
    }

    public MonthlyGoal(CreateMonthlyGoalCommand command) {
        this(null, command.userId(), command.targetKwh(), command.year(), command.month());
    }

    public void updateGoal(Double targetKwh, Integer year, Integer month) {
        if (targetKwh != null) {
            if (targetKwh <= 0) {
                throw new IllegalArgumentException("targetKwh must be greater than zero");
            }
            this.targetKwh = targetKwh;
        }
        if (year != null) {
            this.year = year;
        }
        if (month != null) {
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("month must be between 1 and 12");
            }
            this.month = month;
        }
    }
}