package com.jouletracker.platform.iam.infrastructure.tokens.jwt;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.services.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Adapter implementing {@link TokenService} using the modern JJWT 0.13.0 API.
 *
 * <p>Handles HMAC-SHA256 signing, claims extraction, and token expiration verification.</p>
 */
@Service
@NullMarked
public class JwtTokenService implements TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenService.class);

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(User user) {
        Objects.requireNonNull(user, "User must not be null");

        List<String> roles = user.getRoles().stream()
                .map(Role::name)
                .toList();

        long now = System.currentTimeMillis();
        long expirationMillis = now + ((long) jwtProperties.getExpirationDays() * 24L * 60L * 60L * 1000L);

        var builder = Jwts.builder()
                .subject(user.getEmail().value())
                .claim("name", user.getFullName())
                .claim("roles", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(expirationMillis))
                .signWith(getSigningKey());

        if (user.getId().value() != null) {
            builder.claim("id", user.getId().value());
        }

        return builder.compact();
    }

    @Override
    public boolean validateToken(String token) {
        if (token.isBlank()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            LOGGER.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public @Nullable Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        Object idVal = claims.get("id");
        if (idVal instanceof Number num) {
            return num.longValue();
        }
        if (idVal instanceof String str && !str.isBlank()) {
            try {
                return Long.valueOf(str);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Failed to parse Long user ID from JWT claim: {}", idVal);
                return null;
            }
        }
        return null;
    }

    @Override
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return Collections.emptyList();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
