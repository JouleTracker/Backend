package com.jouletracker.platform.devices.application.internal.commandservices;

import com.jouletracker.platform.devices.application.commandservices.ApplianceProfileCommandService;
import com.jouletracker.platform.devices.domain.model.commands.SeedApplianceProfilesCommand;
import com.jouletracker.platform.devices.domain.model.entities.ApplianceProfile;
import com.jouletracker.platform.devices.domain.repositories.ApplianceProfileRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApplianceProfileCommandServiceImpl implements ApplianceProfileCommandService {

    private final ApplianceProfileRepository applianceProfileRepository;

    public ApplianceProfileCommandServiceImpl(ApplianceProfileRepository applianceProfileRepository) {
        this.applianceProfileRepository = applianceProfileRepository;
    }

    @Override
    public Result<Void, ApplicationError> handle(SeedApplianceProfilesCommand command) {
        if (applianceProfileRepository.count() == 0) {
            List<ApplianceProfile> defaults = List.of(
                    new ApplianceProfile("Refrigeradora Inverter", 1.80, "150W", "kitchen"),
                    new ApplianceProfile("Aire Acondicionado Split", 4.50, "1200W", "mode_fan"),
                    new ApplianceProfile("Terma Eléctrica", 3.20, "1500W", "water_heater"),
                    new ApplianceProfile("Lavadora de Ropa", 0.90, "500W", "local_laundry_service"),
                    new ApplianceProfile("Televisor / Centro Entretenimiento", 0.70, "120W", "tv"),
                    new ApplianceProfile("Computadora / Home Office", 1.20, "250W", "computer")
            );

            for (ApplianceProfile profile : defaults) {
                applianceProfileRepository.save(profile);
            }
        }
        return Result.success(null);
    }
}
