
package com.jouletracker.platform.monitoringspace.interfaces.rest;

import com.jouletracker.platform.monitoringspace.application.internal.services.HomeConfigurationService;
import com.jouletracker.platform.monitoringspace.interfaces.rest.resources.HomeConfigurationResource;
import com.jouletracker.platform.monitoringspace.interfaces.rest.resources.UpdateHomeConfigurationResource;
import com.jouletracker.platform.monitoringspace.interfaces.rest.transform.HomeConfigurationResourceFromEntityAssembler;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
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
 * US-48: REST API for residential home
 * monitoring space configuration.
 */
@RestController
@RequestMapping("/api/v1/monitoring-spaces/home")
@Tag(
        name = "Home Configuration",
        description = "Home monitoring space configuration management"
)
public class HomeConfigurationController {

    private final HomeConfigurationService homeService;
    private final IamContextFacade iamContextFacade;

    public HomeConfigurationController(
            HomeConfigurationService homeService,
            IamContextFacade iamContextFacade) {

        this.homeService = homeService;
        this.iamContextFacade = iamContextFacade;
    }

    @GetMapping
    @Operation(summary = "Get home configuration")
    public ResponseEntity<HomeConfigurationResource>
    getHomeConfiguration() {

        Long userId = resolveAuthenticatedUserId();

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        if (!isHomeowner()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        var resource = homeService.getConfiguration(userId)
                .map(HomeConfigurationResourceFromEntityAssembler
                        ::toResourceFromEntity)
                .orElseGet(() ->
                        HomeConfigurationResourceFromEntityAssembler
                                .toDefaultResource(userId)
                );

        return ResponseEntity.ok(resource);
    }

    @PutMapping
    @Operation(summary = "Update home configuration")
    public ResponseEntity<HomeConfigurationResource>
    updateHomeConfiguration(
            @Valid @RequestBody
            UpdateHomeConfigurationResource resource) {

        Long userId = resolveAuthenticatedUserId();

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        if (!isHomeowner()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        var configuration = homeService.updateConfiguration(
                userId,
                resource.homeType(),
                resource.occupants(),
                resource.location(),
                resource.tariff()
        );

        return ResponseEntity.ok(
                HomeConfigurationResourceFromEntityAssembler
                        .toResourceFromEntity(configuration)
        );
    }

    private Long resolveAuthenticatedUserId() {

        var authentication = SecurityContextHolder
                .getContext()
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

    private boolean isHomeowner() {

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {
            return false;
        }

        var roles = authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .toList();

        return roles.contains(Role.ROLE_USER.name())
                && !roles.contains(Role.ROLE_BUSINESS.name())
                && !roles.contains(Role.ROLE_ADMIN.name());
    }
}
