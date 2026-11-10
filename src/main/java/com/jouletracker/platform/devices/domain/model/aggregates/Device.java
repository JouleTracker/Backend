package com.jouletracker.platform.devices.domain.model.aggregates;

import com.jouletracker.platform.devices.domain.model.commands.CreateDeviceCommand;
import com.jouletracker.platform.devices.domain.model.events.DeviceDeletedEvent;
import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Aggregate Root representing an electrical appliance monitored in JouleTracker.
 */
@Getter
@Setter
public class Device extends AbstractDomainAggregateRoot<Device> {

    private Long id;
    private Long userId;
    private String name;
    private String location;
    private String category;
    private DeviceStatus status;
    private Double currentPowerKw;
    private Double powerLimitKw;
    private String maintenanceDueDate;
    private Double todayKwh;
    private String lastActivity;

    public Device() {
        this.status = DeviceStatus.OFF;
        this.currentPowerKw = 0.0;
        this.todayKwh = 0.0;
        this.lastActivity = "Sin actividad reciente";
    }

    public Device(Long id, Long userId, String name, String location, String category,
                  DeviceStatus status, Double currentPowerKw, Double powerLimitKw,
                  String maintenanceDueDate, Double todayKwh, String lastActivity) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.location = location != null ? location : "General";
        this.category = category != null ? category : "Electrodomésticos";
        this.status = status != null ? status : DeviceStatus.OFF;
        this.currentPowerKw = currentPowerKw != null ? currentPowerKw : 0.0;
        this.powerLimitKw = powerLimitKw;
        this.maintenanceDueDate = maintenanceDueDate;
        this.todayKwh = todayKwh != null ? todayKwh : 0.0;
        this.lastActivity = lastActivity != null ? lastActivity : "Sin actividad reciente";
    }

    public Device(CreateDeviceCommand command) {
        this(
                null,
                command.userId(),
                command.name(),
                command.location(),
                command.category(),
                command.status() != null ? command.status() : DeviceStatus.OFF,
                0.0,
                command.powerLimitKw(),
                command.maintenanceDueDate(),
                0.0,
                "Registrado recientemente"
        );
    }

    /**
     * Updates editable details of the device.
     */
    public void updateDetails(String name, String location, String category,
                              DeviceStatus status, Double powerLimitKw, String maintenanceDueDate) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (location != null && !location.isBlank()) {
            this.location = location.trim();
        }
        if (category != null && !category.isBlank()) {
            this.category = category.trim();
        }
        if (status != null) {
            this.status = status;
        }
        this.powerLimitKw = powerLimitKw;
        this.maintenanceDueDate = maintenanceDueDate;
    }

    /**
     * Updates power telemetry and enforces standby/vampire power classification (US-42).
     */
    public void updateTelemetry(Double powerKw, Double todayKwh, String lastActivity) {
        this.currentPowerKw = powerKw != null ? powerKw : 0.0;
        this.todayKwh = todayKwh != null ? todayKwh : 0.0;
        if (lastActivity != null && !lastActivity.isBlank()) {
            this.lastActivity = lastActivity;
        }

        // US-42: Standby Power Detection (< 0.05 kW and > 0)
        if (this.currentPowerKw > 0.0 && this.currentPowerKw < 0.05) {
            this.status = DeviceStatus.STANDBY;
        } else if (this.currentPowerKw >= 0.05) {
            this.status = DeviceStatus.ONLINE;
        } else {
            this.status = DeviceStatus.OFF;
        }
    }

    /**
     * Triggers domain event on deletion for sensor unlinking (US-44).
     */
    public void onDeleted() {
        registerDomainEvent(new DeviceDeletedEvent(this.id, this.userId));
    }
}
