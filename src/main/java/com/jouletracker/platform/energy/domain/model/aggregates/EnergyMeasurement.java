package com.jouletracker.platform.energy.domain.model.aggregates;

import com.jouletracker.platform.energy.domain.model.commands.RegisterEnergyMeasurementCommand;
import com.jouletracker.platform.energy.domain.model.events.EnergyMeasurementRegisteredEvent;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root representing a single electrical telemetry measurement recorded in JouleTracker.
 */
@Getter
@Setter
public class EnergyMeasurement extends AbstractDomainAggregateRoot<EnergyMeasurement> {

    private static final double DEFAULT_TARIFF_PEN_PER_KWH = 0.70;
    private static final double CO2_KG_PER_KWH = 0.45;

    private Long id;
    private Long userId;
    private Long sensorId;
    private Long deviceId;
    private String deviceName;
    private String deviceCategory;
    private Double powerKw;
    private Double energyKwh;
    private Double voltage;
    private Double currentAmps;
    private Double cost;
    private LocalDateTime measuredAt;

    public EnergyMeasurement() {
        this.powerKw = 0.0;
        this.energyKwh = 0.0;
        this.cost = 0.0;
        this.measuredAt = LocalDateTime.now();
    }

    public EnergyMeasurement(Long id, Long userId, Long sensorId, Long deviceId,
                             String deviceName, String deviceCategory,
                             Double powerKw, Double energyKwh, Double voltage,
                             Double currentAmps, Double cost, LocalDateTime measuredAt) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.sensorId = sensorId;
        this.deviceId = deviceId;
        this.deviceName = (deviceName != null && !deviceName.isBlank()) ? deviceName.trim() : "Dispositivo General";
        this.deviceCategory = (deviceCategory != null && !deviceCategory.isBlank()) ? deviceCategory.trim() : "Electrodomésticos";
        this.powerKw = powerKw != null ? Math.max(0.0, powerKw) : 0.0;
        this.energyKwh = energyKwh != null ? Math.max(0.0, energyKwh) : 0.0;
        this.voltage = voltage != null ? voltage : 220.0;
        this.currentAmps = currentAmps != null ? currentAmps : calculateCurrentAmps(this.powerKw, this.voltage);
        this.cost = cost != null ? cost : calculateCost(this.energyKwh);
        this.measuredAt = measuredAt != null ? measuredAt : LocalDateTime.now();
    }

    public EnergyMeasurement(RegisterEnergyMeasurementCommand command) {
        this(
                null,
                command.userId(),
                command.sensorId(),
                command.deviceId(),
                command.deviceName(),
                command.deviceCategory(),
                command.powerKw(),
                command.energyKwh(),
                command.voltage(),
                command.currentAmps(),
                null,
                command.measuredAt()
        );
    }

    public void markAsRegistered() {
        registerDomainEvent(new EnergyMeasurementRegisteredEvent(
                this.id,
                this.userId,
                this.sensorId,
                this.deviceId,
                this.deviceName,
                this.powerKw,
                this.energyKwh,
                this.measuredAt
        ));
    }

    public static double calculateCost(double kwh) {
        return Math.round(kwh * DEFAULT_TARIFF_PEN_PER_KWH * 100.0) / 100.0;
    }

    public static double calculateAvoidedEmissionsKg(double kwhSaved) {
        return Math.round(Math.max(0.0, kwhSaved) * CO2_KG_PER_KWH * 100.0) / 100.0;
    }

    private static double calculateCurrentAmps(double powerKw, double voltage) {
        if (voltage <= 0) {
            return 0.0;
        }
        return Math.round(((powerKw * 1000.0) / voltage) * 100.0) / 100.0;
    }
}