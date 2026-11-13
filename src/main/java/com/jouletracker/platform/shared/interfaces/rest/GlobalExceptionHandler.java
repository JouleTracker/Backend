package com.jouletracker.platform.shared.interfaces.rest;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.MessageFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Global exception handler for REST API.
 * Provides centralized exception handling for the entire application,
 * ensuring all unhandled exceptions are translated to consistent
 * RFC 9457 ProblemDetail HTTP responses.
 */
@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MESSAGES_BASENAME = "messages";

    /**
     * Handles validation exceptions from Spring's request body validation.
     * Maps validation failure to a standardized RFC 9457 ProblemDetail response.
     *
     * @param ex the validation exception from @Valid binding
     * @return error response with BAD_REQUEST status and detailed invalid parameters
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors();
        var validationPrefix = resolveMessageOrDefault("validation.field.prefix", "Field");
        var errorDetails = fieldErrors.isEmpty()
                ? resolveMessageOrDefault("validation.request.failed", "Request validation failed")
                : fieldErrors.stream()
                .map(error -> "%s %s: %s".formatted(
                        validationPrefix,
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .reduce((a, b) -> a + "; " + b)
                .orElse(resolveMessageOrDefault("validation.request.failed", "Request validation failed"));

        var invalidParams = fieldErrors.stream()
                .map(err -> Map.of(
                        "field", err.getField(),
                        "message", err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid value"
                ))
                .toList();

        var applicationError = ApplicationError.validationError("request-body", errorDetails);
        var problemDetail = ProblemDetailFromApplicationErrorAssembler.toProblemDetailFromApplicationError(applicationError);
        if (!invalidParams.isEmpty()) {
            problemDetail.setProperty("invalidParams", invalidParams);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Handles missing or malformed JSON request payloads.
     *
     * @param ex the HTTP message not readable exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        var applicationError = ApplicationError.validationError(
                "request-body",
                resolveMessageOrDefault("validation.request.malformed", "Request body is missing or malformed JSON")
        );
        return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(applicationError);
    }

    /**
     * Handles unsupported or missing Content-Type headers when a JSON request body is expected.
     *
     * @param ex the HTTP media type not supported exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        var applicationError = ApplicationError.validationError(
                "Content-Type",
                resolveMessageOrDefault("validation.request.media-type", "Request body must be sent with Content-Type: application/json")
        );
        return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(applicationError);
    }

    /**
     * Handles missing required query or form parameters.
     *
     * @param ex the missing request parameter exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        var applicationError = ApplicationError.validationError(
                ex.getParameterName(),
                resolveMessageOrDefault(
                        "validation.parameter.missing",
                        "Required request parameter '" + ex.getParameterName() + "' is missing",
                        ex.getParameterName()
                )
        );
        return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(applicationError);
    }

    /**
     * Handles invalid request arguments such as malformed UUID path or payload values.
     *
     * @param ex the illegal argument exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        var applicationError = ApplicationError.validationError(
                resolveMessageOrDefault("validation.request.argument", "request-argument"),
                ex.getMessage() != null ? ex.getMessage() : resolveMessageOrDefault("validation.request.failed", "Request validation failed")
        );
        return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(applicationError);
    }

    /**
     * Handles unexpected runtime exceptions not caught by specific handlers.
     * Maps to a generic unexpected error response.
     *
     * @param ex the unhandled runtime exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(RuntimeException ex) {
        LOGGER.error("Unhandled runtime exception caught by GlobalExceptionHandler", ex);
        var applicationError = ApplicationError.unexpected(
                resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
                "An unexpected error occurred"
        );
        return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(applicationError);
    }

    /**
     * Handles all other exceptions not matched by specific handlers.
     * Provides a final fallback for any unexpected exception type.
     *
     * @param ex the exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception ex) {
        LOGGER.error("Unhandled exception caught by GlobalExceptionHandler", ex);
        var applicationError = ApplicationError.unexpected(
                resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
                "An unexpected error occurred"
        );
        return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(applicationError);
    }

    private String resolveMessageOrDefault(String key, String defaultValue, Object... args) {
        try {
            var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, LocaleContextHolder.getLocale());
            if (!bundle.containsKey(key)) {
                return defaultValue;
            }
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException ex) {
            return defaultValue;
        }
    }
}