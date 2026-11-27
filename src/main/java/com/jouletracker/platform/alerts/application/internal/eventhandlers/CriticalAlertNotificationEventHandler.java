
package com.jouletracker.platform.alerts.application.internal.eventhandlers;

import com.jouletracker.platform.alerts.application.internal.events.CriticalAlertCreatedEvent;
import com.jouletracker.platform.alerts.application.internal.services.CriticalAlertNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * TS-19: Handles critical alert notification events
 * after the alert transaction has successfully committed.
 */
@Component
public class CriticalAlertNotificationEventHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    CriticalAlertNotificationEventHandler.class
            );

    private final CriticalAlertNotificationService
            notificationService;

    private final boolean notificationsEnabled;

    public CriticalAlertNotificationEventHandler(
            CriticalAlertNotificationService notificationService,
            @Value("${application.notifications.critical-email-enabled:false}")
            boolean notificationsEnabled) {

        this.notificationService = notificationService;
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Dispatches notifications only after a successful
     * transaction commit.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = false
    )
    public void on(CriticalAlertCreatedEvent event) {

        if (event == null
                || event.alert() == null
                || event.snapshot() == null) {

            return;
        }

        Long alertId = event.alert().getId();

        if (alertId == null) {
            return;
        }

        if (!notificationsEnabled) {

            LOGGER.debug(
                    "Critical email notifications disabled for alert {}",
                    alertId
            );

            return;
        }

        try {

            notificationService.notifyCriticalAlert(
                    event.alert(),
                    event.snapshot()
            );

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Critical alert {} notification processing failed: {}",
                    alertId,
                    exception.getClass().getSimpleName()
            );
        }
    }
}
