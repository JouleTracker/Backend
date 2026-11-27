
package com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.assemblers.BusinessAreaPersistenceAssembler;
import com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.repositories.BusinessAreaPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter implementing the BusinessArea domain repository.
 *
 * US-32: Supports transactional persistence and retrieval
 * of shared notification recipients.
 */
@Repository
@Transactional(readOnly = true)
public class BusinessAreaRepositoryImpl implements BusinessAreaRepository {

    private final BusinessAreaPersistenceRepository
            businessAreaPersistenceRepository;

    public BusinessAreaRepositoryImpl(
            BusinessAreaPersistenceRepository businessAreaPersistenceRepository) {

        this.businessAreaPersistenceRepository =
                businessAreaPersistenceRepository;
    }

    @Override
    public List<BusinessArea> findAllByUserId(Long userId) {

        return businessAreaPersistenceRepository
                .findAllByUserId(userId)
                .stream()
                .map(BusinessAreaPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<BusinessArea> findById(Long id) {

        return businessAreaPersistenceRepository
                .findById(id)
                .map(BusinessAreaPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    @Transactional
    public BusinessArea save(BusinessArea businessArea) {

        var entity =
                BusinessAreaPersistenceAssembler
                        .toPersistenceFromDomain(businessArea);

        var savedEntity =
                businessAreaPersistenceRepository.save(entity);

        return BusinessAreaPersistenceAssembler
                .toDomainFromPersistence(savedEntity);
    }

    @Override
    public boolean existsByUserIdAndName(
            Long userId,
            String name) {

        return businessAreaPersistenceRepository
                .existsByUserIdAndName(userId, name);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        businessAreaPersistenceRepository.deleteById(id);
    }
}
