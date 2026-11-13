package com.jouletracker.platform.energy.application.internal.queryservices;

import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.energy.application.queryservices.EnergyReadingQueryService;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.EnergyReading;
import com.jouletracker.platform.energy.domain.model.queries.GetEnergyReadingsQuery;
import com.jouletracker.platform.energy.domain.model.queries.GetLatestReadingBySensorIdQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.jouletracker.platform.energy.application.internal.queryservices.ConsumptionSummaryQueryServiceImpl.resolvePeriodStart;
import static com.jouletracker.platform.energy.application.internal.queryservices.ConsumptionSummaryQueryServiceImpl.round2;

@Service
public class EnergyReadingQueryServiceImpl implements EnergyReadingQueryService {

    private final EnergyMeasurementRepository measurementRepository;
    private final DevicesContextFacade devicesContextFacade;

    public EnergyReadingQueryServiceImpl(EnergyMeasurementRepository measurementRepository,
                                         DevicesContextFacade devicesContextFacade) {
        this.measurementRepository = measurementRepository;
        this.devicesContextFacade = devicesContextFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnergyReading> handle(GetEnergyReadingsQuery query) {
        ConsumptionPeriod period = query.period() != null ? query.period() : ConsumptionPeriod.DAILY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = resolvePeriodStart(now, period);

        List<EnergyMeasurement> measurements = query.deviceId() != null
                ? measurementRepository.findAllByUserIdAndDeviceIdAndMeasuredAtBetween(query.userId(), query.deviceId(), start, now)
                : measurementRepository.findAllByUserIdAndMeasuredAtBetween(query.userId(), start, now);

        DateTimeFormatter formatter = period == ConsumptionPeriod.DAILY
                ? DateTimeFormatter.ofPattern("HH:mm")
                : DateTimeFormatter.ofPattern("dd/MM");

        return measurements.stream()
                .map(m -> new EnergyReading(
                        m.getId(),
                        m.getMeasuredAt().format(formatter),
                        m.getMeasuredAt(),
                        round2(m.getEnergyKwh()),
                        round2(m.getPowerKw()),
                        round2(m.getCost()),
                        m.getDeviceId(),
                        m.getDeviceName()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Result<EnergyMeasurement, ApplicationError> handle(GetLatestReadingBySensorIdQuery query) {
        if (query.sensorId() == null) {
            return Result.failure(ApplicationError.validationError("sensorId", "Sensor ID is required"));
        }
        if (!devicesContextFacade.existsSensorById(query.sensorId())) {
            return Result.failure(ApplicationError.notFound("Sensor", query.sensorId().toString()));
        }
        return measurementRepository.findTopBySensorIdOrderByMeasuredAtDesc(query.sensorId())
                .<Result<EnergyMeasurement, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("EnergyMeasurement", "sensor:" + query.sensorId())
                ));
    }
}
