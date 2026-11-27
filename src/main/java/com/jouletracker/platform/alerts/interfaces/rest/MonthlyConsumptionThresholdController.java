
package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.internal.services.MonthlyConsumptionThresholdService;
import com.jouletracker.platform.alerts.application.internal.services.MonthlyConsumptionThresholdService.MonthlyConsumptionStatus;
import com.jouletracker.platform.alerts.interfaces.rest.resources.MonthlyConsumptionThresholdResource;
import com.jouletracker.platform.alerts.interfaces.rest.resources.UpdateMonthlyConsumptionThresholdResource;
import com.jouletracker.platform.alerts.interfaces.rest.transform.MonthlyConsumptionThresholdResourceFromEntityAssembler;
import com.jouletracker.platform.iam.infrastructure.security.authentication.AuthenticatedUserDetails;
import com.jouletracker.platform.iam.interfaces.acl.IamContextFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * US-26: Authenticated global monthly consumption
 * threshold configuration and consumption status.
 */
@RestController
@RequestMapping("/api/v1/monthly-consumption-threshold")
@Tag(
        name = "Monthly Consumption Threshold",
        description = "Global monthly energy consumption configuration"
)
public class MonthlyConsumptionThresholdController {

    private final MonthlyConsumptionThresholdService thresholdService;
    private final IamContextFacade iamContextFacade;

    public MonthlyConsumptionThresholdController(
            MonthlyConsumptionThresholdService thresholdService,
            IamContextFacade iamContextFacade) {

        this.thresholdService = thresholdService;
        this.iamContextFacade = iamContextFacade;
    }

    @GetMapping
    @Operation(summary = "Get monthly consumption threshold")
    public ResponseEntity<MonthlyConsumptionThresholdResource>
    getThreshold() {

        Long userId = resolveAuthenticatedUserId();

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        var resource = thresholdService.getThreshold(userId)
                .map(MonthlyConsumptionThresholdResourceFromEntityAssembler
                        ::toResourceFromEntity)
                .orElseGet(() ->
                        MonthlyConsumptionThresholdResourceFromEntityAssembler
                                .toUnconfiguredResource(userId)
                );

        return ResponseEntity.ok(resource);
    }

    @PutMapping
    @Operation(summary = "Update monthly consumption threshold")
    public ResponseEntity<MonthlyConsumptionThresholdResource>
    updateThreshold(
            @Valid @RequestBody
            UpdateMonthlyConsumptionThresholdResource resource) {

        Long userId = resolveAuthenticatedUserId();

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        var threshold = thresholdService.updateThreshold(
                userId,
                resource.monthlyLimitKwh()
        );

        return ResponseEntity.ok(
                MonthlyConsumptionThresholdResourceFromEntityAssembler
                        .toResourceFromEntity(threshold)
        );
    }

    @GetMapping("/status")
    @Operation(
            summary = "Get monthly consumption status",
            description = "Returns the accumulated monthly consumption, configured limit, remaining energy and threshold status."
    )
    public ResponseEntity<MonthlyConsumptionStatus>
    getConsumptionStatus() {

        Long userId = resolveAuthenticatedUserId();

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(
                thresholdService.getConsumptionStatus(userId)
        );
    }

    private Long resolveAuthenticatedUserId() {

        var authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return null;
        }

        if (!(authentication.getPrincipal()
                instanceof AuthenticatedUserDetails user)) {
            return null;
        }

        Long userId = user.getId();

        if (userId == null
                || userId <= 0
                || !iamContextFacade.existsUserById(userId)) {
            return null;
        }

        return userId;
    }
}
