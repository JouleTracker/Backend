package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.SignUpCommand;
import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.iam.application.usecases.SignUpUseCase;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.repositories.EmailVerificationTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.EmailService;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import com.jouletracker.platform.iam.infrastructure.configuration.VerificationTokenProperties;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Implementation of the {@link SignUpUseCase} orchestrating user registration.
 */
@Service
@NullMarked
public class SignUpUseCaseImpl implements SignUpUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignUpUseCaseImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,64}$"
    );

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final VerificationTokenProperties verificationTokenProperties;

    @org.springframework.beans.factory.annotation.Autowired
    public SignUpUseCaseImpl(
            UserRepository userRepository,
            PasswordHashingService passwordHashingService,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService,
            VerificationTokenProperties verificationTokenProperties) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
        this.verificationTokenProperties = Objects.requireNonNull(verificationTokenProperties, "VerificationTokenProperties must not be null");
    }

    public SignUpUseCaseImpl(
            UserRepository userRepository,
            PasswordHashingService passwordHashingService,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService) {
        this(userRepository, passwordHashingService, emailVerificationTokenRepository, emailService, new VerificationTokenProperties());
    }

    @Override
    @Transactional
    public Result<UserResult, ApplicationError> handle(SignUpCommand command) {
        EmailAddress emailAddress;
        try {
            emailAddress = EmailAddress.of(command.email());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("email", ex.getMessage()));
        }

        if (userRepository.existsByEmail(emailAddress)) {
            LOGGER.info("Registration attempt rejected: email address is already in use.");
            return Result.failure(ApplicationError.conflict("User", "Email address is already in use"));
        }

        if (command.rawPassword() == null || !STRONG_PASSWORD.matcher(command.rawPassword()).matches()) {
            return Result.failure(ApplicationError.validationError(
                    "password", "Password must be 8-64 characters and include uppercase, lowercase, digit, and special character"
            ));
        }

        PasswordHash passwordHash = passwordHashingService.hash(command.rawPassword());

        User newUser = User.create(
                emailAddress,
                passwordHash,
                command.firstName(),
                command.lastName(),
                command.roles()
        );

        User savedUser = userRepository.save(newUser);

        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String tokenString = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        EmailVerificationToken verificationToken = EmailVerificationToken.create(
                savedUser.getId(),
                tokenString,
                verificationTokenProperties.getValidityDuration()
        );

        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getFullName(),
                verificationToken.getToken()
        );

        LOGGER.info("User successfully registered and verification email dispatched.");
        return Result.success(UserResult.fromUser(savedUser));
    }
}
