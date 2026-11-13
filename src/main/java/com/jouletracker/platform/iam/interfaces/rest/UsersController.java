package com.jouletracker.platform.iam.interfaces.rest;

import com.jouletracker.platform.iam.application.queryservices.UserQueryService;
import com.jouletracker.platform.iam.domain.model.queries.GetAllUsersQuery;
import com.jouletracker.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.jouletracker.platform.iam.interfaces.rest.resources.UserResource;
import com.jouletracker.platform.iam.interfaces.rest.transform.UserResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing user query endpoints.
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Endpoints for querying registered user profiles")
@NullMarked
public class UsersController {

    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    /**
     * Retrieves all registered users.
     *
     * @return 200 OK with list of UserResource
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all registered platform users.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var resources = userQueryService.handle(new GetAllUsersQuery()).stream()
                .map(UserResourceAssembler::toResourceFromUserResult)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a specific user profile by their UUID.
     *
     * @param userId the user's UUID
     * @return 200 OK with UserResource, or 404 Not Found
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves a registered user profile by its unique UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResource> getUserById(
            @Parameter(description = "Unique UUID of the user", required = true)
            @PathVariable UUID userId) {
        return userQueryService.handle(new GetUserByIdQuery(userId))
                .map(UserResourceAssembler::toResourceFromUserResult)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
