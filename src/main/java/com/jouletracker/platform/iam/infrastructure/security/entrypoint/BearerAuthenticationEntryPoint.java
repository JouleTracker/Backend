package com.jouletracker.platform.iam.infrastructure.security.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Authentication entry point translating unauthenticated requests into RFC 9457 {@link org.springframework.http.ProblemDetail} JSON responses.
 */
@Component
@NullMarked
public class BearerAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public BearerAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
    }

    @Autowired(required = false)
    public BearerAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        var applicationError = ApplicationError.unauthorized(
                authException.getMessage() != null
                        ? authException.getMessage()
                        : "Full authentication is required to access this resource"
        );

        var problemDetail = ProblemDetailFromApplicationErrorAssembler
                .toProblemDetailFromApplicationError(applicationError);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
