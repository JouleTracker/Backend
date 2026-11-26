package com.jouletracker.platform.iam.infrastructure.security.configuration;

import com.jouletracker.platform.iam.infrastructure.security.entrypoint.BearerAccessDeniedHandler;
import com.jouletracker.platform.iam.infrastructure.security.entrypoint.BearerAuthenticationEntryPoint;
import com.jouletracker.platform.iam.infrastructure.security.filter.JwtAuthenticationFilter;
import com.jouletracker.platform.shared.infrastructure.configuration.ApplicationProperties;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the JouleTracker Platform.
 *
 * <p>Configures stateless JWT-based authentication, BCrypt password hashing,
 * restricted CORS policy, method-level security, and role-based request authorization.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@NullMarked
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final BearerAuthenticationEntryPoint authenticationEntryPoint;
    private final BearerAccessDeniedHandler accessDeniedHandler;
    private final ApplicationProperties applicationProperties;

    public SecurityConfiguration(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            BearerAuthenticationEntryPoint authenticationEntryPoint,
            BearerAccessDeniedHandler accessDeniedHandler,
            ApplicationProperties applicationProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Password encoder bean utilizing BCrypt with a work factor of 12.
     *
     * @return the configured PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Exposes Spring Security's {@link AuthenticationManager}.
     *
     * @param authenticationConfiguration the Spring authentication configuration
     * @return the authentication manager bean
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the main HTTP security filter chain.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless REST API using Bearer tokens; CSRF protection is disabled as sessions are not stored
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public IAM authentication, verification, and password recovery endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/authentication/sign-up").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/authentication/sign-in").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/authentication/verify-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/authentication/resend-verification").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/authentication/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/authentication/reset-password").permitAll()
                        // Public API documentation
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Administrative moderation endpoints (US-49)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/status").hasRole("ADMIN")
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS policy restricting allowed origins to the configured frontend application.
     *
     * @return the configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(applicationProperties.getNormalizedFrontendBaseUrl()));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
