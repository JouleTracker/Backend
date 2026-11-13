package com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.entities.BusinessAreaPersistenceEntity;

/**
 * Assembler converting between BusinessArea domain aggregate
 * and its JPA persistence entity.
 */
public final class BusinessAreaPersistenceAssembler {

    private BusinessAreaPersistenceAssembler() {
    }

    public static BusinessArea toDomainFromPersistence(BusinessAreaPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new BusinessArea(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getFloor(),
                entity.getDescription(),
                entity.getIcon()
        );
    }

    public static BusinessAreaPersistenceEntity toPersistenceFromDomain(BusinessArea domain) {
        if (domain == null) {
            return null;
        }

        BusinessAreaPersistenceEntity entity = new BusinessAreaPersistenceEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setFloor(domain.getFloor());
        entity.setDescription(domain.getDescription());
        entity.setIcon(domain.getIcon());

        return entity;
    }
}
