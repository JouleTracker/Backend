package com.jouletracker.platform.businessenergy.interfaces.rest.transform;

import com.jouletracker.platform.businessenergy.domain.model.commands.CreateBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.CreateBusinessAreaResource;

/**
 * Assembler to translate a REST request resource
 * into a CreateBusinessAreaCommand.
 */
public final class CreateBusinessAreaCommandFromResourceAssembler {

    private CreateBusinessAreaCommandFromResourceAssembler() {
    }

    public static CreateBusinessAreaCommand toCommandFromResource(
            CreateBusinessAreaResource resource,
            Long userId) {

        return new CreateBusinessAreaCommand(
                userId,
                resource.name(),
                resource.description()
        );
    }
}
