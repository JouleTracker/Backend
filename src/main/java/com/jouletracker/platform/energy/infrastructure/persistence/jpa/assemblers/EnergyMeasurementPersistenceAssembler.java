package com.jouletracker.platform.energy.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.infrastructure.persistence.jpa.entities.EnergyMeasurementPersistenceEntity;

/**
 * Assembler converting between EnergyMeasurement domain aggregate and JPA entity.
 */
public final class EnergyMeasurementPersistenceAssembler {

    private EnergyMeasurementPersistenceAssembler() {
    }

    public static EnergyMeasurement toDomainFromPersistence(EnergyMeasurementPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new EnergyMeasurement(
                entity.getId(),
                entity.getUserId(),
                entity.getSensorId(),
                entity.getDeviceId(),
                entity.getDeviceName(),
                entity.getDeviceCategory(),
                entity.getPowerKw(),
                entity.getEnergyKwh(),
                entity.getVoltage(),
                entity.getCurrentAmps(),
                entity.getCost(),
                entity.getMeasuredAt()
        );
    }

    public static EnergyMeasurementPersistenceEntity toPersistenceFromDomain(EnergyMeasurement domain) {
        if (domain == null) {
            return null;
        }
        EnergyMeasurementPersistenceEntity entity = new EnergyMeasurementPersistenceEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setSensorId(domain.getSensorId());
        entity.setDeviceId(domain.getDeviceId());
        entity.setDeviceName(domain.getDeviceName());
        entity.setDeviceCategory(domain.getDeviceCategory());
        entity.setPowerKw(domain.getPowerKw());
        entity.setEnergyKwh(domain.getEnergyKwh());
        entity.setVoltage(domain.getVoltage());
        entity.setCurrentAmps(domain.getCurrentAmps());
        entity.setCost(domain.getCost());
        entity.setMeasuredAt(domain.getMeasuredAt());
        return entity;
    }
}