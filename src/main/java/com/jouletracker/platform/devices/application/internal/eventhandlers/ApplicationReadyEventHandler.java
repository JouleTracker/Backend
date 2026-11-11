package com.jouletracker.platform.devices.application.internal.eventhandlers;

import com.jouletracker.platform.devices.application.commandservices.ApplianceProfileCommandService;
import com.jouletracker.platform.devices.domain.model.commands.SeedApplianceProfilesCommand;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event handler that seeds standard appliance consumption profiles when application boots.
 */
@Component
public class ApplicationReadyEventHandler {

    private final ApplianceProfileCommandService applianceProfileCommandService;

    public ApplicationReadyEventHandler(ApplianceProfileCommandService applianceProfileCommandService) {
        this.applianceProfileCommandService = applianceProfileCommandService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void on(ApplicationReadyEvent event) {
        applianceProfileCommandService.handle(new SeedApplianceProfilesCommand());
    }
}
