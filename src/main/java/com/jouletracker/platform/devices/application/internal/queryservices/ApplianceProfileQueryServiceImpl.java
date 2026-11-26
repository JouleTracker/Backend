package com.jouletracker.platform.devices.application.internal.queryservices;

import com.jouletracker.platform.devices.application.queryservices.ApplianceProfileQueryService;
import com.jouletracker.platform.devices.domain.model.entities.ApplianceProfile;
import com.jouletracker.platform.devices.domain.model.queries.GetAllApplianceProfilesQuery;
import com.jouletracker.platform.devices.domain.repositories.ApplianceProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ApplianceProfileQueryServiceImpl implements ApplianceProfileQueryService {

    private final ApplianceProfileRepository applianceProfileRepository;

    public ApplianceProfileQueryServiceImpl(ApplianceProfileRepository applianceProfileRepository) {
        this.applianceProfileRepository = applianceProfileRepository;
    }

    @Override
    public List<ApplianceProfile> handle(GetAllApplianceProfilesQuery query) {
        return applianceProfileRepository.findAll();
    }
}
