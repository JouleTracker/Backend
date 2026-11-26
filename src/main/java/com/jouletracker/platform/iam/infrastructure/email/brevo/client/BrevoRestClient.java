package com.jouletracker.platform.iam.infrastructure.email.brevo.client;

import com.jouletracker.platform.iam.infrastructure.email.brevo.BrevoProperties;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private static final int MAX_LOGGED_RESPONSE_LENGTH = 500;

    private final BrevoProperties properties;
    private final RestClient restClient;

    @Autowired
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

    BrevoRestClient(BrevoProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    /**
     * Dispatches a transactional email to Brevo.
     *
     * @param request the structured Brevo email payload
     */
    public void sendTransactionalEmail(BrevoSendSmtpEmailRequest request) {
        String senderEmail = request.sender() != null ? request.sender().email() : properties.getSenderEmail();
        String apiUrl = properties.getApiUrl();

        if (properties.hasMissingApiKey()) {
            LOGGER.error(
                    "Envío de correo por Brevo abortado: BREVO_API_KEY no está configurada o está vacía (endpoint={}, remitente={}).",
                    apiUrl, senderEmail
            );
            throw new IllegalStateException("La clave API de Brevo (BREVO_API_KEY) no está configurada");
        }

        if (properties.hasPlaceholderApiKey()) {
            LOGGER.warn(
                    "BREVO_API_KEY parece contener un valor de marcador de posición (placeholder); verifique la configuración antes de enviar correos vía {} (remitente={}).",
                    apiUrl, senderEmail
            );
        }

        try {
            ResponseEntity<Void> response = restClient.post()
                    .header("api-key", properties.getApiKey())
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            if (!response.getStatusCode().is2xxSuccessful()) {
                LOGGER.error(
                        "La API de Brevo devolvió un estado HTTP no exitoso: {} (endpoint={}, remitente={}).",
                        response.getStatusCode().value(), apiUrl, senderEmail
                );
                throw new IllegalStateException(
                        "Error al enviar correo transaccional vía Brevo: estado HTTP " + response.getStatusCode().value()
                );
            }

            LOGGER.info(
                    "La API de Brevo aceptó el correo transaccional con estado HTTP {} (remitente={}).",
                    response.getStatusCode().value(), senderEmail
            );
        } catch (RestClientResponseException ex) {
            String responseBody = sanitizeResponseBody(ex.getResponseBodyAsString());
            LOGGER.error(
                    "La API de Brevo rechazó el envío con estado HTTP {} (endpoint={}, remitente={}, detalle='{}').",
                    ex.getStatusCode().value(), apiUrl, senderEmail, responseBody
            );
            throw new IllegalStateException(
                    "Error al enviar correo transaccional vía Brevo: HTTP " + ex.getStatusCode().value(), ex
            );
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(
                    "Error inesperado al conectar con la API de Brevo (endpoint={}, remitente={}, causa={}: {}).",
                    apiUrl, senderEmail, ex.getClass().getSimpleName(), ex.getMessage(), ex
            );
            throw new IllegalStateException("Error inesperado de conexión durante el envío de correo vía Brevo", ex);
        }
    }

    private static String sanitizeResponseBody(String body) {
        if (body == null || body.isBlank()) {
            return "(sin cuerpo de respuesta)";
        }
        String singleLine = body.replaceAll("[\\r\\n]+", " ").trim();
        if (singleLine.length() > MAX_LOGGED_RESPONSE_LENGTH) {
            return singleLine.substring(0, MAX_LOGGED_RESPONSE_LENGTH) + "...";
        }
        return singleLine;
    }
}
