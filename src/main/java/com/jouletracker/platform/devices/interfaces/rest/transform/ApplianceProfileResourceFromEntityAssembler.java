package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.entities.ApplianceProfile;
import com.jouletracker.platform.devices.interfaces.rest.resources.ApplianceProfileResource;

/**
 * Assembler to translate {@link ApplianceProfile} domain entity into {@link ApplianceProfileResource}.
 */
public class ApplianceProfileResourceFromEntityAssembler {

    public static ApplianceProfileResource toResourceFromEntity(ApplianceProfile entity) {
        if (entity == null) {
            return null;
        }

        return new ApplianceProfileResource(
                entity.getId(),
                entity.getName(),
                entity.getRecommendedDailyKwh(),
                entity.getTypicalPowerWatts(),
                entity.getIcon()
        );
    }
}
