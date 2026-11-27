
package com.jouletracker.platform.alerts.application.internal.eventhandlers;

import com.jouletracker.platform.alerts.application.internal.events.CriticalAlertCreatedEvent;
import com.jouletracker.platform.alerts.application.internal.services.CriticalAlertNotificationService;
import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.devices.interfaces.acl.SensorThresholdSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * TS-19: Tests critical alert notification processing
 * and transaction lifecycle behavior.
 */
class CriticalAlertNotificationEventHandlerTest {

    private final CriticalAlertNotificationService notificationService =
            mock(CriticalAlertNotificationService.class);

    @Test
    void shouldNotifyOnlyAfterTransactionCommits() {

        CriticalAlertCreatedEvent event = createEvent();

        try (var context = createContext(true)) {

            TransactionTemplate transaction =
                    createTransactionTemplate(context);

            transaction.executeWithoutResult(status -> {

                context.publishEvent(event);

                // The transaction has not committed yet.
                verifyNoInteractions(notificationService);
            });

            // The transaction has successfully committed.
            verify(notificationService, times(1))
                    .notifyCriticalAlert(
                            event.alert(),
                            event.snapshot()
                    );

            verifyNoMoreInteractions(notificationService);
        }
    }

    @Test
    void shouldNotNotifyWhenTransactionRollsBack() {

        CriticalAlertCreatedEvent event = createEvent();

        try (var context = createContext(true)) {

            TransactionTemplate transaction =
                    createTransactionTemplate(context);

            transaction.executeWithoutResult(status -> {

                context.publishEvent(event);

                status.setRollbackOnly();
            });

            verifyNoInteractions(notificationService);
        }
    }

    @Test
    void shouldNotNotifyOutsideTransaction() {

        CriticalAlertCreatedEvent event = createEvent();

        try (var context = createContext(true)) {

            context.publishEvent(event);

            verifyNoInteractions(notificationService);
        }
    }

    @Test
    void shouldNotNotifyWhenNotificationsAreDisabled() {

        CriticalAlertCreatedEvent event = createEvent();

        try (var context = createContext(false)) {

            TransactionTemplate transaction =
                    createTransactionTemplate(context);

            transaction.executeWithoutResult(status ->
                    context.publishEvent(event)
            );

            verifyNoInteractions(notificationService);
        }
    }

    @Test
    void shouldHandleNotificationServiceFailure() {

        CriticalAlertCreatedEvent event = createEvent();

        doThrow(new RuntimeException("Brevo unavailable"))
                .when(notificationService)
                .notifyCriticalAlert(
                        event.alert(),
                        event.snapshot()
                );

        try (var context = createContext(true)) {

            TransactionTemplate transaction =
                    createTransactionTemplate(context);

            assertDoesNotThrow(() ->
                    transaction.executeWithoutResult(status ->
                            context.publishEvent(event)
                    )
            );

            verify(notificationService, times(1))
                    .notifyCriticalAlert(
                            event.alert(),
                            event.snapshot()
                    );
        }
    }

    @Test
    void shouldIgnoreAlertsWithoutPersistedIdentifier() {

        CriticalAlertCreatedEvent event = createEvent();

        event.alert().setId(null);

        try (var context = createContext(true)) {

            TransactionTemplate transaction =
                    createTransactionTemplate(context);

            transaction.executeWithoutResult(status ->
                    context.publishEvent(event)
            );

            verifyNoInteractions(notificationService);
        }
    }

    /**
     * Creates an isolated Spring application context with
     * transactional event handling and an H2 data source.
     *
     * The notification service is mocked to avoid
     * sending real emails through Brevo.
     */
    private AnnotationConfigApplicationContext createContext(
            boolean notificationsEnabled) {

        var context =
                new AnnotationConfigApplicationContext();

        context.registerBean(
                TransactionalEventListenerFactory.class
        );

        context.registerBean(
                DataSourceTransactionManager.class,
                () -> new DataSourceTransactionManager(
                        new DriverManagerDataSource(
                                "jdbc:h2:mem:notification_event_test",
                                "sa",
                                ""
                        )
                )
        );

        context.registerBean(
                CriticalAlertNotificationEventHandler.class,
                () -> new CriticalAlertNotificationEventHandler(
                        notificationService,
                        notificationsEnabled
                )
        );

        context.refresh();

        return context;
    }

    private TransactionTemplate createTransactionTemplate(
            AnnotationConfigApplicationContext context) {

        return new TransactionTemplate(
                context.getBean(
                        DataSourceTransactionManager.class
                )
        );
    }

    private CriticalAlertCreatedEvent createEvent() {

        Alert alert = new Alert(
                100L,
                1L,
                10L,
                5L,
                "Exceso de potencia detectado",
                "La potencia supera el límite configurado.",
                AlertSeverity.CRITICAL,
                "/dispositivos",
                false,
                AlertStatus.ACTIVE,
                LocalDateTime.now()
        );

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L,
                        1L,
                        "Sensor Cocina",
                        5L,
                        "Refrigeradora",
                        3.0,
                        8.0,
                        3.0,
                        1.5,
                        4L
                );

        return new CriticalAlertCreatedEvent(
                alert,
                snapshot
        );
    }
}
