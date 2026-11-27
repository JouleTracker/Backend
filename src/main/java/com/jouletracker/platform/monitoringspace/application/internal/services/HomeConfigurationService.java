
package com.jouletracker.platform.monitoringspace.application.internal.services;

import com.jouletracker.platform.monitoringspace.domain.model.aggregates.HomeConfiguration;
import com.jouletracker.platform.monitoringspace.domain.repositories.HomeConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * US-48: Application service for managing
 * persistent home monitoring configurations.
 */
@Service
@Transactional(readOnly = true)
public class HomeConfigurationService {

    private final HomeConfigurationRepository homeRepository;

    public HomeConfigurationService(
            HomeConfigurationRepository homeRepository) {

        this.homeRepository = homeRepository;
    }

    public Optional<HomeConfiguration> getConfiguration(
            Long userId) {

        validateUserId(userId);

        return homeRepository.findByUserId(userId);
    }

    @Transactional
    public HomeConfiguration updateConfiguration(
            Long userId,
            String homeType,
            Integer occupants,
            String location,
            String tariff) {

        validateUserId(userId);

        var configuration = homeRepository.findByUserId(userId)
                .orElseGet(() ->
                        new HomeConfiguration(userId));

        configuration.updateConfiguration(
                homeType,
                occupants,
                location,
                tariff
        );

        return homeRepository.save(configuration);
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {

            throw new IllegalArgumentException(
                    "User ID must be greater than zero"
            );
        }
    }
}
