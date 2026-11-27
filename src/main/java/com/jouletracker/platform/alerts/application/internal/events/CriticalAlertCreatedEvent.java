
package com.jouletracker.platform.alerts.application.internal.events;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.devices.interfaces.acl.SensorThresholdSnapshot;

/**
 * TS-19: Event published when a new critical alert is persisted.
 *
 * Notification delivery must occur after the transaction commits.
 */
public record CriticalAlertCreatedEvent(
        Alert alert,
        SensorThresholdSnapshot snapshot
) {
}
