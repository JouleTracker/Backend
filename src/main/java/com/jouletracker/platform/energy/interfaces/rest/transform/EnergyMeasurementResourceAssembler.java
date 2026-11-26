package com.jouletracker.platform.energy.interfaces.rest.transform;

import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.commands.RegisterEnergyMeasurementCommand;
import com.jouletracker.platform.energy.domain.model.entities.*;
import com.jouletracker.platform.energy.interfaces.rest.resources.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Assembler translating domain entities and aggregates into REST resources for the Energy context.
 */
public final class EnergyMeasurementResourceAssembler {

    private EnergyMeasurementResourceAssembler() {
    }

    public static ConsumptionSummaryResource toResourceFromEntity(ConsumptionSummary entity) {
        if (entity == null) {
            return null;
        }
        return new ConsumptionSummaryResource(
                entity.userId(),
                entity.period() != null ? entity.period().getCode() : "daily",
                entity.totalKwh(),
                entity.totalCost(),
                entity.averagePowerKw(),
                entity.peakPowerKw(),
                entity.variationPercentage(),
                entity.avoidedEmissionsKg(),
                entity.activeDevicesCount()
        );
    }

    public static EnergyReadingResource toResourceFromEntity(EnergyReading entity) {
        if (entity == null) {
            return null;
        }
        return new EnergyReadingResource(
                entity.id(),
                entity.label(),
                entity.timestamp(),
                entity.kwh(),
                entity.powerKw(),
                entity.cost(),
                entity.deviceId(),
                entity.deviceName()
        );
    }

    public static EnergyReadingResource toReadingResourceFromAggregate(EnergyMeasurement measurement) {
        if (measurement == null) {
            return null;
        }
        LocalDateTime measuredAt = measurement.getMeasuredAt() != null ? measurement.getMeasuredAt() : LocalDateTime.now();
        return new EnergyReadingResource(
                measurement.getId(),
                measuredAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                measuredAt,
                measurement.getEnergyKwh(),
                measurement.getPowerKw(),
                measurement.getCost(),
                measurement.getDeviceId(),
                measurement.getDeviceName()
        );
    }

    public static DeviceDistributionResource toResourceFromEntity(DeviceDistribution entity) {
        if (entity == null) {
            return null;
        }
        return new DeviceDistributionResource(
                entity.deviceId(),
                entity.deviceName(),
                entity.category(),
                entity.totalKwh(),
                entity.totalCost(),
                entity.percentage()
        );
    }

    public static ComparativeConsumptionResource toResourceFromEntity(ComparativeConsumption entity) {
        if (entity == null) {
            return null;
        }
        return new ComparativeConsumptionResource(
                entity.label(),
                entity.currentPeriodKwh(),
                entity.previousPeriodKwh(),
                entity.currentPeriodCost(),
                entity.previousPeriodCost(),
                entity.differencePercentage()
        );
    }

    public static ConsumptionHistoryResource toResourceFromEntity(ConsumptionHistory entity) {
        if (entity == null) {
            return null;
        }
        return new ConsumptionHistoryResource(
                entity.id(),
                entity.deviceId(),
                entity.deviceName(),
                entity.category(),
                entity.powerKw(),
                entity.energyKwh(),
                entity.cost(),
                entity.status(),
                entity.recordedAt()
        );
    }

    public static RegisterEnergyMeasurementCommand toCommandFromMap(Long userId, Long sensorId, Long deviceId,
                                                                    String deviceName, String deviceCategory,
                                                                    Double powerKw, Double energyKwh,
                                                                    Double voltage, Double currentAmps) {
        return new RegisterEnergyMeasurementCommand(
                userId != null ? userId : 1L,
                sensorId,
                deviceId,
                deviceName,
                deviceCategory,
                powerKw != null ? powerKw : 0.0,
                energyKwh != null ? energyKwh : 0.0,
                voltage,
                currentAmps,
                LocalDateTime.now()
        );
    }

    public static TelemetryReadingResource toTelemetryResourceFromAggregate(EnergyMeasurement measurement) {
        if (measurement == null) {
            return null;
        }
        return new TelemetryReadingResource(
                measurement.getId(),
                measurement.getUserId(),
                measurement.getSensorId(),
                measurement.getDeviceId(),
                measurement.getDeviceName(),
                measurement.getDeviceCategory(),
                measurement.getPowerKw(),
                measurement.getEnergyKwh(),
                measurement.getVoltage(),
                measurement.getCurrentAmps(),
                measurement.getCost(),
                measurement.getMeasuredAt()
        );
    }

    public static RegisterEnergyMeasurementCommand toCommandFromTelemetryResource(
            RegisterTelemetryReadingResource resource, Long queryUserId) {
        Long resolvedUserId = queryUserId != null ? queryUserId : (resource.userId() != null ? resource.userId() : 1L);
        Double resolvedEnergyKwh = resource.energyKwh() != null
                ? resource.energyKwh()
                : (resource.kwh() != null ? resource.kwh() : 0.0);
        return new RegisterEnergyMeasurementCommand(
                resolvedUserId,
                resource.sensorId(),
                resource.deviceId(),
                resource.deviceName(),
                resource.deviceCategory(),
                resource.powerKw() != null ? resource.powerKw() : 0.0,
                resolvedEnergyKwh,
                resource.voltage(),
                resource.currentAmps(),
                resource.measuredAt() != null ? resource.measuredAt() : LocalDateTime.now()
        );
    }
}