
package com.jouletracker.platform.businessenergy.interfaces.acl;

import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Anti-Corruption Layer exposing business energy capabilities
 * to other bounded contexts.
 *
 * US-32: Provides business area information.
 * TS-19: Provides shared notification recipients.
 */
@Service
@NullMarked
@Transactional(readOnly = true)
public class BusinessEnergyContextFacade {

    private final BusinessAreaRepository businessAreaRepository;

    public BusinessEnergyContextFacade(
            BusinessAreaRepository businessAreaRepository) {

        this.businessAreaRepository = businessAreaRepository;
    }

    /**
     * Checks whether a business area belongs to a user.
     */
    public boolean existsBusinessAreaForUser(
            @Nullable Long businessAreaId,
            @Nullable Long userId) {

        if (businessAreaId == null || businessAreaId <= 0
                || userId == null || userId <= 0) {

            return false;
        }

        return businessAreaRepository
                .findById(businessAreaId)
                .filter(area -> Objects.equals(
                        area.getUserId(), userId))
                .isPresent();
    }

    /**
     * Retrieves business area information and shared
     * notification recipients for the specified owner.
     */
    public Optional<BusinessAreaNotificationSnapshot>
    fetchBusinessAreaNotificationSnapshot(
            @Nullable Long businessAreaId,
            @Nullable Long userId) {

        if (businessAreaId == null || businessAreaId <= 0
                || userId == null || userId <= 0) {

            return Optional.empty();
        }

        return businessAreaRepository
                .findById(businessAreaId)
                .filter(area -> Objects.equals(
                        area.getUserId(), userId))
                .map(area -> new BusinessAreaNotificationSnapshot(
                        area.getId(),
                        area.getUserId(),
                        area.getName(),
                        area.getNotificationRecipients()
                                .stream()
                                .sorted()
                                .toList()
                ));
    }

    /**
     * Read-only business area notification information
     * exposed to other bounded contexts.
     */
    public record BusinessAreaNotificationSnapshot(
            Long businessAreaId,
            Long userId,
            String businessAreaName,
            List<String> notificationRecipients
    ) {

        public BusinessAreaNotificationSnapshot {

            notificationRecipients =
                    List.copyOf(notificationRecipients);
        }
    }
}
