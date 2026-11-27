package com.jouletracker.platform.recommendations.domain.model.commands;

public record CreateRecommendationCommand(Long userId, String title, String description,
                                          Double estimatedSavingKwh, Double estimatedSavingAmount,
                                          String category) { }
