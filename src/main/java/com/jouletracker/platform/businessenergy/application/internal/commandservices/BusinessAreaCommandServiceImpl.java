package com.jouletracker.platform.businessenergy.application.internal.commandservices;

import com.jouletracker.platform.businessenergy.application.commandservices.BusinessAreaCommandService;
import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.commands.CreateBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementation for business area commands.
 */
@Service
@Transactional
public class BusinessAreaCommandServiceImpl implements BusinessAreaCommandService {

    private final BusinessAreaRepository businessAreaRepository;

    public BusinessAreaCommandServiceImpl(BusinessAreaRepository businessAreaRepository) {
        this.businessAreaRepository = businessAreaRepository;
    }

    @Override
    public Result<BusinessArea, ApplicationError> handle(CreateBusinessAreaCommand command) {

        if (command.userId() == null) {
            return Result.failure(
                    ApplicationError.validationError("userId", "User identifier must be provided")
            );
        }

        if (command.name() == null || command.name().isBlank()) {
            return Result.failure(
                    ApplicationError.validationError("name", "Business area name must not be blank")
            );
        }

        String normalizedName = command.name().trim();

        if (businessAreaRepository.existsByUserIdAndName(command.userId(), normalizedName)) {
            return Result.failure(
                    ApplicationError.conflict("BUSINESS_AREA", "A business area with this name already exists")
            );
        }

        BusinessArea businessArea = new BusinessArea(command);
        BusinessArea savedBusinessArea = businessAreaRepository.save(businessArea);

        return Result.success(savedBusinessArea);
    }
}
