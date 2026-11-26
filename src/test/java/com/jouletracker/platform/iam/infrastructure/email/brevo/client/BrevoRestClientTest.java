package com.jouletracker.platform.iam.infrastructure.email.brevo.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jouletracker.platform.iam.infrastructure.email.brevo.BrevoProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

@DisplayName("BrevoRestClient Logging & Error Handling Tests")
class BrevoRestClientTest {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private BrevoProperties properties;
    private MockRestServiceServer mockServer;
    private BrevoRestClient brevoRestClient;
    private Logger clientLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        properties = new BrevoProperties();
        properties.setApiKey("xkeysib-valid-test-key");
        properties.setSenderEmail("no-reply@jouletracker.com");
        properties.setSenderName("JouleTracker Platform");
        properties.setApiUrl(BREVO_URL);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BREVO_URL)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        brevoRestClient = new BrevoRestClient(properties, builder.build());

        clientLogger = (Logger) LoggerFactory.getLogger(BrevoRestClient.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        clientLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        clientLogger.detachAppender(listAppender);
    }

    private BrevoSendSmtpEmailRequest sampleRequest() {
        return new BrevoSendSmtpEmailRequest(
                new BrevoSendSmtpEmailRequest.Sender("JouleTracker Platform", "no-reply@jouletracker.com"),
                List.of(new BrevoSendSmtpEmailRequest.Recipient("user@jouletracker.com", "User")),
                "Confirm your account",
                "<p>Hello</p>"
        );
    }

    @Test
    @DisplayName("Should log ERROR and fail immediately when BREVO_API_KEY is missing")
    void shouldLogErrorAndFailWhenApiKeyIsBlank() {
        properties.setApiKey("");

        assertThatThrownBy(() -> brevoRestClient.sendTransactionalEmail(sampleRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BREVO_API_KEY");

        assertThat(listAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getFormattedMessage()).contains("BREVO_API_KEY no está configurada");
                });
    }

    @Test
    @DisplayName("Should log INFO on 201 Created response from Brevo")
    void shouldLogInfoOnSuccessfulDispatch() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "xkeysib-valid-test-key"))
                .andRespond(withStatus(HttpStatus.CREATED));

        brevoRestClient.sendTransactionalEmail(sampleRequest());

        mockServer.verify();
        assertThat(listAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.INFO);
                    assertThat(event.getFormattedMessage()).contains("201");
                })
                .noneSatisfy(event -> assertThat(event.getFormattedMessage()).contains("xkeysib-valid-test-key"));
    }

    @Test
    @DisplayName("Should log WARN for placeholder key and ERROR with status and response body on 401 from Brevo")
    void shouldLogWarnAndErrorOnNon2xxResponse() {
        String placeholderKey = "xkeysib-change_me_brevo_api_key";
        properties.setApiKey(placeholderKey);

        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withUnauthorizedRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"unauthorized\",\"message\":\"Key not found\"}"));

        assertThatThrownBy(() -> brevoRestClient.sendTransactionalEmail(sampleRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("401");

        mockServer.verify();
        assertThat(listAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.WARN);
                    assertThat(event.getFormattedMessage()).contains("placeholder");
                })
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getFormattedMessage())
                            .contains("401")
                            .contains("Key not found");
                })
                .noneSatisfy(event -> assertThat(event.getFormattedMessage()).contains(placeholderKey));
    }

    @Test
    @DisplayName("Should log ERROR and throw on non-2xx 3xx redirect status from Brevo")
    void shouldLogErrorAndThrowOnNon2xxRedirect() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.FOUND));

        assertThatThrownBy(() -> brevoRestClient.sendTransactionalEmail(sampleRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("302");

        mockServer.verify();
        assertThat(listAppender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getFormattedMessage()).contains("302");
                });
    }
}
