package com.jouletracker.platform.devices.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.devices.domain.model.entities.ApplianceProfile;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities.ApplianceProfilePersistenceEntity;

/**
 * Assembler converting between ApplianceProfile domain entity and JPA entity.
 */
public final class ApplianceProfilePersistenceAssembler {

    private ApplianceProfilePersistenceAssembler() {
    }

    public static ApplianceProfile toDomainFromPersistence(ApplianceProfilePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ApplianceProfile(
                entity.getId(),
                entity.getName(),
                entity.getRecommendedDailyKwh(),
                entity.getTypicalPowerWatts(),
                entity.getIcon()
        );
    }

    public static ApplianceProfilePersistenceEntity toPersistenceFromDomain(ApplianceProfile domain) {
        if (domain == null) {
            return null;
        }
        ApplianceProfilePersistenceEntity entity = new ApplianceProfilePersistenceEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setRecommendedDailyKwh(domain.getRecommendedDailyKwh());
        entity.setTypicalPowerWatts(domain.getTypicalPowerWatts());
        entity.setIcon(domain.getIcon());
        return entity;
    }
}
