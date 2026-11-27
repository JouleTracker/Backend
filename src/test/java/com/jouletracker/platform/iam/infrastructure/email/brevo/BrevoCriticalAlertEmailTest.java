
package com.jouletracker.platform.iam.infrastructure.email.brevo;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.infrastructure.configuration.PasswordResetTokenProperties;
import com.jouletracker.platform.iam.infrastructure.configuration.VerificationTokenProperties;
import com.jouletracker.platform.iam.infrastructure.email.brevo.client.BrevoRestClient;
import com.jouletracker.platform.iam.infrastructure.email.brevo.client.BrevoSendSmtpEmailRequest;
import com.jouletracker.platform.shared.infrastructure.configuration.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TS-19: Tests the Brevo critical alert email adapter.
 */
@ExtendWith(MockitoExtension.class)
class BrevoCriticalAlertEmailTest {

    @Mock
    private BrevoRestClient brevoRestClient;

    private BrevoEmailService emailService;

    @BeforeEach
    void setUp() {

        BrevoProperties brevoProperties =
                new BrevoProperties();

        brevoProperties.setApiKey(
                "xkeysib-test-configured-key"
        );

        brevoProperties.setSenderEmail(
                "no-reply@jouletracker.com"
        );

        brevoProperties.setSenderName(
                "JouleTracker Platform"
        );

        brevoProperties.setApiUrl(
                "https://api.brevo.com/v3/smtp/email"
        );

        ApplicationProperties applicationProperties =
                new ApplicationProperties();

        applicationProperties.setFrontendUrl(
                "http://localhost:4200"
        );

        emailService = new BrevoEmailService(
                brevoRestClient,
                brevoProperties,
                applicationProperties,
                new VerificationTokenProperties(),
                new PasswordResetTokenProperties()
        );
    }

    @Test
    void shouldSendCriticalAlertEmailWithCorrectRecipient() {

        emailService.sendCriticalAlertEmail(
                EmailAddress.of("owner@example.com"),
                "Administrador",
                "Exceso de potencia",
                "El consumo supera el limite configurado.",
                "Sensor Cocina",
                "Cocina Principal"
        );

        BrevoSendSmtpEmailRequest request =
                captureRequest();

        assertThat(request.sender().email())
                .isEqualTo("no-reply@jouletracker.com");

        assertThat(request.sender().name())
                .isEqualTo("JouleTracker Platform");

        assertThat(request.to()).hasSize(1);

        assertThat(request.to().getFirst().email())
                .isEqualTo("owner@example.com");

        assertThat(request.to().getFirst().name())
                .isEqualTo("Administrador");

        assertThat(request.subject())
                .isEqualTo(
                        "JouleTracker | Alerta crítica de consumo energético"
                );

        verifyNoMoreInteractions(brevoRestClient);
    }

    @Test
    void shouldIncludeCriticalAlertDetailsInHtmlContent() {

        emailService.sendCriticalAlertEmail(
                EmailAddress.of("owner@example.com"),
                "Administrador",
                "Exceso de potencia",
                "El sensor supero los 5 kW.",
                "Sensor Cocina",
                "Cocina Principal"
        );

        BrevoSendSmtpEmailRequest request =
                captureRequest();

        assertThat(request.htmlContent())
                .contains("Administrador")
                .contains("Exceso de potencia")
                .contains("El sensor supero los 5 kW.")
                .contains("Sensor Cocina")
                .contains("Cocina Principal")
                .contains("JouleTracker");
    }

    @Test
    void shouldEscapeUnsafeHtmlInAlertContent() {

        emailService.sendCriticalAlertEmail(
                EmailAddress.of("owner@example.com"),
                "<b>Administrador</b>",
                "<script>test</script>",
                "<img src=x onerror=alert(1)>",
                "<sensor>",
                "<area>"
        );

        BrevoSendSmtpEmailRequest request =
                captureRequest();

        assertThat(request.htmlContent())
                .contains("&lt;b&gt;Administrador&lt;/b&gt;")
                .contains("&lt;script&gt;test&lt;/script&gt;")
                .contains("&lt;img src=x onerror=alert(1)&gt;")
                .contains("&lt;sensor&gt;")
                .contains("&lt;area&gt;")
                .doesNotContain("<script>test</script>")
                .doesNotContain("<img src=x onerror=alert(1)>")
                .doesNotContain("<sensor>")
                .doesNotContain("<area>");
    }

    @Test
    void shouldPropagateBrevoDeliveryFailure() {

        doThrow(new IllegalStateException("Brevo unavailable"))
                .when(brevoRestClient)
                .sendTransactionalEmail(
                        any(BrevoSendSmtpEmailRequest.class)
                );

        assertThatThrownBy(() ->
                emailService.sendCriticalAlertEmail(
                        EmailAddress.of("owner@example.com"),
                        "Administrador",
                        "Exceso de potencia",
                        "Limite superado.",
                        "Sensor Cocina",
                        "Cocina Principal"
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Brevo unavailable");

        verify(brevoRestClient, times(1))
                .sendTransactionalEmail(
                        any(BrevoSendSmtpEmailRequest.class)
                );
    }

    private BrevoSendSmtpEmailRequest captureRequest() {

        ArgumentCaptor<BrevoSendSmtpEmailRequest> captor =
                ArgumentCaptor.forClass(
                        BrevoSendSmtpEmailRequest.class
                );

        verify(brevoRestClient, times(1))
                .sendTransactionalEmail(captor.capture());

        return captor.getValue();
    }
}
