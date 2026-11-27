
package com.jouletracker.platform.businessenergy.interfaces.rest.transform;

import com.jouletracker.platform.businessenergy.domain.model.commands.UpdateBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.UpdateBusinessAreaResource;

/**
 * Assembler to translate an UpdateBusinessAreaResource
 * into UpdateBusinessAreaCommand (US-31 Escenario 3).
 *
 * US-32: Supports updating shared notification recipients.
 */
public final class UpdateBusinessAreaCommandFromResourceAssembler {

    private UpdateBusinessAreaCommandFromResourceAssembler() {
    }

    public static UpdateBusinessAreaCommand toCommandFromResource(
            Long id,
            Long userId,
            UpdateBusinessAreaResource resource) {

        return new UpdateBusinessAreaCommand(
                id,
                userId,
                resource.name(),
                resource.floor(),
                resource.description(),
                resource.icon(),
                resource.notificationRecipients()
        );
    }
}
