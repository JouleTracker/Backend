package com.jouletracker.platform.businessenergy.domain.repositories;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for BusinessArea aggregate.
 */
public interface BusinessAreaRepository {

    List<BusinessArea> findAllByUserId(Long userId);

    Optional<BusinessArea> findById(Long id);

    BusinessArea save(BusinessArea businessArea);

    boolean existsByUserIdAndName(Long userId, String name);

    void deleteById(Long id);
}
