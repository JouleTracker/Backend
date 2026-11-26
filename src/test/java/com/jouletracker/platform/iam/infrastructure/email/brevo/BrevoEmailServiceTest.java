package com.jouletracker.platform.iam.infrastructure.email.brevo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.infrastructure.configuration.PasswordResetTokenProperties;
import com.jouletracker.platform.iam.infrastructure.configuration.VerificationTokenProperties;
import com.jouletracker.platform.iam.infrastructure.email.brevo.client.BrevoRestClient;
import com.jouletracker.platform.iam.infrastructure.email.brevo.client.BrevoSendSmtpEmailRequest;
import com.jouletracker.platform.shared.infrastructure.configuration.ApplicationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrevoEmailService Diagnostic Logging & Error Handling Tests")
class BrevoEmailServiceTest {

    @Mock
    private BrevoRestClient brevoRestClient;

    private BrevoProperties brevoProperties;
    private ApplicationProperties applicationProperties;
    private BrevoEmailService emailService;
    private Logger serviceLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        brevoProperties = new BrevoProperties();
        brevoProperties.setApiKey("xkeysib-real-configured-key");
        brevoProperties.setSenderEmail("no-reply@jouletracker.com");
        brevoProperties.setSenderName("JouleTracker Platform");
        brevoProperties.setApiUrl("https://api.brevo.com/v3/smtp/email");

        applicationProperties = new ApplicationProperties();
        applicationProperties.setFrontendUrl("http://localhost:4200");

        emailService = new BrevoEmailService(
                brevoRestClient,
                brevoProperties,
                applicationProperties,
                new VerificationTokenProperties(),
                new PasswordResetTokenProperties()
        );

        serviceLogger = (Logger) LoggerFactory.getLogger(BrevoEmailService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        serviceLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        serviceLogger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("Should log non-sensitive INFO diagnostics when sending verification email")
    void shouldLogInfoDiagnosticsOnVerificationEmailSuccess() {
        String secretToken = "super-secret-verification-token-123";
        emailService.sendVerificationEmail(
                EmailAddress.of("john.doe@jouletracker.com"),
                "John Doe",
                secretToken
        );

        verify(brevoRestClient).sendTransactionalEmail(any(BrevoSendSmtpEmailRequest.class));

        assertThat(listAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.INFO);
                    assertThat(event.getFormattedMessage())
                            .contains("verificación de cuenta")
                            .contains("j***@jouletracker.com");
                })
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.INFO);
                    assertThat(event.getFormattedMessage()).contains("aceptado por Brevo");
                })
                .noneSatisfy(event -> assertThat(event.getFormattedMessage())
                        .containsAnyOf(secretToken, brevoProperties.getApiKey(), "john.doe@jouletracker.com"));
    }

    @Test
    @DisplayName("Should log WARN when BREVO_API_KEY is a placeholder without leaking the key")
    void shouldLogWarnOnPlaceholderApiKey() {
        String placeholderKey = "xkeysib-change_me_brevo_api_key";
        brevoProperties.setApiKey(placeholderKey);

        emailService.sendPasswordResetEmail(
                EmailAddress.of("jane.doe@jouletracker.com"),
                "Jane Doe",
                "secret-reset-token-456"
        );

        assertThat(listAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.WARN);
                    assertThat(event.getFormattedMessage())
                            .contains("BREVO_API_KEY")
                            .contains("placeholder")
                            .contains("recuperación de contraseña");
                })
                .noneSatisfy(event -> assertThat(event.getFormattedMessage())
                        .containsAnyOf(placeholderKey, "secret-reset-token-456"));
    }

    @Test
    @DisplayName("Should log ERROR when BREVO_API_KEY is missing and propagate failure")
    void shouldLogErrorWhenApiKeyIsMissingAndPropagateFailure() {
        brevoProperties.setApiKey("   ");
        doThrow(new IllegalStateException("La clave API de Brevo (BREVO_API_KEY) no está configurada"))
                .when(brevoRestClient).sendTransactionalEmail(any());

        assertThatThrownBy(() -> emailService.sendVerificationEmail(
                EmailAddress.of("john.doe@jouletracker.com"),
                "John Doe",
                "secret-token"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BREVO_API_KEY");

        assertThat(listAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getFormattedMessage()).contains("BREVO_API_KEY no está configurada");
                })
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getFormattedMessage()).contains("Fallo al enviar correo de verificación de cuenta");
                });
    }

    @Test
    @DisplayName("Should deduplicate identical greeting name (case-insensitive) in verification email")
    void shouldDeduplicateGreetingNameInVerificationEmail() {
        emailService.sendVerificationEmail(
                EmailAddress.of("rodrigo@jouletracker.com"),
                "rodrigo RODRIGO",
                "verification-token"
        );

        ArgumentCaptor<BrevoSendSmtpEmailRequest> captor = ArgumentCaptor.forClass(BrevoSendSmtpEmailRequest.class);
        verify(brevoRestClient).sendTransactionalEmail(captor.capture());

        BrevoSendSmtpEmailRequest request = captor.getValue();
        assertThat(request.to().getFirst().name()).isEqualTo("rodrigo");
        assertThat(request.htmlContent())
                .contains("Hello <strong>rodrigo</strong>,")
                .doesNotContain("rodrigo RODRIGO")
                .doesNotContain("rodrigo rodrigo");
    }

    @Test
    @DisplayName("Should deduplicate identical greeting name in password reset email and preserve distinct names")
    void shouldDeduplicateGreetingNameInPasswordResetEmail() {
        emailService.sendPasswordResetEmail(
                EmailAddress.of("rodrigo@jouletracker.com"),
                "Rodrigo rodrigo",
                "reset-token"
        );

        ArgumentCaptor<BrevoSendSmtpEmailRequest> captor = ArgumentCaptor.forClass(BrevoSendSmtpEmailRequest.class);
        verify(brevoRestClient).sendTransactionalEmail(captor.capture());

        BrevoSendSmtpEmailRequest request = captor.getValue();
        assertThat(request.to().getFirst().name()).isEqualTo("Rodrigo");
        assertThat(request.htmlContent())
                .contains("Hello <strong>Rodrigo</strong>,")
                .doesNotContain("Rodrigo rodrigo");

        assertThat(BrevoEmailService.formatGreetingName("rodrigo", "rodrigo")).isEqualTo("rodrigo");
        assertThat(BrevoEmailService.formatGreetingName("rodrigo", "RODRIGO")).isEqualTo("rodrigo");
        assertThat(BrevoEmailService.formatGreetingName("rodrigo", "")).isEqualTo("rodrigo");
        assertThat(BrevoEmailService.formatGreetingName("rodrigo", null)).isEqualTo("rodrigo");
        assertThat(BrevoEmailService.formatGreetingName("Jane", "Doe")).isEqualTo("Jane Doe");
    }
}
