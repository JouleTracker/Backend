package com.jouletracker.platform.devices.application.internal.queryservices;

import com.jouletracker.platform.devices.application.queryservices.DeviceQueryService;
import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.domain.model.entities.DeviceSummary;
import com.jouletracker.platform.devices.domain.model.queries.GetAllDevicesByUserIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetDeviceByIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetDeviceSummaryByUserIdQuery;
import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;
import com.jouletracker.platform.devices.domain.repositories.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DeviceQueryServiceImpl implements DeviceQueryService {

    private final DeviceRepository deviceRepository;

    public DeviceQueryServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Optional<Device> handle(GetDeviceByIdQuery query) {
        return deviceRepository.findById(query.id());
    }

    @Override
    public List<Device> handle(GetAllDevicesByUserIdQuery query) {
        if (query.userId() != null) {
            return deviceRepository.findAllByUserId(query.userId());
        }
        return deviceRepository.findAll();
    }

    @Override
    public DeviceSummary handle(GetDeviceSummaryByUserIdQuery query) {
        List<Device> devices = query.userId() != null
                ? deviceRepository.findAllByUserId(query.userId())
                : deviceRepository.findAll();

        int connected = 0;
        int waiting = 0;
        double totalPower = 0.0;

        for (Device d : devices) {
            if (d.getStatus() == DeviceStatus.ONLINE) {
                connected++;
            } else if (d.getStatus() == DeviceStatus.STANDBY) {
                waiting++;
            }
            if (d.getCurrentPowerKw() != null) {
                totalPower += d.getCurrentPowerKw();
            }
        }

        double roundedPower = round(totalPower, 2);
        double estimatedCost = round(roundedPower * 0.70 * 24, 2);

        return new DeviceSummary(
                1L,
                connected,
                waiting,
                roundedPower,
                -0.3,
                "-12% vs ayer",
                15.4,
                "+8% vs mes ant.",
                18.5,
                estimatedCost
        );
    }

    private double round(double val, int places) {
        return BigDecimal.valueOf(val).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
