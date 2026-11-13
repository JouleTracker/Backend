package com.jouletracker.platform.iam.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.UserEntity;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.mappers.UserPersistenceMapper;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.repositories.SpringDataJpaUserRepository;
import com.jouletracker.platform.shared.infrastructure.events.DomainEventPublisher;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter implementing the pure domain {@link UserRepository} contract
 * using Spring Data JPA and Hibernate.
 */
@Repository
@NullMarked
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataJpaUserRepository jpaRepository;
    private final UserPersistenceMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    public UserRepositoryAdapter(
            SpringDataJpaUserRepository jpaRepository,
            UserPersistenceMapper mapper,
            DomainEventPublisher domainEventPublisher) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        if (id.value() == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(EmailAddress email) {
        return jpaRepository.findByEmail(email.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(EmailAddress email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entityToSave;
        if (user.getId().value() != null) {
            entityToSave = jpaRepository.findById(user.getId().value())
                    .orElseGet(() -> mapper.toEntity(user));
            mapper.updateEntity(entityToSave, user);
        } else {
            entityToSave = mapper.toEntity(user);
        }

        UserEntity savedEntity = jpaRepository.save(entityToSave);
        User savedUser = mapper.toDomain(savedEntity);

        if (user.getId().value() == null && savedUser.getId().value() != null) {
            user.assignId(savedUser.getId());
        }

        domainEventPublisher.publishAndClearEvents(user);

        return savedUser;
    }
}
