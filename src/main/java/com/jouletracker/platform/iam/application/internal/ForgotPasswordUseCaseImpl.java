package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.ForgotPasswordCommand;
import com.jouletracker.platform.iam.application.usecases.ForgotPasswordUseCase;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.PasswordResetToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.repositories.PasswordResetTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.EmailService;
import com.jouletracker.platform.iam.infrastructure.configuration.PasswordResetTokenProperties;
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
 * Implementation of {@link ForgotPasswordUseCase} orchestrating password recovery token generation
 * and transactional email delivery via Brevo (TS-03).
 */
@Service
@NullMarked
public class ForgotPasswordUseCaseImpl implements ForgotPasswordUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForgotPasswordUseCaseImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String GENERIC_SUCCESS_MESSAGE =
            "Si existe una cuenta asociada al correo electrónico proporcionado, se ha enviado un enlace de recuperación de contraseña.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordResetTokenProperties tokenProperties;

    @Autowired
    public ForgotPasswordUseCaseImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService,
            PasswordResetTokenProperties tokenProperties) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.tokenProperties = Objects.requireNonNull(tokenProperties, "PasswordResetTokenProperties must not be null");
    }

    public ForgotPasswordUseCaseImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService) {
        this(userRepository, passwordResetTokenRepository, emailService, new PasswordResetTokenProperties());
    }

    @Override
    @Transactional
    public Result<String, ApplicationError> handle(ForgotPasswordCommand command) {
        EmailAddress emailAddress;
        try {
            emailAddress = EmailAddress.of(command.email());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("email", ex.getMessage()));
        }

        Optional<User> optionalUser = userRepository.findByEmail(emailAddress);
        if (optionalUser.isEmpty()) {
            LOGGER.info("Password reset requested for non-existent email; returning generic response to prevent account enumeration.");
            return Result.success(GENERIC_SUCCESS_MESSAGE);
        }

        User user = optionalUser.get();
        if (user.isDeactivated() || user.isRestricted()) {
            LOGGER.info("Password reset skipped for deactivated or restricted account.");
            return Result.success(GENERIC_SUCCESS_MESSAGE);
        }

        // Invalidate any previously issued recovery tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        PasswordResetToken resetToken = PasswordResetToken.create(
                user.getId(),
                rawToken,
                tokenProperties.getValidityDuration()
        );

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName(),
                resetToken.getToken()
        );

        LOGGER.info("Password recovery token generated and reset email dispatched.");
        return Result.success(GENERIC_SUCCESS_MESSAGE);
    }
}
