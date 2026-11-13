package com.jouletracker.platform.iam.infrastructure.email.brevo.client;

import com.jouletracker.platform.iam.infrastructure.email.brevo.BrevoProperties;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * HTTP client communicating with the Brevo v3 Transactional Email REST endpoint.
 *
 * <p>Built using Spring's native {@link RestClient} to ensure lightweight execution
 * and high concurrency compatibility with Project Loom virtual threads.</p>
 */
@Component
@NullMarked
public class BrevoRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevoRestClient.class);

    private final BrevoProperties properties;
    private final RestClient restClient;

    public BrevoRestClient(BrevoProperties properties) {
        this.properties = properties;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(properties.getConnectTimeoutSeconds()));
        requestFactory.setReadTimeout(java.time.Duration.ofSeconds(properties.getReadTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .requestFactory(requestFactory)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Dispatches a transactional email to Brevo.
     *
     * @param request the structured Brevo email payload
     */
    public void sendTransactionalEmail(BrevoSendSmtpEmailRequest request) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("Brevo API key is not configured");
        }

        try {
            restClient.post()
                    .header("api-key", properties.getApiKey())
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("Transactional email successfully sent via Brevo to recipient.");
        } catch (RestClientResponseException ex) {
            LOGGER.error("Brevo API responded with error status: {}", ex.getStatusCode());
            throw new IllegalStateException("Failed to send transactional email via Brevo: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error connecting to Brevo API.");
            throw new IllegalStateException("Unexpected failure during Brevo email delivery", ex);
        }
    }
}
