
package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.internal.services.NotificationPreferencesService;
import com.jouletracker.platform.alerts.interfaces.rest.resources.NotificationPreferencesResource;
import com.jouletracker.platform.alerts.interfaces.rest.resources.UpdateNotificationPreferencesResource;
import com.jouletracker.platform.alerts.interfaces.rest.transform.NotificationPreferencesResourceFromEntityAssembler;
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
 * US-38: REST controller for authenticated user
 * notification preferences.
 */
@RestController
@RequestMapping("/api/v1/notification-preferences")
@Tag(
        name = "Notification Preferences",
        description = "User notification channel preferences"
)
public class NotificationPreferencesController {

    private final NotificationPreferencesService
            preferencesService;

    private final IamContextFacade iamContextFacade;

    public NotificationPreferencesController(
            NotificationPreferencesService preferencesService,
            IamContextFacade iamContextFacade) {

        this.preferencesService = preferencesService;
        this.iamContextFacade = iamContextFacade;
    }

    @GetMapping
    @Operation(
            summary = "Get notification preferences",
            description = "Returns notification preferences for the authenticated user."
    )
    public ResponseEntity<NotificationPreferencesResource>
    getPreferences() {

        Long userId = resolveAuthenticatedUserId();

        if (userId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        var preferences =
                preferencesService.getPreferences(userId);

        var resource =
                NotificationPreferencesResourceFromEntityAssembler
                        .toResourceFromEntity(preferences);

        return ResponseEntity.ok(resource);
    }

    @PutMapping
    @Operation(
            summary = "Update notification preferences",
            description = "Persists email and WhatsApp preferences for the authenticated user."
    )
    public ResponseEntity<NotificationPreferencesResource>
    updatePreferences(
            @Valid @RequestBody
            UpdateNotificationPreferencesResource resource) {

        Long userId = resolveAuthenticatedUserId();

        if (userId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        var preferences =
                preferencesService.updatePreferences(
                        userId,
                        resource.emailEnabled(),
                        resource.whatsappEnabled()
                );

        var response =
                NotificationPreferencesResourceFromEntityAssembler
                        .toResourceFromEntity(preferences);

        return ResponseEntity.ok(response);
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
