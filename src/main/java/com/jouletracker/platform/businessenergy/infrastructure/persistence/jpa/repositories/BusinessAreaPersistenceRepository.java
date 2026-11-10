package com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.entities.BusinessAreaPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for business area persistence.
 */
@Repository
public interface BusinessAreaPersistenceRepository
        extends JpaRepository<BusinessAreaPersistenceEntity, Long> {

    /**
     * Retrieves all business areas belonging to a specific user.
     */
    List<BusinessAreaPersistenceEntity> findAllByUserId(Long userId);

    /**
     * Checks whether a business area with the same name
     * already exists for the specified user.
     */
    boolean existsByUserIdAndName(Long userId, String name);
}
