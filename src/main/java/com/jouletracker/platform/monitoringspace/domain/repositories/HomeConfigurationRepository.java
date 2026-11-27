
package com.jouletracker.platform.monitoringspace.domain.repositories;

import com.jouletracker.platform.monitoringspace.domain.model.aggregates.HomeConfiguration;

import java.util.Optional;

/**
 * US-48: Domain repository for home configurations.
 */
public interface HomeConfigurationRepository {

    Optional<HomeConfiguration> findByUserId(Long userId);

    HomeConfiguration save(HomeConfiguration configuration);
}
