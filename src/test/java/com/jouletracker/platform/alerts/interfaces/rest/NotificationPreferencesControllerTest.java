
package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.internal.services.NotificationPreferencesService;
import com.jouletracker.platform.alerts.domain.model.aggregates.NotificationPreferences;
import com.jouletracker.platform.iam.infrastructure.security.authentication.AuthenticatedUserDetails;
import com.jouletracker.platform.iam.interfaces.acl.IamContextFacade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * US-38: REST controller tests for notification
 * preferences, validation and user isolation.
 */
@ExtendWith(MockitoExtension.class)
class NotificationPreferencesControllerTest {

    private static final String BASE_URL =
            "/api/v1/notification-preferences";

    @Mock
    private NotificationPreferencesService preferencesService;

    @Mock
    private IamContextFacade iamContextFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        mockMvc = MockMvcBuilders.standaloneSetup(
                new NotificationPreferencesController(
                        preferencesService,
                        iamContextFacade
                )
        ).build();
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnPreferencesForAuthenticatedUser()
            throws Exception {

        authenticateUser(1L);

        when(iamContextFacade.existsUserById(1L))
                .thenReturn(true);

        when(preferencesService.getPreferences(1L))
                .thenReturn(
                        new NotificationPreferences(
                                1L,
                                true,
                                false
                        )
                );

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.emailEnabled").value(true))
                .andExpect(jsonPath("$.whatsappEnabled").value(false));

        verify(preferencesService)
                .getPreferences(1L);
    }

    @Test
    void shouldReturnDefaultPreferencesForNewUser()
            throws Exception {

        authenticateUser(1L);

        when(iamContextFacade.existsUserById(1L))
                .thenReturn(true);

        when(preferencesService.getPreferences(1L))
                .thenReturn(new NotificationPreferences(1L));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.emailEnabled").value(false))
                .andExpect(jsonPath("$.whatsappEnabled").value(false));
    }

    @Test
    void shouldUpdateNotificationPreferences()
            throws Exception {

        authenticateUser(1L);

        when(iamContextFacade.existsUserById(1L))
                .thenReturn(true);

        when(preferencesService.updatePreferences(
                1L,
                true,
                true
        )).thenReturn(
                new NotificationPreferences(
                        1L,
                        true,
                        true
                )
        );

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "emailEnabled": true,
                                            "whatsappEnabled": true
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.emailEnabled").value(true))
                .andExpect(jsonPath("$.whatsappEnabled").value(true));

        verify(preferencesService)
                .updatePreferences(1L, true, true);
    }

    @Test
    void shouldRejectGetRequestWithoutAuthentication()
            throws Exception {

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(
                preferencesService,
                iamContextFacade
        );
    }

    @Test
    void shouldRejectUpdateRequestWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "emailEnabled": true,
                                            "whatsappEnabled": true
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(
                preferencesService,
                iamContextFacade
        );
    }

    @Test
    void shouldRejectRequestWithoutEmailPreference()
            throws Exception {

        authenticateUser(1L);

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "whatsappEnabled": true
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(preferencesService);
    }

    @Test
    void shouldRejectRequestWithoutWhatsappPreference()
            throws Exception {

        authenticateUser(1L);

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "emailEnabled": true
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(preferencesService);
    }

    @Test
    void shouldRejectAuthenticatedUserThatDoesNotExist()
            throws Exception {

        authenticateUser(99L);

        when(iamContextFacade.existsUserById(99L))
                .thenReturn(false);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(preferencesService);
    }

    @Test
    void shouldIgnoreExternalUserIdWhenReadingPreferences()
            throws Exception {

        authenticateUser(1L);

        when(iamContextFacade.existsUserById(1L))
                .thenReturn(true);

        when(preferencesService.getPreferences(1L))
                .thenReturn(
                        new NotificationPreferences(
                                1L,
                                true,
                                false
                        )
                );

        mockMvc.perform(
                        get(BASE_URL)
                                .param("userId", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(preferencesService)
                .getPreferences(1L);

        verify(preferencesService, never())
                .getPreferences(2L);
    }

    @Test
    void shouldPreventUpdatingAnotherUsersPreferences()
            throws Exception {

        authenticateUser(1L);

        when(iamContextFacade.existsUserById(1L))
                .thenReturn(true);

        when(preferencesService.updatePreferences(
                1L,
                true,
                false
        )).thenReturn(
                new NotificationPreferences(
                        1L,
                        true,
                        false
                )
        );

        mockMvc.perform(
                        put(BASE_URL)
                                .param("userId", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "emailEnabled": true,
                                            "whatsappEnabled": false
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(preferencesService)
                .updatePreferences(1L, true, false);

        verify(preferencesService, never())
                .updatePreferences(
                        eq(2L),
                        anyBoolean(),
                        anyBoolean()
                );
    }

    private void authenticateUser(Long userId) {

        AuthenticatedUserDetails principal =
                mock(AuthenticatedUserDetails.class);

        lenient().when(principal.getId())
                .thenReturn(userId);

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
