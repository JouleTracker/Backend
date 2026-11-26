package com.jouletracker.platform.iam.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.iam.domain.model.entities.PasswordResetToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.PasswordResetTokenRepository;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenEntity;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.mappers.PasswordResetTokenPersistenceMapper;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.repositories.SpringDataJpaPasswordResetTokenRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository adapter implementing {@link PasswordResetTokenRepository} via Spring Data JPA.
 */
@Repository
@NullMarked
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final SpringDataJpaPasswordResetTokenRepository jpaRepository;
    private final PasswordResetTokenPersistenceMapper mapper;

    public PasswordResetTokenRepositoryAdapter(
            SpringDataJpaPasswordResetTokenRepository jpaRepository,
            PasswordResetTokenPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(mapper.hash(token))
                .map(entity -> new PasswordResetToken(
                        entity.getId(),
                        token,
                        UserId.of(entity.getUserId()),
                        entity.getExpiresAt(),
                        entity.getUsedAt()
                ));
    }

    @Override
    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entityToSave;
        if (token.getId() != null) {
            entityToSave = jpaRepository.findById(token.getId())
                    .orElseGet(() -> mapper.toEntity(token));
            mapper.updateEntity(entityToSave, token);
        } else {
            entityToSave = mapper.toEntity(token);
        }

        PasswordResetTokenEntity savedEntity = jpaRepository.save(entityToSave);
        PasswordResetToken savedDomainToken = mapper.toDomain(savedEntity);

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
