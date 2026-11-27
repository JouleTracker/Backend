
package com.jouletracker.platform.businessenergy.interfaces.rest;

import com.jouletracker.platform.businessenergy.application.commandservices.BusinessAreaCommandService;
import com.jouletracker.platform.businessenergy.application.queryservices.BusinessAreaQueryService;
import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessAreaMetrics;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessDashboardMetrics;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaMetricsByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessDashboardMetricsQuery;
import com.jouletracker.platform.iam.infrastructure.security.authentication.AuthenticatedUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BusinessAreasControllerTest {

    @Mock
    private BusinessAreaCommandService commandService;

    @Mock
    private BusinessAreaQueryService queryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        BusinessAreasController controller =
                new BusinessAreasController(
                        commandService,
                        queryService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Simulates an authenticated user for controller tests.
     */
    private void authenticateUser(Long userId) {

        AuthenticatedUserDetails principal =
                mock(AuthenticatedUserDetails.class);

        when(principal.getId()).thenReturn(userId);

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @Test
    void getBusinessDashboardMetricsShouldReturn200WithAggregatedMetrics()
            throws Exception {

        authenticateUser(1L);

        BusinessAreaMetrics area = new BusinessAreaMetrics(
                1L, 1L,
                "Cocina", "Piso 1",
                "Cocina", "restaurant",
                2, 1, 1.5, 10.0, 7.0, true
        );

        BusinessDashboardMetrics dashboard =
                new BusinessDashboardMetrics(
                        1L,
                        "daily",
                        1,
                        1.5,
                        10.0,
                        7.0,
                        "Cocina",
                        List.of(area)
                );

        when(queryService.handle(
                any(GetBusinessDashboardMetricsQuery.class)))
                .thenReturn(dashboard);

        mockMvc.perform(
                        get("/api/v1/business-areas/metrics")
                                .param("userId", "1")
                                .param("period", "daily")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalAreas").value(1))
                .andExpect(jsonPath("$.totalPowerKw").value(1.5))
                .andExpect(jsonPath("$.totalTodayKwh").value(10.0))
                .andExpect(jsonPath("$.highestConsumptionAreaName")
                        .value("Cocina"))
                .andExpect(jsonPath("$.areas[0].name").value("Cocina"))
                .andExpect(jsonPath("$.areas[0].hasDevices").value(true));
    }

    @Test
    void getBusinessAreaMetricsByIdShouldReturn404WhenAreaNotFound()
            throws Exception {

        authenticateUser(1L);

        when(queryService.handle(
                any(GetBusinessAreaMetricsByIdQuery.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/v1/business-areas/999/metrics")
                )
                .andExpect(status().isNotFound());
    }

    /**
     * US-32: Rejects requests without authentication.
     */
    @Test
    void getBusinessAreasShouldReturn401WhenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/business-areas")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(queryService);
    }

    /**
     * US-32: Prevents impersonation through userId parameters.
     */
    @Test
    void getBusinessAreasShouldReturn403WhenRequestingAnotherUser()
            throws Exception {

        authenticateUser(1L);

        mockMvc.perform(
                        get("/api/v1/business-areas")
                                .param("userId", "2")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(queryService);
    }

    /**
     * US-32: Prevents access to business areas owned by other users.
     */
    @Test
    void getBusinessAreaByIdShouldReturn404WhenOwnedByAnotherUser()
            throws Exception {

        authenticateUser(1L);

        BusinessArea foreignArea = new BusinessArea(
                10L,
                2L,
                "Almacen",
                "Piso 1",
                "Foreign business area",
                "storefront"
        );

        foreignArea.addNotificationRecipient(
                "supervisor@example.com"
        );

        when(queryService.handle(
                any(GetBusinessAreaByIdQuery.class)))
                .thenReturn(Optional.of(foreignArea));

        mockMvc.perform(
                        get("/api/v1/business-areas/10")
                )
                .andExpect(status().isNotFound());
    }

    /**
     * US-32: Returns shared notification recipients
     * to the authenticated owner.
     */
    @Test
    void getBusinessAreaByIdShouldReturnNotificationRecipients()
            throws Exception {

        authenticateUser(1L);

        BusinessArea area = new BusinessArea(
                10L,
                1L,
                "Cocina",
                "Piso 1",
                "Business kitchen",
                "restaurant"
        );

        area.addNotificationRecipient("admin@example.com");
        area.addNotificationRecipient("supervisor@example.com");

        when(queryService.handle(
                any(GetBusinessAreaByIdQuery.class)))
                .thenReturn(Optional.of(area));

        mockMvc.perform(
                        get("/api/v1/business-areas/10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Cocina"))
                .andExpect(jsonPath("$.notificationRecipients.length()")
                        .value(2))
                .andExpect(jsonPath("$.notificationRecipients[0]")
                        .value("admin@example.com"))
                .andExpect(jsonPath("$.notificationRecipients[1]")
                        .value("supervisor@example.com"));
    }
}
