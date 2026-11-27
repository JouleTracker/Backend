
package com.jouletracker.platform.alerts.application.internal.commandservices;

import com.jouletracker.platform.alerts.application.commandservices.AlertCommandService;
import com.jouletracker.platform.alerts.application.internal.events.CriticalAlertCreatedEvent;
import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.commands.EvaluateConsumptionAlertCommand;
import com.jouletracker.platform.alerts.domain.model.commands.MarkAlertAsReadCommand;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.devices.interfaces.acl.SensorThresholdSnapshot;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Evaluates telemetry consumption and power thresholds
 * to generate automated alerts (TS-14).
 *
 * US-32: Validates sensor ownership.
 * TS-19: Publishes events for newly persisted critical alerts.
 */
@Service
@Transactional
public class AlertCommandServiceImpl implements AlertCommandService {

    private static final double PREVENTIVE_THRESHOLD_RATIO = 0.80;
    private static final double CRITICAL_EXCESS_RATIO = 1.20;

    private final AlertRepository alertRepository;

    private final DevicesContextFacade devicesContextFacade;

    private final ApplicationEventPublisher eventPublisher;

    public AlertCommandServiceImpl(
            AlertRepository alertRepository,
            DevicesContextFacade devicesContextFacade,
            ApplicationEventPublisher eventPublisher) {

        this.alertRepository = alertRepository;
        this.devicesContextFacade = devicesContextFacade;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<Alert> handle(
            EvaluateConsumptionAlertCommand command) {

        if (command == null
                || command.userId() == null
                || command.sensorId() == null) {

            return List.of();
        }

        // US-32: Validate sensor ownership before evaluating alerts.
        return devicesContextFacade
                .fetchSensorThresholdById(command.sensorId())
                .filter(snapshot ->
                        Objects.equals(
                                snapshot.userId(),
                                command.userId()
                        ))
                .map(snapshot -> evaluateSnapshot(
                        snapshot,
                        command.powerKw(),
                        command.deviceName()
                ))
                .orElse(List.of());
    }

    @Override
    public List<Alert> evaluateUserSensors(Long userId) {

        if (userId == null) {
            return List.of();
        }

        List<Alert> generated = new ArrayList<>();

        for (SensorThresholdSnapshot snapshot :
                devicesContextFacade
                        .fetchSensorThresholdsByUserId(userId)) {

            if (!Objects.equals(
                    snapshot.userId(),
                    userId)) {

                continue;
            }

            generated.addAll(evaluateSnapshot(
                    snapshot,
                    snapshot.currentPowerKw(),
                    snapshot.assignedDeviceName()
            ));
        }

        return generated;
    }

    @Override
    public Result<Alert, ApplicationError> handle(
            MarkAlertAsReadCommand command) {

        if (command == null || command.alertId() == null) {

            return Result.failure(
                    ApplicationError.validationError(
                            "alertId",
                            "Alert ID is required"
                    )
            );
        }

        return alertRepository.findById(command.alertId())
                .map(alert -> {

                    alert.markAsRead();

                    return Result.<Alert, ApplicationError>success(
                            alertRepository.save(alert)
                    );
                })
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound(
                                "Alert",
                                command.alertId().toString()
                        )
                ));
    }

    private List<Alert> evaluateSnapshot(
            SensorThresholdSnapshot snapshot,
            Double measuredPowerKw,
            String fallbackDeviceName) {

        List<Alert> created = new ArrayList<>();

        String sensorDisplayName =
                (snapshot.sensorName() != null
                        && !snapshot.sensorName().isBlank())
                        ? snapshot.sensorName()
                        : "Sensor #" + snapshot.sensorId();

        String deviceDisplayName = resolveDeviceName(
                snapshot,
                fallbackDeviceName,
                sensorDisplayName
        );

        double todayKwh = snapshot.todayKwh() != null
                ? snapshot.todayKwh()
                : 0.0;

        Double dailyLimit = snapshot.recommendedDailyKwh();

        // Evaluate daily consumption thresholds.
        if (dailyLimit != null && dailyLimit > 0) {

            if (todayKwh > dailyLimit) {

                AlertSeverity severity =
                        todayKwh >= dailyLimit * CRITICAL_EXCESS_RATIO
                                ? AlertSeverity.CRITICAL
                                : AlertSeverity.WARNING;

                // TS-19: Distinguish warnings from critical alerts
                // so warnings cannot suppress critical notifications.
                String title =
                        severity == AlertSeverity.CRITICAL
                                ? "Límite diario superado (crítico): "
                                + sensorDisplayName
                                : "Límite diario superado: "
                                + sensorDisplayName;

                String description = String.format(
                        Locale.US,
                        "El dispositivo %s consumió %.2f kWh hoy "
                                + "(límite sugerido: %.2f kWh/día).",
                        deviceDisplayName,
                        todayKwh,
                        dailyLimit
                );

                saveIfNotDuplicate(
                        snapshot,
                        title,
                        description,
                        severity
                ).ifPresent(created::add);

            } else if (
                    todayKwh >= dailyLimit
                            * PREVENTIVE_THRESHOLD_RATIO) {

                String title =
                        "Advertencia preventiva de consumo: "
                                + sensorDisplayName;

                String description = String.format(
                        Locale.US,
                        "El dispositivo %s alcanzó %.2f kWh "
                                + "de %.2f kWh diarios recomendados.",
                        deviceDisplayName,
                        todayKwh,
                        dailyLimit
                );

                saveIfNotDuplicate(
                        snapshot,
                        title,
                        description,
                        AlertSeverity.INFO
                ).ifPresent(created::add);
            }
        }

        // Evaluate instantaneous power thresholds.
        double effectivePowerKw = measuredPowerKw != null
                ? measuredPowerKw
                : (snapshot.currentPowerKw() != null
                ? snapshot.currentPowerKw()
                : 0.0);

        Double powerLimitKw = snapshot.devicePowerLimitKw();

        if (powerLimitKw != null
                && powerLimitKw > 0
                && effectivePowerKw > powerLimitKw) {

            String title =
                    "Exceso de potencia detectado: "
                            + deviceDisplayName;

            String description = String.format(
                    Locale.US,
                    "Potencia instantánea de %.2f kW supera "
                            + "el límite configurado de %.2f kW.",
                    effectivePowerKw,
                    powerLimitKw
            );

            saveIfNotDuplicate(
                    snapshot,
                    title,
                    description,
                    AlertSeverity.CRITICAL
            ).ifPresent(created::add);
        }

        return created;
    }

    /**
     * Persists an alert only when an equivalent active alert
     * does not already exist for the same user and sensor.
     *
     * TS-19: Publishes a notification event only for newly
     * persisted critical alerts.
     */
    private Optional<Alert> saveIfNotDuplicate(
            SensorThresholdSnapshot snapshot,
            String title,
            String description,
            AlertSeverity severity) {

        if (alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                snapshot.userId(),
                snapshot.sensorId(),
                title
        )) {

            return Optional.empty();
        }

        Alert alert = new Alert(
                null,
                snapshot.userId(),
                snapshot.sensorId(),
                snapshot.assignedDeviceId(),
                title,
                description,
                severity,
                "/dispositivos",
                false,
                AlertStatus.ACTIVE,
                LocalDateTime.now()
        );

        Alert savedAlert = alertRepository.save(alert);

        // TS-19: Publish only newly persisted critical alerts.
        if (savedAlert.getId() != null
                && savedAlert.getSeverity()
                == AlertSeverity.CRITICAL) {

            eventPublisher.publishEvent(
                    new CriticalAlertCreatedEvent(
                            savedAlert,
                            snapshot
                    )
            );
        }

        return Optional.of(savedAlert);
    }

    private String resolveDeviceName(
            SensorThresholdSnapshot snapshot,
            String fallbackDeviceName,
            String sensorDisplayName) {

        if (snapshot.assignedDeviceName() != null
                && !snapshot.assignedDeviceName().isBlank()) {

            return snapshot.assignedDeviceName();
        }

        if (fallbackDeviceName != null
                && !fallbackDeviceName.isBlank()) {

            return fallbackDeviceName;
        }

        return sensorDisplayName;
    }
}
