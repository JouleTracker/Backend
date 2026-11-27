
package com.jouletracker.platform.alerts.application.internal.services;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.businessenergy.interfaces.acl.BusinessEnergyContextFacade;
import com.jouletracker.platform.businessenergy.interfaces.acl.BusinessEnergyContextFacade.BusinessAreaNotificationSnapshot;
import com.jouletracker.platform.devices.interfaces.acl.SensorThresholdSnapshot;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.services.EmailService;
import com.jouletracker.platform.iam.interfaces.acl.IamContextFacade;
import com.jouletracker.platform.iam.interfaces.acl.IamContextFacade.NotificationRecipient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TS-19: Unit tests for critical alert notifications.
 *
 * US-32: Validates business area notification recipients.
 *
 * US-38: Validates persisted notification preferences.
 */
@ExtendWith(MockitoExtension.class)
class CriticalAlertNotificationServiceTest {

    @Mock
    private IamContextFacade iamContextFacade;

    @Mock
    private BusinessEnergyContextFacade businessEnergyContextFacade;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationPreferencesService preferencesService;

    @InjectMocks
    private CriticalAlertNotificationService notificationService;

    @Test
    void shouldNotifyOwnerWhenSensorHasNoBusinessArea() {

        Alert alert = createAlert(AlertSeverity.CRITICAL);

        SensorThresholdSnapshot snapshot =
                createSnapshot(1L, null);

        mockOwner();
        enableEmailNotifications();

        notificationService.notifyCriticalAlert(
                alert,
                snapshot
        );

        verify(emailService, times(1))
                .sendCriticalAlertEmail(
                        EmailAddress.of("owner@example.com"),
                        "Administrador",
                        alert.getTitle(),
                        alert.getDescription(),
                        "Sensor Cocina",
                        "Sin área comercial"
                );

        verify(preferencesService).isEmailEnabled(1L);

        verifyNoInteractions(businessEnergyContextFacade);
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void shouldNotifyOwnerAndBusinessAreaRecipients() {

        Alert alert = createAlert(AlertSeverity.CRITICAL);

        SensorThresholdSnapshot snapshot =
                createSnapshot(1L, 4L);

        mockOwner();
        enableEmailNotifications();

        when(businessEnergyContextFacade
                .fetchBusinessAreaNotificationSnapshot(4L, 1L))
                .thenReturn(Optional.of(
                        new BusinessAreaNotificationSnapshot(
                                4L,
                                1L,
                                "Cocina Principal",
                                List.of(
                                        "supervisor@example.com",
                                        "manager@example.com"
                                )
                        )
                ));

        notificationService.notifyCriticalAlert(
                alert,
                snapshot
        );

        verify(emailService).sendCriticalAlertEmail(
                eq(EmailAddress.of("owner@example.com")),
                eq("Administrador"),
                eq(alert.getTitle()),
                eq(alert.getDescription()),
                eq("Sensor Cocina"),
                eq("Cocina Principal")
        );

        verify(emailService).sendCriticalAlertEmail(
                eq(EmailAddress.of("supervisor@example.com")),
                eq("Colaborador"),
                eq(alert.getTitle()),
                eq(alert.getDescription()),
                eq("Sensor Cocina"),
                eq("Cocina Principal")
        );

        verify(emailService).sendCriticalAlertEmail(
                eq(EmailAddress.of("manager@example.com")),
                eq("Colaborador"),
                eq(alert.getTitle()),
                eq(alert.getDescription()),
                eq("Sensor Cocina"),
                eq("Cocina Principal")
        );

        verify(preferencesService).isEmailEnabled(1L);

        verifyNoMoreInteractions(emailService);
    }

    @Test
    void shouldAvoidDuplicateEmailRecipients() {

        Alert alert = createAlert(AlertSeverity.CRITICAL);

        SensorThresholdSnapshot snapshot =
                createSnapshot(1L, 4L);

        mockOwner();
        enableEmailNotifications();

        when(businessEnergyContextFacade
                .fetchBusinessAreaNotificationSnapshot(4L, 1L))
                .thenReturn(Optional.of(
                        new BusinessAreaNotificationSnapshot(
                                4L,
                                1L,
                                "Cocina Principal",
                                List.of(
                                        "owner@example.com",
                                        "OWNER@example.com",
                                        "supervisor@example.com",
                                        "SUPERVISOR@example.com"
                                )
                        )
                ));

        notificationService.notifyCriticalAlert(
                alert,
                snapshot
        );

        verify(emailService, times(2))
                .sendCriticalAlertEmail(
                        any(EmailAddress.class),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        verify(emailService, times(1))
                .sendCriticalAlertEmail(
                        eq(EmailAddress.of("owner@example.com")),
                        eq("Administrador"),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        verify(emailService, times(1))
                .sendCriticalAlertEmail(
                        eq(EmailAddress.of("supervisor@example.com")),
                        eq("Colaborador"),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldNotNotifyForWarningAlerts() {

        notificationService.notifyCriticalAlert(
                createAlert(AlertSeverity.WARNING),
                createSnapshot(1L, 4L)
        );

        verifyNoInteractions(
                iamContextFacade,
                businessEnergyContextFacade,
                emailService,
                preferencesService
        );
    }

    @Test
    void shouldNotNotifyForInformationalAlerts() {

        notificationService.notifyCriticalAlert(
                createAlert(AlertSeverity.INFO),
                createSnapshot(1L, 4L)
        );

        verifyNoInteractions(
                iamContextFacade,
                businessEnergyContextFacade,
                emailService,
                preferencesService
        );
    }

    @Test
    void shouldRejectSensorBelongingToAnotherUser() {

        notificationService.notifyCriticalAlert(
                createAlert(AlertSeverity.CRITICAL),
                createSnapshot(2L, 4L)
        );

        verifyNoInteractions(
                iamContextFacade,
                businessEnergyContextFacade,
                emailService,
                preferencesService
        );
    }

    @Test
    void shouldNotNotifyWhenOwnerDoesNotExist() {

        when(iamContextFacade.fetchNotificationRecipient(1L))
                .thenReturn(Optional.empty());

        notificationService.notifyCriticalAlert(
                createAlert(AlertSeverity.CRITICAL),
                createSnapshot(1L, 4L)
        );

        verifyNoInteractions(
                businessEnergyContextFacade,
                emailService,
                preferencesService
        );
    }

    @Test
    void shouldContinueDeliveryWhenBrevoFailsForOneRecipient() {

        Alert alert = createAlert(AlertSeverity.CRITICAL);

        SensorThresholdSnapshot snapshot =
                createSnapshot(1L, 4L);

        mockOwner();
        enableEmailNotifications();

        when(businessEnergyContextFacade
                .fetchBusinessAreaNotificationSnapshot(4L, 1L))
                .thenReturn(Optional.of(
                        new BusinessAreaNotificationSnapshot(
                                4L,
                                1L,
                                "Cocina Principal",
                                List.of("supervisor@example.com")
                        )
                ));

        doThrow(new RuntimeException("Brevo unavailable"))
                .when(emailService)
                .sendCriticalAlertEmail(
                        eq(EmailAddress.of("owner@example.com")),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        assertDoesNotThrow(() ->
                notificationService.notifyCriticalAlert(
                        alert,
                        snapshot
                )
        );

        verify(emailService).sendCriticalAlertEmail(
                eq(EmailAddress.of("supervisor@example.com")),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void shouldIgnoreInvalidRecipientEmails() {

        mockOwner();
        enableEmailNotifications();

        when(businessEnergyContextFacade
                .fetchBusinessAreaNotificationSnapshot(4L, 1L))
                .thenReturn(Optional.of(
                        new BusinessAreaNotificationSnapshot(
                                4L,
                                1L,
                                "Cocina Principal",
                                List.of(
                                        "invalid-email",
                                        "supervisor@example.com"
                                )
                        )
                ));

        notificationService.notifyCriticalAlert(
                createAlert(AlertSeverity.CRITICAL),
                createSnapshot(1L, 4L)
        );

        verify(emailService, times(2))
                .sendCriticalAlertEmail(
                        any(EmailAddress.class),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        verify(emailService).sendCriticalAlertEmail(
                eq(EmailAddress.of("supervisor@example.com")),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void shouldOnlyNotifyOwnerWhenBusinessAreaIsUnavailable() {

        mockOwner();
        enableEmailNotifications();

        when(businessEnergyContextFacade
                .fetchBusinessAreaNotificationSnapshot(4L, 1L))
                .thenReturn(Optional.empty());

        notificationService.notifyCriticalAlert(
                createAlert(AlertSeverity.CRITICAL),
                createSnapshot(1L, 4L)
        );

        verify(emailService).sendCriticalAlertEmail(
                eq(EmailAddress.of("owner@example.com")),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq("Área no disponible")
        );

        verifyNoMoreInteractions(emailService);
    }

    @Test
    void shouldNotNotifyWhenAlertHasNoPersistedId() {

        Alert alert = createAlert(AlertSeverity.CRITICAL);

        alert.setId(null);

        notificationService.notifyCriticalAlert(
                alert,
                createSnapshot(1L, 4L)
        );

        verifyNoInteractions(
                iamContextFacade,
                businessEnergyContextFacade,
                emailService,
                preferencesService
        );
    }

    /**
     * US-38: Prevents personal email notifications
     * when the user has disabled the email channel.
     */
    @Test
    void shouldNotNotifyOwnerWhenEmailNotificationsAreDisabled() {

        mockOwner();

        when(preferencesService.isEmailEnabled(1L))
                .thenReturn(false);

        notificationService.notifyCriticalAlert(
                createAlert(AlertSeverity.CRITICAL),
                createSnapshot(1L, null)
        );

        verify(preferencesService, times(1))
                .isEmailEnabled(1L);

        verifyNoInteractions(
                businessEnergyContextFacade,
                emailService
        );
    }

    /**
     * US-38: Prevents business area email delivery
     * when the owner's email preference is disabled.
     */
    @Test
    void shouldNotNotifyBusinessAreaWhenEmailNotificationsAreDisabled() {

        mockOwner();

        when(preferencesService.isEmailEnabled(1L))
                .thenReturn(false);

        notificationService.notifyCriticalAlert(
                createAlert(AlertSeverity.CRITICAL),
                createSnapshot(1L, 4L)
        );

        verify(iamContextFacade)
                .fetchNotificationRecipient(1L);

        verify(preferencesService)
                .isEmailEnabled(1L);

        verifyNoInteractions(
                businessEnergyContextFacade,
                emailService
        );
    }

    private void mockOwner() {

        when(iamContextFacade.fetchNotificationRecipient(1L))
                .thenReturn(Optional.of(
                        new NotificationRecipient(
                                1L,
                                "owner@example.com",
                                "Administrador"
                        )
                ));
    }

    private void enableEmailNotifications() {

        when(preferencesService.isEmailEnabled(1L))
                .thenReturn(true);
    }

    private Alert createAlert(AlertSeverity severity) {

        return new Alert(
                100L,
                1L,
                10L,
                5L,
                "Exceso de potencia detectado",
                "El consumo supera el límite configurado.",
                severity,
                "/dispositivos",
                false,
                AlertStatus.ACTIVE,
                LocalDateTime.now()
        );
    }

    private SensorThresholdSnapshot createSnapshot(
            Long userId,
            Long businessAreaId) {

        return new SensorThresholdSnapshot(
                10L,
                userId,
                "Sensor Cocina",
                5L,
                "Refrigeradora",
                3.0,
                8.0,
                3.0,
                1.5,
                businessAreaId
        );
    }
}
