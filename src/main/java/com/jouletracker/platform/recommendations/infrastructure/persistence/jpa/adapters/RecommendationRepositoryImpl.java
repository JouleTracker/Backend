package com.jouletracker.platform.recommendations.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.recommendations.domain.model.aggregates.Recommendation;
import com.jouletracker.platform.recommendations.domain.repositories.RecommendationRepository;
import com.jouletracker.platform.recommendations.infrastructure.persistence.jpa.assemblers.RecommendationPersistenceAssembler;
import com.jouletracker.platform.recommendations.infrastructure.persistence.jpa.repositories.RecommendationPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class RecommendationRepositoryImpl implements RecommendationRepository {
    private final RecommendationPersistenceRepository persistenceRepository;

    public RecommendationRepositoryImpl(RecommendationPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Recommendation save(Recommendation recommendation) {
        var saved = persistenceRepository.save(RecommendationPersistenceAssembler.toPersistenceFromDomain(recommendation));
        return RecommendationPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public Optional<Recommendation> findById(Long id) {
        return persistenceRepository.findById(id).map(RecommendationPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Recommendation> findAllByUserId(Long userId) {
        return persistenceRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(RecommendationPersistenceAssembler::toDomainFromPersistence).toList();
    }
}
