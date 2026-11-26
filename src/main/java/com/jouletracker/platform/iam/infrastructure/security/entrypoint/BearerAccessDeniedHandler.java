package com.jouletracker.platform.iam.infrastructure.security.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Access denied handler translating Spring Security authorization failures (HTTP 403)
 * into RFC 9457 {@link org.springframework.http.ProblemDetail} JSON responses.
 */
@Component
@NullMarked
public class BearerAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public BearerAccessDeniedHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Autowired(required = false)
    public BearerAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        var applicationError = ApplicationError.forbidden(
                "No tienes permisos suficientes para acceder a este recurso"
        );

        var problemDetail = ProblemDetailFromApplicationErrorAssembler
                .toProblemDetailFromApplicationError(applicationError);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
