
package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.internal.services.MonthlyConsumptionThresholdService;
import com.jouletracker.platform.alerts.application.internal.services.MonthlyConsumptionThresholdService.MonthlyConsumptionStatus;
import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyConsumptionThreshold;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MonthlyConsumptionThresholdControllerTest {

    private static final String BASE_URL =
            "/api/v1/monthly-consumption-threshold";

    @Mock
    private MonthlyConsumptionThresholdService thresholdService;

    @Mock
    private IamContextFacade iamContextFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        mockMvc = MockMvcBuilders.standaloneSetup(
                new MonthlyConsumptionThresholdController(
                        thresholdService,
                        iamContextFacade
                )
        ).build();
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnConfiguredThreshold()
            throws Exception {

        authenticateUser(1L);

        when(thresholdService.getThreshold(1L))
                .thenReturn(Optional.of(
                        new MonthlyConsumptionThreshold(
                                1L,
                                new BigDecimal("150")
                        )
                ));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.monthlyLimitKwh").value(150))
                .andExpect(jsonPath("$.configured").value(true));
    }

    @Test
    void shouldReturnUnconfiguredThreshold()
            throws Exception {

        authenticateUser(1L);

        when(thresholdService.getThreshold(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.configured").value(false))
                .andExpect(jsonPath("$.monthlyLimitKwh").doesNotExist());
    }

    @Test
    void shouldUpdateMonthlyThreshold()
            throws Exception {

        authenticateUser(1L);

        when(thresholdService.updateThreshold(
                1L,
                new BigDecimal("200")
        )).thenReturn(
                new MonthlyConsumptionThreshold(
                        1L,
                        new BigDecimal("200")
                )
        );

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "monthlyLimitKwh": 200
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.monthlyLimitKwh").value(200))
                .andExpect(jsonPath("$.configured").value(true));

        verify(thresholdService)
                .updateThreshold(
                        1L,
                        new BigDecimal("200")
                );
    }

    @Test
    void shouldReturnMonthlyConsumptionStatus()
            throws Exception {

        authenticateUser(1L);

        when(thresholdService.getConsumptionStatus(1L))
                .thenReturn(
                        new MonthlyConsumptionStatus(
                                1L,
                                "2026-11",
                                true,
                                new BigDecimal("150"),
                                new BigDecimal("165"),
                                BigDecimal.ZERO,
                                new BigDecimal("110.00"),
                                true
                        )
                );

        mockMvc.perform(get(BASE_URL + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.period").value("2026-11"))
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.monthlyLimitKwh").value(150))
                .andExpect(jsonPath("$.currentConsumptionKwh").value(165))
                .andExpect(jsonPath("$.remainingKwh").value(0))
                .andExpect(jsonPath("$.consumptionPercentage").value(110))
                .andExpect(jsonPath("$.thresholdReached").value(true));
    }

    @Test
    void shouldRejectNegativeThreshold()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "monthlyLimitKwh": -100
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(thresholdService);
    }

    @Test
    void shouldRejectZeroThreshold()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "monthlyLimitKwh": 0
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(thresholdService);
    }

    @Test
    void shouldRejectMissingThreshold()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(thresholdService);
    }

    @Test
    void shouldRejectExcessiveDecimalPrecision()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "monthlyLimitKwh": 150.1234
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(thresholdService);
    }

    @Test
    void shouldRejectUnauthenticatedGet()
            throws Exception {

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(thresholdService);
    }

    @Test
    void shouldRejectUnauthenticatedUpdate()
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "monthlyLimitKwh": 150
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(thresholdService);
    }

    @Test
    void shouldRejectUnauthenticatedStatus()
            throws Exception {

        mockMvc.perform(get(BASE_URL + "/status"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(thresholdService);
    }

    @Test
    void shouldPreventAccessToAnotherUsersThreshold()
            throws Exception {

        authenticateUser(1L);

        when(thresholdService.getThreshold(1L))
                .thenReturn(Optional.of(
                        new MonthlyConsumptionThreshold(
                                1L,
                                new BigDecimal("150")
                        )
                ));

        mockMvc.perform(
                        get(BASE_URL)
                                .param("userId", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(thresholdService).getThreshold(1L);

        verify(thresholdService, never())
                .getThreshold(2L);
    }

    private void authenticateUser(Long userId) {

        AuthenticatedUserDetails principal =
                mock(AuthenticatedUserDetails.class);

        when(principal.getId())
                .thenReturn(userId);

        when(iamContextFacade.existsUserById(userId))
                .thenReturn(true);

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
