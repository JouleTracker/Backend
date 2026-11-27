
package com.jouletracker.platform.alerts.application.internal.services;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.businessenergy.interfaces.acl.BusinessEnergyContextFacade;
import com.jouletracker.platform.devices.interfaces.acl.SensorThresholdSnapshot;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.services.EmailService;
import com.jouletracker.platform.iam.interfaces.acl.IamContextFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TS-19: Delivers critical alert email notifications.
 *
 * US-32: Resolves business area notification recipients.
 *
 * US-38: Respects persisted user notification preferences.
 */
@Service
public class CriticalAlertNotificationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    CriticalAlertNotificationService.class
            );

    private final IamContextFacade iamContextFacade;

    private final BusinessEnergyContextFacade
            businessEnergyContextFacade;

    private final EmailService emailService;

    private final NotificationPreferencesService
            preferencesService;

    public CriticalAlertNotificationService(
            IamContextFacade iamContextFacade,
            BusinessEnergyContextFacade businessEnergyContextFacade,
            EmailService emailService,
            NotificationPreferencesService preferencesService) {

        this.iamContextFacade = iamContextFacade;

        this.businessEnergyContextFacade =
                businessEnergyContextFacade;

        this.emailService = emailService;

        this.preferencesService = preferencesService;
    }

    /**
     * Processes critical alert notifications after
     * the original alert transaction commits.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public void notifyCriticalAlert(
            Alert alert,
            SensorThresholdSnapshot snapshot) {

        if (alert == null || snapshot == null) {
            return;
        }

        if (alert.getId() == null
                || alert.getUserId() == null
                || alert.getSeverity() != AlertSeverity.CRITICAL) {

            return;
        }

        // US-32: Validate sensor ownership.
        if (!Objects.equals(
                alert.getUserId(),
                snapshot.userId())
                || !Objects.equals(
                alert.getSensorId(),
                snapshot.sensorId())) {

            LOGGER.warn(
                    "Notification rejected: invalid sensor ownership for alert {}",
                    alert.getId()
            );

            return;
        }

        Long userId = alert.getUserId();

        var ownerOpt =
                iamContextFacade.fetchNotificationRecipient(
                        userId
                );

        if (ownerOpt.isEmpty()) {

            LOGGER.warn(
                    "No eligible owner found for critical alert {}",
                    alert.getId()
            );

            return;
        }

        // US-38: Respect persisted email preferences.
        if (!preferencesService.isEmailEnabled(userId)) {

            LOGGER.debug(
                    "Email notifications disabled by user {} for alert {}",
                    userId,
                    alert.getId()
            );

            return;
        }

        Map<EmailAddress, String> recipients =
                new LinkedHashMap<>();

        var owner = ownerOpt.get();

        addRecipient(
                recipients,
                owner.email(),
                owner.fullName()
        );

        String businessAreaName = "Sin área comercial";

        // US-32: Resolve shared recipients by business area.
        if (snapshot.businessAreaId() != null) {

            var businessAreaOpt =
                    businessEnergyContextFacade
                            .fetchBusinessAreaNotificationSnapshot(
                                    snapshot.businessAreaId(),
                                    userId
                            );

            if (businessAreaOpt.isPresent()) {

                var businessArea = businessAreaOpt.get();

                businessAreaName =
                        businessArea.businessAreaName();

                for (String recipientEmail :
                        businessArea.notificationRecipients()) {

                    addRecipient(
                            recipients,
                            recipientEmail,
                            "Colaborador"
                    );
                }

            } else {

                businessAreaName = "Área no disponible";

                LOGGER.warn(
                        "Business area unavailable for critical alert {}",
                        alert.getId()
                );
            }
        }

        if (recipients.isEmpty()) {

            LOGGER.warn(
                    "No valid recipients found for critical alert {}",
                    alert.getId()
            );

            return;
        }

        String sensorName = snapshot.sensorName();

        if (sensorName == null || sensorName.isBlank()) {

            sensorName = "Sensor sin nombre";
        }

        int successfulDeliveries = 0;
        int failedDeliveries = 0;

        for (var recipient : recipients.entrySet()) {

            try {

                emailService.sendCriticalAlertEmail(
                        recipient.getKey(),
                        recipient.getValue(),
                        alert.getTitle(),
                        alert.getDescription(),
                        sensorName,
                        businessAreaName
                );

                successfulDeliveries++;

            } catch (RuntimeException exception) {

                failedDeliveries++;

                LOGGER.error(
                        "Critical alert {} email delivery failed: {}",
                        alert.getId(),
                        exception.getClass().getSimpleName()
                );
            }
        }

        LOGGER.info(
                "Critical alert {} delivery completed. Successful: {}, failed: {}",
                alert.getId(),
                successfulDeliveries,
                failedDeliveries
        );
    }

    /**
     * Validates and deduplicates email recipients.
     */
    private void addRecipient(
            Map<EmailAddress, String> recipients,
            String email,
            String recipientName) {

        if (email == null || email.isBlank()) {
            return;
        }

        try {

            EmailAddress address =
                    EmailAddress.of(email);

            String resolvedName =
                    recipientName == null
                            || recipientName.isBlank()
                            ? "Usuario"
                            : recipientName;

            recipients.putIfAbsent(
                    address,
                    resolvedName
            );

        } catch (IllegalArgumentException exception) {

            LOGGER.warn(
                    "Invalid notification recipient omitted"
            );
        }
    }
}
