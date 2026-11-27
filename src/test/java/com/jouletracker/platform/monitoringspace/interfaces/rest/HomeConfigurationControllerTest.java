
package com.jouletracker.platform.monitoringspace.interfaces.rest;

import com.jouletracker.platform.monitoringspace.application.internal.services.HomeConfigurationService;
import com.jouletracker.platform.monitoringspace.domain.model.aggregates.HomeConfiguration;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeConfigurationControllerTest {

    private static final String BASE_URL =
            "/api/v1/monitoring-spaces/home";

    private static final String VALID_REQUEST = """
            {
                "homeType": "Apartment",
                "occupants": 4,
                "location": "Chorrillos",
                "tariff": "BT5B"
            }
            """;

    @Mock
    private HomeConfigurationService homeService;

    @Mock
    private IamContextFacade iamContextFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        mockMvc = MockMvcBuilders.standaloneSetup(
                new HomeConfigurationController(
                        homeService,
                        iamContextFacade
                )
        ).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnExistingHomeConfiguration()
            throws Exception {

        authenticateUser(1L);

        when(homeService.getConfiguration(1L))
                .thenReturn(Optional.of(
                        createConfiguration()
                ));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.homeType").value("Apartment"))
                .andExpect(jsonPath("$.occupants").value(4))
                .andExpect(jsonPath("$.location").value("Chorrillos"))
                .andExpect(jsonPath("$.tariff").value("BT5B"))
                .andExpect(jsonPath("$.configured").value(true));
    }

    @Test
    void shouldReturnDefaultHomeConfiguration()
            throws Exception {

        authenticateUser(1L);

        when(homeService.getConfiguration(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.homeType").value("House"))
                .andExpect(jsonPath("$.occupants").value(1))
                .andExpect(jsonPath("$.location").value(""))
                .andExpect(jsonPath("$.tariff").value(""))
                .andExpect(jsonPath("$.configured").value(false));
    }

    @Test
    void shouldUpdateHomeConfiguration()
            throws Exception {

        authenticateUser(1L);

        mockConfigurationUpdate();

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.occupants").value(4))
                .andExpect(jsonPath("$.homeType").value("Apartment"));

        verify(homeService).updateConfiguration(
                1L, "Apartment", 4, "Chorrillos", "BT5B"
        );
    }

    @Test
    void shouldRejectUnauthenticatedGet()
            throws Exception {

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(homeService);
    }

    @Test
    void shouldRejectUnauthenticatedUpdate()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(homeService);
    }

    @Test
    void shouldRejectInvalidHomeType()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "homeType": "Invalid",
                                            "occupants": 3
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(homeService);
    }

    @Test
    void shouldRejectZeroOccupants()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "homeType": "House",
                                            "occupants": 0
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(homeService);
    }

    @Test
    void shouldRejectMissingRequiredFields()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(homeService);
    }

    @Test
    void shouldPreventAccessToAnotherUsersConfiguration()
            throws Exception {

        authenticateUser(1L);

        when(homeService.getConfiguration(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get(BASE_URL)
                                .param("userId", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(homeService).getConfiguration(1L);

        verify(homeService, never())
                .getConfiguration(2L);
    }

    @Test
    void shouldPreventUpdatingAnotherUsersConfiguration()
            throws Exception {

        authenticateUser(1L);

        mockConfigurationUpdate();

        mockMvc.perform(
                        put(BASE_URL)
                                .param("userId", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(homeService).updateConfiguration(
                1L, "Apartment", 4, "Chorrillos", "BT5B"
        );

        verifyNoMoreInteractions(homeService);
    }

    @Test
    void shouldRejectBusinessUserGet()
            throws Exception {

        authenticateUser(1L, Role.ROLE_BUSINESS);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());

        verifyNoInteractions(homeService);
    }

    @Test
    void shouldRejectBusinessUserUpdate()
            throws Exception {

        authenticateUser(1L, Role.ROLE_BUSINESS);

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(homeService);
    }

    @Test
    void shouldRejectAdministrator()
            throws Exception {

        authenticateUser(1L, Role.ROLE_ADMIN);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());

        verifyNoInteractions(homeService);
    }

    @Test
    void shouldRejectUserWithConflictingRoles()
            throws Exception {

        authenticateUser(
                1L,
                Role.ROLE_USER,
                Role.ROLE_BUSINESS
        );

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());

        verifyNoInteractions(homeService);
    }

    private HomeConfiguration createConfiguration() {

        return new HomeConfiguration(
                1L,
                "Apartment",
                4,
                "Chorrillos",
                "BT5B"
        );
    }

    private void mockConfigurationUpdate() {

        when(homeService.updateConfiguration(
                1L,
                "Apartment",
                4,
                "Chorrillos",
                "BT5B"
        )).thenReturn(createConfiguration());
    }

    private void authenticateUser(Long userId) {

        authenticateUser(userId, Role.ROLE_USER);
    }

    private void authenticateUser(
            Long userId,
            Role... roles) {

        AuthenticatedUserDetails principal =
                mock(AuthenticatedUserDetails.class);

        when(principal.getId()).thenReturn(userId);

        when(iamContextFacade.existsUserById(userId))
                .thenReturn(true);

        var authorities = Arrays.stream(roles)
                .map(role ->
                        new SimpleGrantedAuthority(role.name()))
                .toList();

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
