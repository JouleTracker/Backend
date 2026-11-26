package com.jouletracker.platform.users.interfaces.rest;

import com.jouletracker.platform.iam.infrastructure.security.authentication.AuthenticatedUserDetails;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.interfaces.rest.resources.MessageResource;
import com.jouletracker.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import com.jouletracker.platform.users.application.commands.ChangePasswordCommand;
import com.jouletracker.platform.users.application.commands.DeactivateUserCommand;
import com.jouletracker.platform.users.application.commands.UpdateUserProfileCommand;
import com.jouletracker.platform.users.application.commands.UpdateUserStatusCommand;
import com.jouletracker.platform.users.application.usecases.ChangePasswordUseCase;
import com.jouletracker.platform.users.application.usecases.DeactivateUserUseCase;
import com.jouletracker.platform.users.application.usecases.UpdateUserProfileUseCase;
import com.jouletracker.platform.users.application.usecases.UpdateUserStatusUseCase;
import com.jouletracker.platform.users.interfaces.rest.resources.ChangePasswordRequest;
import com.jouletracker.platform.users.interfaces.rest.resources.ManagedUserResource;
import com.jouletracker.platform.users.interfaces.rest.resources.UpdateUserProfileRequest;
import com.jouletracker.platform.users.interfaces.rest.resources.UpdateUserStatusRequest;
import com.jouletracker.platform.users.interfaces.rest.transform.ManagedUserResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the User Management bounded context exposing password updates,
 * account soft-deactivation, and administrative user moderation endpoints.
 */
@RestController("userManagementController")
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Endpoints for user password management, account deactivation, and administrative moderation")
@SecurityRequirement(name = "bearerAuth")
@NullMarked
public class UsersController {

    private final ChangePasswordUseCase changePasswordUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final UpdateUserStatusUseCase updateUserStatusUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;

    public UsersController(
            ChangePasswordUseCase changePasswordUseCase,
            DeactivateUserUseCase deactivateUserUseCase,
            UpdateUserStatusUseCase updateUserStatusUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase) {
        this.changePasswordUseCase = changePasswordUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.updateUserStatusUseCase = updateUserStatusUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
    }

    /**
     * Updates the authenticated user's own personal profile information (TS-12 / US-22).
     *
     * @param id        the target user ID
     * @param request   the updated first name, last name, and optional phone number
     * @param principal the authenticated security principal
     * @return 200 OK with updated {@link ManagedUserResource}, or RFC 9457 error
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's own first name, last name, and optional phone number."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = ManagedUserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid profile data",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden to modify another user's profile",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> updateProfile(
            @Parameter(description = "Unique identifier of the user account", required = true)
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserProfileRequest request,
            @AuthenticationPrincipal @Nullable AuthenticatedUserDetails principal) {
        Long authenticatedUserId = resolveAuthenticatedUserId(principal);
        if (authenticatedUserId == null) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.unauthorized("Full authentication is required to update profile")
            );
        }

        var command = new UpdateUserProfileCommand(
                id,
                authenticatedUserId,
                request.firstName(),
                request.lastName(),
                request.phone()
        );
        var result = updateUserProfileUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ManagedUserResourceAssembler::toResourceFromResult,
                HttpStatus.OK
        );
    }

    /**
     * Changes the authenticated user's own password after verifying the current BCrypt hash (US-25).
     *
     * @param id        the target user UUID
     * @param request   the current and new passwords
     * @param principal the authenticated security principal
     * @return 200 OK with confirmation message, or RFC 9457 error
     */
    @PutMapping(value = "/{id}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Change password",
            description = "Updates the authenticated user's own password after verifying the current password with BCrypt and validating domain password policy."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated successfully",
                    content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Incorrect current password or weak new password",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden to modify another user's password",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> changePassword(
            @Parameter(description = "Unique identifier of the user account", required = true)
            @PathVariable("id") Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal @Nullable AuthenticatedUserDetails principal) {
        Long authenticatedUserId = resolveAuthenticatedUserId(principal);
        if (authenticatedUserId == null) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.unauthorized("Full authentication is required to change password")
            );
        }

        var command = new ChangePasswordCommand(
                id,
                authenticatedUserId,
                request.currentPassword(),
                request.newPassword()
        );
        var result = changePasswordUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MessageResource::new,
                HttpStatus.OK
        );
    }

    /**
     * Soft-deactivates the authenticated user's account, immediately preventing sign-in and API access (US-40).
     *
     * @param id        the target user UUID
     * @param principal the authenticated security principal
     * @return 200 OK with updated managed user resource, or RFC 9457 error
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Deactivate account",
            description = "Soft-deactivates the authenticated user's own account, updating status to INACTIVE and recording the deactivation timestamp."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deactivated successfully",
                    content = @Content(schema = @Schema(implementation = ManagedUserResource.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden to deactivate another user's account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Account is already deactivated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> deactivateAccount(
            @Parameter(description = "Unique identifier of the user account to deactivate", required = true)
            @PathVariable("id") Long id,
            @AuthenticationPrincipal @Nullable AuthenticatedUserDetails principal) {
        Long authenticatedUserId = resolveAuthenticatedUserId(principal);
        if (authenticatedUserId == null) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.unauthorized("Full authentication is required to deactivate an account")
            );
        }

        var command = new DeactivateUserCommand(id, authenticatedUserId);
        var result = deactivateUserUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ManagedUserResourceAssembler::toResourceFromResult,
                HttpStatus.OK
        );
    }

    /**
     * Allows platform administrators (ROLE_ADMIN) to update a user's moderation status (US-49).
     *
     * @param id        the target user UUID
     * @param request   the desired moderation status and optional reason
     * @param principal the authenticated administrator principal
     * @return 200 OK with updated managed user resource, or RFC 9457 error
     */
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user moderation status (Admin only)",
            description = "Allows administrators with ROLE_ADMIN to update a user account status (ACTIVE, SUSPENDED, BANNED, INACTIVE). Self-moderation is forbidden."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User status updated successfully",
                    content = @Content(schema = @Schema(implementation = ManagedUserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN or attempted self-moderation",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Target user not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> updateUserStatus(
            @Parameter(description = "Unique identifier of the target user account", required = true)
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal @Nullable AuthenticatedUserDetails principal) {
        Long adminUserId = resolveAuthenticatedUserId(principal);
        if (adminUserId == null) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.unauthorized("Full authentication is required to moderate user accounts")
            );
        }

        var command = new UpdateUserStatusCommand(id, adminUserId, request.status(), request.reason());
        var result = updateUserStatusUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ManagedUserResourceAssembler::toResourceFromResult,
                HttpStatus.OK
        );
    }

    private @Nullable Long resolveAuthenticatedUserId(@Nullable AuthenticatedUserDetails principal) {
        if (principal != null && principal.getId() != null) {
            return principal.getId();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserDetails details) {
            return details.getId();
        }
        return null;
    }
}
