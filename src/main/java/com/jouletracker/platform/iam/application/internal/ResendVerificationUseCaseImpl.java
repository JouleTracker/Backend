package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.ResendVerificationCommand;
import com.jouletracker.platform.iam.application.usecases.ResendVerificationUseCase;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.repositories.EmailVerificationTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.EmailService;
import com.jouletracker.platform.iam.infrastructure.configuration.VerificationTokenProperties;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link ResendVerificationUseCase} issuing fresh email verification links
 * for unverified accounts via Brevo (US-04).
 */
@Service
@NullMarked
public class ResendVerificationUseCaseImpl implements ResendVerificationUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResendVerificationUseCaseImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final VerificationTokenProperties verificationTokenProperties;

    @Autowired
    public ResendVerificationUseCaseImpl(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService,
            VerificationTokenProperties verificationTokenProperties) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
        this.verificationTokenProperties = Objects.requireNonNull(
                verificationTokenProperties,
                "VerificationTokenProperties must not be null"
        );
    }

    public ResendVerificationUseCaseImpl(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService) {
        this(userRepository, emailVerificationTokenRepository, emailService, new VerificationTokenProperties());
    }

    @Override
    @Transactional
    public Result<String, ApplicationError> handle(ResendVerificationCommand command) {
        EmailAddress emailAddress;
        try {
            emailAddress = EmailAddress.of(command.email());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("email", ex.getMessage()));
        }

        Optional<User> optionalUser = userRepository.findByEmail(emailAddress);
        if (optionalUser.isEmpty()) {
            LOGGER.info("Resend verification rejected: account was not found.");
            return Result.failure(ApplicationError.notFound("User", "No se encontró ninguna cuenta asociada al correo electrónico proporcionado"));
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            LOGGER.info("Resend verification rejected: account email is already verified.");
            return Result.failure(ApplicationError.conflict(
                    "EmailVerification",
                    "El correo electrónico de la cuenta ya está verificado"
            ));
        }

        if (user.isDeactivated() || user.isRestricted()) {
            LOGGER.info("Resend verification rejected: account is deactivated or restricted.");
            return Result.failure(ApplicationError.forbidden(
                    "No se puede reenviar el correo de verificación a una cuenta desactivada o restringida"
            ));
        }

        // Invalidate prior unused tokens before issuing a fresh single-use token
        emailVerificationTokenRepository.deleteByUserId(user.getId());

        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        EmailVerificationToken verificationToken = EmailVerificationToken.create(
                user.getId(),
                rawToken,
                verificationTokenProperties.getValidityDuration()
        );

        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                verificationToken.getToken()
        );

        LOGGER.info("New verification token generated and email dispatched.");
        return Result.success("El correo de verificación ha sido reenviado correctamente.");
    }
}
