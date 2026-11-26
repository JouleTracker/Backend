package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.ResetPasswordCommand;
import com.jouletracker.platform.iam.application.usecases.ResetPasswordUseCase;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.PasswordResetToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.repositories.PasswordResetTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.shared.domain.model.valueobjects.PasswordPolicy;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link ResetPasswordUseCase} validating single-use password recovery tokens
 * and securely updating credentials using BCrypt (TS-03).
 */
@Service
@NullMarked
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetPasswordUseCaseImpl.class);

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;

    public ResetPasswordUseCaseImpl(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordHashingService passwordHashingService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
    }

    @Override
    @Transactional
    public Result<String, ApplicationError> handle(ResetPasswordCommand command) {
        if (command.token().isBlank()) {
            return Result.failure(ApplicationError.validationError("token", "Password reset token must not be blank"));
        }

        if (!PasswordPolicy.isSatisfiedBy(command.newPassword())) {
            return Result.failure(ApplicationError.validationError("newPassword", PasswordPolicy.POLICY_MESSAGE));
        }

        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(command.token());
        if (optionalToken.isEmpty()) {
            LOGGER.info("Password reset failed: recovery token was not found.");
            return Result.failure(ApplicationError.notFound("PasswordResetToken", "Provided password reset token was not found"));
        }

        PasswordResetToken resetToken = optionalToken.get();
        if (resetToken.isUsed() || resetToken.isExpired()) {
            LOGGER.info("Password reset rejected: recovery token is expired or already used.");
            return Result.failure(ApplicationError.businessRuleViolation(
                    "PASSWORD_RESET_TOKEN_EXPIRED",
                    "Password reset token has expired or has already been used"
            ));
        }

        Optional<User> optionalUser = userRepository.findById(resetToken.getUserId());
        if (optionalUser.isEmpty()) {
            LOGGER.error("Data inconsistency: user referenced by password reset token was not found.");
            return Result.failure(ApplicationError.notFound("User", String.valueOf(resetToken.getUserId().value())));
        }

        User user = optionalUser.get();
        if (user.isDeactivated() || user.isRestricted()) {
            LOGGER.info("Password reset rejected: target account is deactivated or restricted.");
            return Result.failure(ApplicationError.forbidden("Account is deactivated or restricted and cannot reset its password"));
        }

        PasswordHash newHash = passwordHashingService.hash(command.newPassword());
        user.updatePassword(newHash);
        userRepository.save(user);

        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        LOGGER.info("User password reset completed successfully.");
        return Result.success("Password has been reset successfully.");
    }
}
