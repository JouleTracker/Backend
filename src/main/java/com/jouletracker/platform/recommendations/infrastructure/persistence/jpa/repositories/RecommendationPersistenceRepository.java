package com.jouletracker.platform.recommendations.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.recommendations.infrastructure.persistence.jpa.entities.RecommendationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecommendationPersistenceRepository extends JpaRepository<RecommendationPersistenceEntity, Long> {
    List<RecommendationPersistenceEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
