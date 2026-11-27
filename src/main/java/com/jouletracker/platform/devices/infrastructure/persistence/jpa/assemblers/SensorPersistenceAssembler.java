
package com.jouletracker.platform.devices.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.valueobjects.SerialNumber;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities.SensorPersistenceEntity;

/**
 * Assembler converting between Sensor domain aggregate and JPA entity.
 */
public final class SensorPersistenceAssembler {

    private SensorPersistenceAssembler() {
    }

    public static Sensor toDomainFromPersistence(SensorPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        Sensor sensor = new Sensor(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getModel(),
                new SerialNumber(entity.getSerialNumber()),
                entity.getStatus(),
                entity.getCurrentPowerKw(),
                entity.getTodayKwh(),
                entity.getAssignedDeviceId(),
                entity.getAssignedDeviceName(),
                entity.getAssignedDeviceCategory(),
                entity.getApplianceProfileId(),
                entity.getApplianceProfileName(),
                entity.getRecommendedDailyKwh(),
                entity.getLastSync()
        );

        // US-32: Restore business area association.
        if (entity.getBusinessAreaId() != null) {
            sensor.assignBusinessArea(entity.getBusinessAreaId());
        }

        return sensor;
    }

    public static SensorPersistenceEntity toPersistenceFromDomain(Sensor domain) {
        if (domain == null) {
            return null;
        }

        SensorPersistenceEntity entity = new SensorPersistenceEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());

        // US-32: Persist business area association.
        entity.setBusinessAreaId(domain.getBusinessAreaId());

        entity.setName(domain.getName());
        entity.setModel(domain.getModel());
        entity.setSerialNumber(domain.getSerialNumber() != null ? domain.getSerialNumber().value() : null);
        entity.setStatus(domain.getStatus());
        entity.setCurrentPowerKw(domain.getCurrentPowerKw());
        entity.setTodayKwh(domain.getTodayKwh());
        entity.setAssignedDeviceId(domain.getAssignedDeviceId());
        entity.setAssignedDeviceName(domain.getAssignedDeviceName());
        entity.setAssignedDeviceCategory(domain.getAssignedDeviceCategory());
        entity.setApplianceProfileId(domain.getApplianceProfileId());
        entity.setApplianceProfileName(domain.getApplianceProfileName());
        entity.setRecommendedDailyKwh(domain.getRecommendedDailyKwh());
        entity.setLastSync(domain.getLastSync());

        return entity;
    }
}
