package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.VerifyEmailCommand;
import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.iam.application.usecases.VerifyEmailUseCase;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.repositories.EmailVerificationTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of the {@link VerifyEmailUseCase} orchestrating email verification token validation.
 */
@Service
@NullMarked
public class VerifyEmailUseCaseImpl implements VerifyEmailUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyEmailUseCaseImpl.class);

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public VerifyEmailUseCaseImpl(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Result<UserResult, ApplicationError> handle(VerifyEmailCommand command) {
        if (command.token().isBlank()) {
            return Result.failure(ApplicationError.validationError("token", "Verification token must not be blank"));
        }

        Optional<EmailVerificationToken> optionalToken = tokenRepository.findByToken(command.token());
        if (optionalToken.isEmpty()) {
            LOGGER.info("Verification failed: token string not found in repository.");
            return Result.failure(ApplicationError.notFound("VerificationToken", "Provided token was not found"));
        }

        EmailVerificationToken token = optionalToken.get();

        if (token.isUsed() || token.isExpired()) {
            LOGGER.info("Verification rejected: token is expired or already used.");
            return Result.failure(ApplicationError.businessRuleViolation(
                    "TOKEN_EXPIRED",
                    "Verification token has expired or has already been used"
            ));
        }

        token.markAsUsed();
        tokenRepository.save(token);

        Optional<User> optionalUser = userRepository.findById(token.getUserId());
        if (optionalUser.isEmpty()) {
            LOGGER.error("Data inconsistency: a user associated with a verification token was not found.");
            return Result.failure(ApplicationError.notFound(
                    "User",
                    String.valueOf(token.getUserId().value())
            ));
        }

        User user = optionalUser.get();
        user.confirmEmail();
        User savedUser = userRepository.save(user);

        LOGGER.info("User email address successfully verified.");
        return Result.success(UserResult.fromUser(savedUser));
    }
}
