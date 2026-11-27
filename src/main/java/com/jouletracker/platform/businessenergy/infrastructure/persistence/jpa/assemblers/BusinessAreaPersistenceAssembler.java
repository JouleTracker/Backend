
package com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.entities.BusinessAreaPersistenceEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Assembler converting between BusinessArea domain aggregate
 * and its JPA persistence entity.
 *
 * US-32: Supports shared notification recipients.
 */
public final class BusinessAreaPersistenceAssembler {

    private BusinessAreaPersistenceAssembler() {
    }

    public static BusinessArea toDomainFromPersistence(
            BusinessAreaPersistenceEntity entity) {

        if (entity == null) {
            return null;
        }

        BusinessArea domain = new BusinessArea(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getFloor(),
                entity.getDescription(),
                entity.getIcon()
        );

        // US-32: Restore notification recipients from persistence.
        if (entity.getNotificationRecipients() != null) {

            domain.replaceNotificationRecipients(
                    new ArrayList<>(
                            entity.getNotificationRecipients()
                    )
            );
        }

        return domain;
    }

    public static BusinessAreaPersistenceEntity toPersistenceFromDomain(
            BusinessArea domain) {

        if (domain == null) {
            return null;
        }

        BusinessAreaPersistenceEntity entity =
                new BusinessAreaPersistenceEntity();

        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setFloor(domain.getFloor());
        entity.setDescription(domain.getDescription());
        entity.setIcon(domain.getIcon());

        // US-32: Persist shared notification recipients.
        entity.setNotificationRecipients(
                new LinkedHashSet<>(
                        domain.getNotificationRecipients()
                )
        );

        return entity;
    }
}
