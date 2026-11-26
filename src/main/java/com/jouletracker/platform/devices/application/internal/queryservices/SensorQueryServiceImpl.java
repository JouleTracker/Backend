package com.jouletracker.platform.devices.application.internal.queryservices;

import com.jouletracker.platform.devices.application.queryservices.SensorQueryService;
import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.entities.SensorSummary;
import com.jouletracker.platform.devices.domain.model.queries.GetAllSensorsByUserIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetSensorByIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetSensorSummaryByUserIdQuery;
import com.jouletracker.platform.devices.domain.model.valueobjects.SensorNetworkStatus;
import com.jouletracker.platform.devices.domain.repositories.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SensorQueryServiceImpl implements SensorQueryService {

    private final SensorRepository sensorRepository;

    public SensorQueryServiceImpl(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @Override
    public Optional<Sensor> handle(GetSensorByIdQuery query) {
        return sensorRepository.findById(query.id());
    }

    @Override
    public List<Sensor> handle(GetAllSensorsByUserIdQuery query) {
        if (query.userId() != null) {
            return sensorRepository.findAllByUserId(query.userId());
        }
        return sensorRepository.findAll();
    }

    @Override
    public SensorSummary handle(GetSensorSummaryByUserIdQuery query) {
        List<Sensor> sensors = query.userId() != null
                ? sensorRepository.findAllByUserId(query.userId())
                : sensorRepository.findAll();

        int total = sensors.size();
        int active = 0;
        int monitored = 0;
        double totalPower = 0.0;
        double totalEnergyToday = 0.0;

        for (Sensor s : sensors) {
            if (s.getStatus() == SensorNetworkStatus.ONLINE) {
                active++;
            }
            if (s.getAssignedDeviceId() != null) {
                monitored++;
            }
            if (s.getCurrentPowerKw() != null) {
                totalPower += s.getCurrentPowerKw();
            }
            if (s.getTodayKwh() != null) {
                totalEnergyToday += s.getTodayKwh();
            }
        }

        return new SensorSummary(
                1L,
                total,
                active,
                monitored,
                total - monitored,
                round(totalPower, 2),
                round(totalEnergyToday, 2)
        );
    }

    private double round(double val, int places) {
        return BigDecimal.valueOf(val).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
