package com.jouletracker.platform.devices.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain entity representing a recurring domestic appliance profile with standard consumption benchmark.
 */
@Getter
@Setter
public class ApplianceProfile {

    private Long id;
    private String name;
    private Double recommendedDailyKwh;
    private String typicalPowerWatts;
    private String icon;

    public ApplianceProfile(Long id, String name, Double recommendedDailyKwh, String typicalPowerWatts, String icon) {
        this.id = id;
        this.name = name;
        this.recommendedDailyKwh = recommendedDailyKwh != null ? recommendedDailyKwh : 0.0;
        this.typicalPowerWatts = typicalPowerWatts != null ? typicalPowerWatts : "";
        this.icon = icon != null ? icon : "bolt";
    }

    public ApplianceProfile(String name, Double recommendedDailyKwh, String typicalPowerWatts, String icon) {
        this(null, name, recommendedDailyKwh, typicalPowerWatts, icon);
    }
}
