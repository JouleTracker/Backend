package com.jouletracker.platform.iam.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.EmailVerificationTokenRepository;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.EmailVerificationTokenEntity;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.mappers.EmailVerificationTokenPersistenceMapper;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.repositories.SpringDataJpaEmailVerificationTokenRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository adapter implementing the pure domain {@link EmailVerificationTokenRepository}
 * using Spring Data JPA.
 */
@Repository
@NullMarked
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {

    private final SpringDataJpaEmailVerificationTokenRepository jpaRepository;
    private final EmailVerificationTokenPersistenceMapper mapper;

    public EmailVerificationTokenRepositoryAdapter(
            SpringDataJpaEmailVerificationTokenRepository jpaRepository,
            EmailVerificationTokenPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailVerificationToken> findByToken(String token) {
        return jpaRepository.findByToken(mapper.hash(token)).map(entity -> new EmailVerificationToken(
                entity.getId(), token, UserId.of(entity.getUserId()), entity.getExpiresAt(), entity.getUsedAt()
        ));
    }

    @Override
    @Transactional
    public EmailVerificationToken save(EmailVerificationToken token) {
        EmailVerificationTokenEntity entityToSave;
        if (token.getId() != null) {
            entityToSave = jpaRepository.findById(token.getId())
                    .orElseGet(() -> mapper.toEntity(token));
            mapper.updateEntity(entityToSave, token);
        } else {
            entityToSave = mapper.toEntity(token);
        }

        EmailVerificationTokenEntity savedEntity = jpaRepository.save(entityToSave);
        EmailVerificationToken savedDomainToken = mapper.toDomain(savedEntity);

        if (token.getId() == null && savedDomainToken.getId() != null) {
            token.assignId(savedDomainToken.getId());
        }

        return savedDomainToken;
    }

    @Override
    @Transactional
    public void deleteByUserId(UserId userId) {
        if (userId.value() != null) {
            jpaRepository.deleteByUserId(userId.value());
        }
    }
}
