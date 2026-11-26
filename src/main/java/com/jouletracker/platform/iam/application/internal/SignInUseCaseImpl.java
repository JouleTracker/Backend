package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.SignInCommand;
import com.jouletracker.platform.iam.application.out.AuthenticatedUserResult;
import com.jouletracker.platform.iam.application.usecases.SignInUseCase;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import com.jouletracker.platform.iam.domain.services.TokenService;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link SignInUseCase} orchestrating credential authentication and JWT issuance.
 *
 * <p>Blocks authentication for unverified, soft-deactivated, suspended, or banned accounts.</p>
 */
@Service
@NullMarked
public class SignInUseCaseImpl implements SignInUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignInUseCaseImpl.class);
    private static final String INVALID_CREDENTIALS_MSG = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final TokenService tokenService;

    public SignInUseCaseImpl(
            UserRepository userRepository,
            PasswordHashingService passwordHashingService,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<AuthenticatedUserResult, ApplicationError> handle(SignInCommand command) {
        EmailAddress emailAddress;
        try {
            emailAddress = EmailAddress.of(command.email());
        } catch (IllegalArgumentException ex) {
            LOGGER.debug("Authentication failed: malformed email string provided.");
            return Result.failure(ApplicationError.unauthorized(INVALID_CREDENTIALS_MSG));
        }

        Optional<User> optionalUser = userRepository.findByEmail(emailAddress);
        if (optionalUser.isEmpty()) {
            LOGGER.info("Authentication failed: account was not found.");
            return Result.failure(ApplicationError.unauthorized(INVALID_CREDENTIALS_MSG));
        }

        User user = optionalUser.get();

        if (!passwordHashingService.matches(command.rawPassword(), user.getPasswordHash())) {
            LOGGER.info("Authentication failed: password mismatch.");
            return Result.failure(ApplicationError.unauthorized(INVALID_CREDENTIALS_MSG));
        }

        if (user.isDeactivated()) {
            LOGGER.info("Authentication rejected: user account has been deactivated.");
            return Result.failure(ApplicationError.forbidden(
                    "Account has been deactivated. Please contact support to reactivate your account."
            ));
        }

        if (user.isRestricted()) {
            LOGGER.info("Authentication rejected: user account is suspended or banned.");
            return Result.failure(ApplicationError.forbidden(
                    "Account has been suspended or banned. Please contact support."
            ));
        }

        if (!user.isEmailVerified()) {
            LOGGER.info("Authentication rejected: account email has not been confirmed.");
            return Result.failure(new ApplicationError(
                    "UNVERIFIED_EMAIL",
                    "Forbidden: Account email has not been verified",
                    "Account email has not been verified. Please check your inbox."
            ));
        }

        String token = tokenService.generateToken(user);
        LOGGER.info("User authenticated successfully. JWT issued.");

        return Result.success(AuthenticatedUserResult.of(token, user));
    }
}
