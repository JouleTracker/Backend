package com.jouletracker.platform.devices.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities.DevicePersistenceEntity;

/**
 * Assembler converting between Device domain aggregate and JPA entity.
 */
public final class DevicePersistenceAssembler {

    private DevicePersistenceAssembler() {
    }

    public static Device toDomainFromPersistence(DevicePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Device(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getLocation(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getCurrentPowerKw(),
                entity.getPowerLimitKw(),
                entity.getMaintenanceDueDate(),
                entity.getTodayKwh(),
                entity.getLastActivity()
        );
    }

    public static DevicePersistenceEntity toPersistenceFromDomain(Device domain) {
        if (domain == null) {
            return null;
        }
        DevicePersistenceEntity entity = new DevicePersistenceEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setLocation(domain.getLocation());
        entity.setCategory(domain.getCategory());
        entity.setStatus(domain.getStatus());
        entity.setCurrentPowerKw(domain.getCurrentPowerKw());
        entity.setPowerLimitKw(domain.getPowerLimitKw());
        entity.setMaintenanceDueDate(domain.getMaintenanceDueDate());
        entity.setTodayKwh(domain.getTodayKwh());
        entity.setLastActivity(domain.getLastActivity());
        return entity;
    }
}
