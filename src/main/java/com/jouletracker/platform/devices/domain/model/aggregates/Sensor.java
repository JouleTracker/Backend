package com.jouletracker.platform.devices.domain.model.aggregates;

import com.jouletracker.platform.devices.domain.model.commands.RegisterSensorCommand;
import com.jouletracker.platform.devices.domain.model.events.SensorRegisteredEvent;
import com.jouletracker.platform.devices.domain.model.valueobjects.SensorNetworkStatus;
import com.jouletracker.platform.devices.domain.model.valueobjects.SerialNumber;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Aggregate Root representing an IoT smart metering sensor / smart plug.
 */
@Getter
@Setter
public class Sensor extends AbstractDomainAggregateRoot<Sensor> {

    private Long id;
    private Long userId;
    private String name;
    private String model;
    private SerialNumber serialNumber;
    private SensorNetworkStatus status;
    private Double currentPowerKw;
    private Double todayKwh;
    private Long assignedDeviceId;
    private String assignedDeviceName;
    private String assignedDeviceCategory;
    private Long applianceProfileId;
    private String applianceProfileName;
    private Double recommendedDailyKwh;
    private String lastSync;

    public Sensor() {
        this.status = SensorNetworkStatus.OFFLINE;
        this.currentPowerKw = 0.0;
        this.todayKwh = 0.0;
        this.lastSync = "Sin lectura";
    }

    public Sensor(Long id, Long userId, String name, String model, SerialNumber serialNumber,
                  SensorNetworkStatus status, Double currentPowerKw, Double todayKwh,
                  Long assignedDeviceId, String assignedDeviceName, String assignedDeviceCategory,
                  Long applianceProfileId, String applianceProfileName, Double recommendedDailyKwh,
                  String lastSync) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.model = model != null ? model : "JoulePlug-v1";
        this.serialNumber = Objects.requireNonNull(serialNumber, "serialNumber must not be null");
        this.status = status != null ? status : SensorNetworkStatus.OFFLINE;
        this.currentPowerKw = currentPowerKw != null ? currentPowerKw : 0.0;
        this.todayKwh = todayKwh != null ? todayKwh : 0.0;
        this.assignedDeviceId = assignedDeviceId;
        this.assignedDeviceName = assignedDeviceName;
        this.assignedDeviceCategory = assignedDeviceCategory;
        this.applianceProfileId = applianceProfileId;
        this.applianceProfileName = applianceProfileName;
        this.recommendedDailyKwh = recommendedDailyKwh;
        this.lastSync = lastSync != null ? lastSync : "Sin lectura";
    }

    public Sensor(RegisterSensorCommand command) {
        this(
                null,
                command.userId(),
                command.name(),
                command.model(),
                new SerialNumber(command.serialNumber()),
                SensorNetworkStatus.ONLINE,
                0.0,
                0.0,
                command.assignedDeviceId(),
                null,
                null,
                command.applianceProfileId(),
                null,
                null,
                "Recién registrado"
        );
    }

    /**
     * Updates configuration details (TS-06, US-12).
     */
    public void updateConfiguration(String name, String model, SensorNetworkStatus status) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (model != null && !model.isBlank()) {
            this.model = model.trim();
        }
        if (status != null) {
            this.status = status;
            if (status == SensorNetworkStatus.OFFLINE) {
                this.currentPowerKw = 0.0;
            }
        }
    }

    /**
     * Assigns this sensor to a domestic appliance (US-41, US-43).
     */
    public void assignDevice(Long deviceId, String deviceName, String deviceCategory) {
        this.assignedDeviceId = deviceId;
        this.assignedDeviceName = deviceName;
        this.assignedDeviceCategory = deviceCategory;
    }

    /**
     * Unlinks the assigned device (US-15, US-44).
     */
    public void unassignDevice() {
        this.assignedDeviceId = null;
        this.assignedDeviceName = null;
        this.assignedDeviceCategory = null;
    }

    /**
     * Associates sensor to a reference appliance consumption profile (US-43).
     */
    public void assignApplianceProfile(Long profileId, String profileName, Double recommendedDailyKwh) {
        this.applianceProfileId = profileId;
        this.applianceProfileName = profileName;
        this.recommendedDailyKwh = recommendedDailyKwh;
    }

    /**
     * Unassigns the reference appliance profile.
     */
    public void unassignApplianceProfile() {
        this.applianceProfileId = null;
        this.applianceProfileName = null;
        this.recommendedDailyKwh = null;
    }

    /**
     * Records new power telemetry measurements.
     */
    public void recordTelemetry(Double powerKw, Double addedKwh, String timestamp) {
        this.currentPowerKw = powerKw != null ? powerKw : 0.0;
        if (addedKwh != null && addedKwh > 0) {
            this.todayKwh = (this.todayKwh != null ? this.todayKwh : 0.0) + addedKwh;
        }
        this.lastSync = timestamp != null ? timestamp : "Ahora";
        this.status = SensorNetworkStatus.ONLINE;
    }

    /**
     * Signals sensor registration event.
     */
    public void onRegistered() {
        registerDomainEvent(new SensorRegisteredEvent(this.id, this.userId, this.serialNumber.value()));
    }
}
