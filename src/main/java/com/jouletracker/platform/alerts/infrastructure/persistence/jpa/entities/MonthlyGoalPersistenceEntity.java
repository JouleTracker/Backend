package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing a monthly energy consumption goal (US-28).
 */
@Getter
@Setter
@Entity
@Table(name = "monthly_goals")
public class MonthlyGoalPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_kwh", nullable = false)
    private Double targetKwh;

    @Column(name = "goal_year", nullable = false)
    private Integer goalYear;

    @Column(name = "goal_month", nullable = false)
    private Integer goalMonth;

    public MonthlyGoalPersistenceEntity() {
    }
}