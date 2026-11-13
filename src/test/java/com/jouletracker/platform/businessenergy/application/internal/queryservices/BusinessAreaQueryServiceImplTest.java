package com.jouletracker.platform.businessenergy.application.internal.queryservices;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessAreaMetrics;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessDashboardMetrics;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaMetricsByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessDashboardMetricsQuery;
import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import com.jouletracker.platform.devices.interfaces.acl.DeviceTelemetrySnapshot;
import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessAreaQueryServiceImplTest {

    @Mock
    private BusinessAreaRepository businessAreaRepository;

    @Mock
    private DevicesContextFacade devicesContextFacade;

    @Mock
    private EnergyMeasurementRepository energyMeasurementRepository;

    @InjectMocks
    private BusinessAreaQueryServiceImpl queryService;

    @Test
    void handleDashboardMetricsShouldAggregateAreaMetricsAndFlagEmptyAreas() {
        BusinessArea kitchen = new BusinessArea(1L, 1L, "Cocina", "Piso 1", "Cocina principal", "restaurant");
        BusinessArea warehouse = new BusinessArea(2L, 1L, "Almacén", "Piso 1", "Depósito", "warehouse");

        DeviceTelemetrySnapshot oven = new DeviceTelemetrySnapshot(
                10L, 1L, "Horno Industrial", "Cocina Principal", "Cocina", true, 2.5, 4.0, 12.0
        );
        DeviceTelemetrySnapshot fridge = new DeviceTelemetrySnapshot(
                11L, 1L, "Congeladora", "Cocina", "Refrigeración", false, 0.0, 2.0, 4.0
        );

        when(businessAreaRepository.findAllByUserId(1L)).thenReturn(List.of(kitchen, warehouse));
        when(devicesContextFacade.fetchDevicesByUserId(1L)).thenReturn(List.of(oven, fridge));

        BusinessDashboardMetrics metrics = queryService.handle(new GetBusinessDashboardMetricsQuery(1L, "daily"));

        assertEquals(2, metrics.totalAreas());
        assertEquals("daily", metrics.period());
        assertEquals(2.5, metrics.totalPowerKw());
        assertEquals(16.0, metrics.totalTodayKwh());
        assertEquals(11.2, metrics.totalTodayCostSoles());
        assertEquals("Cocina", metrics.highestConsumptionAreaName());

        BusinessAreaMetrics kitchenMetrics = metrics.areas().get(0);
        assertEquals(2, kitchenMetrics.deviceCount());
        assertEquals(1, kitchenMetrics.onlineDeviceCount());
        assertTrue(kitchenMetrics.hasDevices());

        BusinessAreaMetrics warehouseMetrics = metrics.areas().get(1);
        assertEquals(0, warehouseMetrics.deviceCount());
        assertEquals(0.0, warehouseMetrics.todayKwh());
        assertFalse(warehouseMetrics.hasDevices());
    }

    @Test
    void handleAreaMetricsByIdShouldUseMeasurementHistoryWhenPeriodIsWeekly() {
        BusinessArea office = new BusinessArea(3L, 1L, "Oficina", "Piso 2", "Área administrativa", "business_center");
        DeviceTelemetrySnapshot pc = new DeviceTelemetrySnapshot(
                20L, 1L, "Servidor", "Oficina", "Cómputo", true, 0.6, 1.5, 5.0
        );
        EnergyMeasurement m1 = new EnergyMeasurement(
                100L, 1L, 5L, 20L, "Servidor", "Cómputo", 0.6, 10.0, 220.0, 2.7, 7.0, LocalDateTime.now().minusDays(2)
        );

        when(businessAreaRepository.findById(3L)).thenReturn(Optional.of(office));
        when(devicesContextFacade.fetchDevicesByUserId(1L)).thenReturn(List.of(pc));
        when(energyMeasurementRepository.findAllByUserIdAndDeviceIdAndMeasuredAtBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(List.of(m1));

        Optional<BusinessAreaMetrics> result = queryService.handle(new GetBusinessAreaMetricsByIdQuery(3L, "semana"));

        assertTrue(result.isPresent());
        assertEquals(10.0, result.get().todayKwh());
        assertEquals(7.0, result.get().todayCostSoles());
        assertTrue(result.get().hasDevices());
    }
}
