package com.jouletracker.platform.recommendations.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "recommendations")
public class RecommendationPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, length = 1000)
    private String description;
    @Column(name = "estimated_saving_kwh")
    private Double estimatedSavingKwh;
    @Column(name = "estimated_saving_amount")
    private Double estimatedSavingAmount;
    @Column(nullable = false, length = 30)
    private String category;
    @Column(nullable = false)
    private boolean completed;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
