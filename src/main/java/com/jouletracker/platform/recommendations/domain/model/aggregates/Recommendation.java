package com.jouletracker.platform.recommendations.domain.model.aggregates;

import com.jouletracker.platform.recommendations.domain.model.commands.CreateRecommendationCommand;
import com.jouletracker.platform.recommendations.domain.model.events.RecommendationCreatedEvent;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Recommendation extends AbstractDomainAggregateRoot<Recommendation> {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Double estimatedSavingKwh;
    private Double estimatedSavingAmount;
    private String category;
    private boolean completed;
    private LocalDateTime createdAt;

    public Recommendation() { }

    public Recommendation(CreateRecommendationCommand command) {
        this(null, command.userId(), command.title().trim(), command.description().trim(),
                command.estimatedSavingKwh(), command.estimatedSavingAmount(), command.category().trim(),
                false, LocalDateTime.now());
    }

    public Recommendation(Long id, Long userId, String title, String description,
                          Double estimatedSavingKwh, Double estimatedSavingAmount,
                          String category, boolean completed, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.estimatedSavingKwh = estimatedSavingKwh;
        this.estimatedSavingAmount = estimatedSavingAmount;
        this.category = category;
        this.completed = completed;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void markAsCreated() {
        registerDomainEvent(new RecommendationCreatedEvent(id, userId, title, createdAt));
    }
}
