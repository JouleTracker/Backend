package com.jouletracker.platform.devices.interfaces.rest;

import com.jouletracker.platform.devices.application.queryservices.ApplianceProfileQueryService;
import com.jouletracker.platform.devices.domain.model.queries.GetAllApplianceProfilesQuery;
import com.jouletracker.platform.devices.interfaces.rest.resources.ApplianceProfileResource;
import com.jouletracker.platform.devices.interfaces.rest.transform.ApplianceProfileResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for domestic appliance catalog profiles (US-43).
 */
@RestController
@RequestMapping(value = {"/api/v1/appliance-profiles", "/appliance-profiles"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Appliance Profiles", description = "Standard domestic appliance consumption benchmark catalog")
public class ApplianceProfilesController {

    private final ApplianceProfileQueryService applianceProfileQueryService;

    public ApplianceProfilesController(ApplianceProfileQueryService applianceProfileQueryService) {
        this.applianceProfileQueryService = applianceProfileQueryService;
    }

    /**
     * Retrieves the catalog of standard domestic appliance profiles.
     */
    @GetMapping
    @Operation(summary = "Get all appliance profiles", description = "Retrieves benchmark consumption profiles for domestic appliances.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApplianceProfileResource.class))))
    })
    public ResponseEntity<List<ApplianceProfileResource>> getApplianceProfiles() {
        var query = new GetAllApplianceProfilesQuery();
        var profiles = applianceProfileQueryService.handle(query);
        var resources = profiles.stream()
                .map(ApplianceProfileResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
