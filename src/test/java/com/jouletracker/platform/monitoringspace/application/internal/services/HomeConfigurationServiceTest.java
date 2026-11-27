
package com.jouletracker.platform.monitoringspace.application.internal.services;

import com.jouletracker.platform.monitoringspace.domain.model.aggregates.HomeConfiguration;
import com.jouletracker.platform.monitoringspace.domain.repositories.HomeConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeConfigurationServiceTest {

    @Mock
    private HomeConfigurationRepository homeRepository;

    @InjectMocks
    private HomeConfigurationService homeService;

    @Test
    void shouldReturnEmptyWhenHomeIsNotConfigured() {

        when(homeRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertTrue(
                homeService.getConfiguration(1L).isEmpty()
        );
    }

    @Test
    void shouldReturnExistingConfiguration() {

        var configuration = new HomeConfiguration(
                1L, "House", 4, "Lima", "BT5B"
        );

        when(homeRepository.findByUserId(1L))
                .thenReturn(Optional.of(configuration));

        var result = homeService
                .getConfiguration(1L)
                .orElseThrow();

        assertEquals("House", result.getHomeType());
        assertEquals(4, result.getOccupants());
        assertEquals("Lima", result.getLocation());
        assertEquals("BT5B", result.getTariff());
    }

    @Test
    void shouldCreateHomeConfiguration() {

        when(homeRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(homeRepository.save(any(HomeConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = homeService.updateConfiguration(
                1L,
                "Apartment",
                3,
                "Chorrillos",
                "BT5B"
        );

        assertEquals(1L, result.getUserId());
        assertEquals("Apartment", result.getHomeType());
        assertEquals(3, result.getOccupants());
        assertEquals("Chorrillos", result.getLocation());
        assertEquals("BT5B", result.getTariff());

        verify(homeRepository).save(result);
    }

    @Test
    void shouldUpdateExistingConfiguration() {

        var existing = new HomeConfiguration(
                1L, "House", 2, "Lima", "BT5B"
        );

        when(homeRepository.findByUserId(1L))
                .thenReturn(Optional.of(existing));

        when(homeRepository.save(existing))
                .thenReturn(existing);

        var result = homeService.updateConfiguration(
                1L,
                "Apartment",
                5,
                "Miraflores",
                "BT2"
        );

        assertEquals("Apartment", result.getHomeType());
        assertEquals(5, result.getOccupants());
        assertEquals("Miraflores", result.getLocation());
        assertEquals("BT2", result.getTariff());

        verify(homeRepository).save(existing);
    }

    @Test
    void shouldRejectInvalidHomeType() {

        when(homeRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> homeService.updateConfiguration(
                        1L, "Invalid", 3, "Lima", "BT5B"
                )
        );

        verify(homeRepository, never())
                .save(any(HomeConfiguration.class));
    }

    @Test
    void shouldRejectInvalidNumberOfOccupants() {

        when(homeRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> homeService.updateConfiguration(
                        1L, "House", 0, "Lima", "BT5B"
                )
        );

        verify(homeRepository, never())
                .save(any(HomeConfiguration.class));
    }

    @Test
    void shouldRejectInvalidUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> homeService.getConfiguration(0L)
        );

        verifyNoInteractions(homeRepository);
    }

    @Test
    void shouldNormalizeLocationAndTariff() {

        when(homeRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(homeRepository.save(any(HomeConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = homeService.updateConfiguration(
                1L,
                "House",
                3,
                "  Chorrillos  ",
                "  BT5B  "
        );

        assertEquals("Chorrillos", result.getLocation());
        assertEquals("BT5B", result.getTariff());
    }

    @Test
    void shouldRejectExcessiveLocationLength() {

        when(homeRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> homeService.updateConfiguration(
                        1L, "House", 3, "A".repeat(256), "BT5B"
                )
        );

        verify(homeRepository, never())
                .save(any(HomeConfiguration.class));
    }

    @Test
    void shouldProvideDefaultHomeConfiguration() {

        var configuration = new HomeConfiguration(1L);

        assertEquals(1L, configuration.getUserId());
        assertEquals("House", configuration.getHomeType());
        assertEquals(1, configuration.getOccupants());
        assertEquals("", configuration.getLocation());
        assertEquals("", configuration.getTariff());
    }
}
