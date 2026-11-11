package com.jouletracker.platform.businessenergy.interfaces.rest.transform;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.BusinessAreaResource;

/**
 * Assembler to translate a BusinessArea domain aggregate
 * into a REST response resource.
 */
public final class BusinessAreaResourceFromEntityAssembler {

    private BusinessAreaResourceFromEntityAssembler() {
    }

    public static BusinessAreaResource toResourceFromEntity(BusinessArea entity) {
        if (entity == null) {
            return null;
        }

        return new BusinessAreaResource(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getFloor(),
                entity.getDescription(),
                entity.getIcon()
        );
    }
}
