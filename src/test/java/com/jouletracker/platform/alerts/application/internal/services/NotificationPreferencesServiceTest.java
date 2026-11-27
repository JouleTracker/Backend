
package com.jouletracker.platform.alerts.application.internal.services;

import com.jouletracker.platform.alerts.domain.model.aggregates.NotificationPreferences;
import com.jouletracker.platform.alerts.domain.repositories.NotificationPreferencesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * US-38: Unit tests for persistent notification preferences.
 */
@ExtendWith(MockitoExtension.class)
class NotificationPreferencesServiceTest {

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @InjectMocks
    private NotificationPreferencesService preferencesService;

    @Test
    void shouldReturnDefaultPreferencesWhenUserHasNoConfiguration() {

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        NotificationPreferences preferences =
                preferencesService.getPreferences(1L);

        assertEquals(1L, preferences.getUserId());

        assertFalse(preferences.isEmailEnabled());

        assertFalse(preferences.isWhatsappEnabled());

        verify(preferencesRepository, never())
                .save(any(NotificationPreferences.class));
    }

    @Test
    void shouldReturnExistingPreferences() {

        NotificationPreferences existing =
                new NotificationPreferences(
                        1L,
                        true,
                        false
                );

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.of(existing));

        NotificationPreferences result =
                preferencesService.getPreferences(1L);

        assertEquals(1L, result.getUserId());

        assertTrue(result.isEmailEnabled());

        assertFalse(result.isWhatsappEnabled());

        verify(preferencesRepository)
                .findByUserId(1L);
    }

    @Test
    void shouldCreatePreferencesWhenTheyDoNotExist() {

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(preferencesRepository.save(
                any(NotificationPreferences.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        NotificationPreferences result =
                preferencesService.updatePreferences(
                        1L,
                        true,
                        true
                );

        assertEquals(1L, result.getUserId());

        assertTrue(result.isEmailEnabled());

        assertTrue(result.isWhatsappEnabled());

        ArgumentCaptor<NotificationPreferences> captor =
                ArgumentCaptor.forClass(
                        NotificationPreferences.class
                );

        verify(preferencesRepository)
                .save(captor.capture());

        NotificationPreferences saved =
                captor.getValue();

        assertEquals(1L, saved.getUserId());

        assertTrue(saved.isEmailEnabled());

        assertTrue(saved.isWhatsappEnabled());
    }

    @Test
    void shouldUpdateExistingPreferences() {

        NotificationPreferences existing =
                new NotificationPreferences(
                        1L,
                        false,
                        true
                );

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.of(existing));

        when(preferencesRepository.save(
                any(NotificationPreferences.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        NotificationPreferences result =
                preferencesService.updatePreferences(
                        1L,
                        true,
                        false
                );

        assertEquals(1L, result.getUserId());

        assertTrue(result.isEmailEnabled());

        assertFalse(result.isWhatsappEnabled());

        verify(preferencesRepository, times(1))
                .save(existing);
    }

    @Test
    void shouldAllowDisablingAllNotificationChannels() {

        NotificationPreferences existing =
                new NotificationPreferences(
                        1L,
                        true,
                        true
                );

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.of(existing));

        when(preferencesRepository.save(
                any(NotificationPreferences.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        NotificationPreferences result =
                preferencesService.updatePreferences(
                        1L,
                        false,
                        false
                );

        assertFalse(result.isEmailEnabled());

        assertFalse(result.isWhatsappEnabled());

        assertFalse(result.hasEnabledChannels());

        verify(preferencesRepository)
                .save(existing);
    }

    @Test
    void shouldReturnTrueWhenEmailIsEnabled() {

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.of(
                        new NotificationPreferences(
                                1L,
                                true,
                                false
                        )
                ));

        assertTrue(
                preferencesService.isEmailEnabled(1L)
        );
    }

    @Test
    void shouldReturnFalseWhenEmailIsDisabled() {

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.of(
                        new NotificationPreferences(
                                1L,
                                false,
                                true
                        )
                ));

        assertFalse(
                preferencesService.isEmailEnabled(1L)
        );
    }

    @Test
    void shouldReturnFalseWhenEmailPreferencesDoNotExist() {

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertFalse(
                preferencesService.isEmailEnabled(1L)
        );
    }

    @Test
    void shouldReturnTrueWhenWhatsappIsEnabled() {

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.of(
                        new NotificationPreferences(
                                1L,
                                false,
                                true
                        )
                ));

        assertTrue(
                preferencesService.isWhatsappEnabled(1L)
        );
    }

    @Test
    void shouldReturnFalseWhenWhatsappPreferencesDoNotExist() {

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertFalse(
                preferencesService.isWhatsappEnabled(1L)
        );
    }

    @Test
    void shouldKeepPreferencesIndependentBetweenUsers() {

        NotificationPreferences firstUser =
                new NotificationPreferences(
                        1L,
                        true,
                        false
                );

        NotificationPreferences secondUser =
                new NotificationPreferences(
                        2L,
                        false,
                        true
                );

        when(preferencesRepository.findByUserId(1L))
                .thenReturn(Optional.of(firstUser));

        when(preferencesRepository.findByUserId(2L))
                .thenReturn(Optional.of(secondUser));

        NotificationPreferences firstResult =
                preferencesService.getPreferences(1L);

        NotificationPreferences secondResult =
                preferencesService.getPreferences(2L);

        assertEquals(1L, firstResult.getUserId());

        assertTrue(firstResult.isEmailEnabled());

        assertFalse(firstResult.isWhatsappEnabled());

        assertEquals(2L, secondResult.getUserId());

        assertFalse(secondResult.isEmailEnabled());

        assertTrue(secondResult.isWhatsappEnabled());
    }

    @Test
    void shouldRejectNullUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> preferencesService.getPreferences(null)
        );

        verifyNoInteractions(preferencesRepository);
    }

    @Test
    void shouldRejectZeroUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> preferencesService.getPreferences(0L)
        );

        verifyNoInteractions(preferencesRepository);
    }

    @Test
    void shouldRejectNegativeUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> preferencesService.getPreferences(-1L)
        );

        verifyNoInteractions(preferencesRepository);
    }

    @Test
    void shouldRejectUpdatingPreferencesWithInvalidUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> preferencesService.updatePreferences(
                        -1L,
                        true,
                        true
                )
        );

        verifyNoInteractions(preferencesRepository);
    }
}
