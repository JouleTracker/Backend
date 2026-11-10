package com.jouletracker.platform.businessenergy.interfaces.rest;

import com.jouletracker.platform.businessenergy.application.commandservices.BusinessAreaCommandService;
import com.jouletracker.platform.businessenergy.application.queryservices.BusinessAreaQueryService;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetAllBusinessAreasQuery;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.CreateBusinessAreaResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.BusinessAreaResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.transform.CreateBusinessAreaCommandFromResourceAssembler;
import com.jouletracker.platform.businessenergy.interfaces.rest.transform.BusinessAreaResourceFromEntityAssembler;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for business area management (TS-16).
 */
@RestController
@RequestMapping(
        value = "/api/v1/business-areas",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Business Areas",
        description = "Business area management endpoints"
)
public class BusinessAreasController {

    private final BusinessAreaCommandService businessAreaCommandService;
    private final BusinessAreaQueryService businessAreaQueryService;

    public BusinessAreasController(
            BusinessAreaCommandService businessAreaCommandService,
            BusinessAreaQueryService businessAreaQueryService) {
        this.businessAreaCommandService = businessAreaCommandService;
        this.businessAreaQueryService = businessAreaQueryService;
    }

    /**
     * Registers a physical area belonging to a business.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Register business area",
            description = "Creates a new physical area for business energy management."
    )
    public ResponseEntity<?> createBusinessArea(
            @Valid @RequestBody CreateBusinessAreaResource resource,
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = userId != null ? userId : resource.userId();

        var command =
                CreateBusinessAreaCommandFromResourceAssembler
                        .toCommandFromResource(resource, resolvedUserId);

        var result = businessAreaCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                BusinessAreaResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieves all business areas belonging to a user.
     */
    @GetMapping
    @Operation(
            summary = "Get business areas",
            description = "Retrieves all physical areas registered by a user."
    )
    public ResponseEntity<List<BusinessAreaResource>> getBusinessAreas(
            @RequestParam Long userId) {

        var query = new GetAllBusinessAreasQuery(userId);

        var businessAreas = businessAreaQueryService.handle(query);

        var resources = businessAreas.stream()
                .map(BusinessAreaResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }
}
