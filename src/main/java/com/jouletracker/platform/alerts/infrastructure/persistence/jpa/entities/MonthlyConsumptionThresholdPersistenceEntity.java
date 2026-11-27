
package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * US-26: JPA persistence entity for the
 * user's global monthly consumption threshold.
 */
@Getter
@Setter
@Entity
@Table(name = "monthly_consumption_thresholds")
public class MonthlyConsumptionThresholdPersistenceEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(
            name = "monthly_limit_kwh",
            nullable = false,
            precision = 12,
            scale = 3
    )
    private BigDecimal monthlyLimitKwh;

    public MonthlyConsumptionThresholdPersistenceEntity() {
    }
}
