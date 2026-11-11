package com.jouletracker.platform.businessenergy.application.internal.commandservices;

import com.jouletracker.platform.businessenergy.application.commandservices.BusinessAreaCommandService;
import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.commands.CreateBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.domain.model.commands.DeleteBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.domain.model.commands.UpdateBusinessAreaCommand;
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

    @Override
    public Result<BusinessArea, ApplicationError> handle(UpdateBusinessAreaCommand command) {

        if (command.id() == null) {
            return Result.failure(
                    ApplicationError.validationError("id", "Business area identifier must be provided")
            );
        }

        var areaOpt = businessAreaRepository.findById(command.id());
        if (areaOpt.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("BUSINESS_AREA", String.valueOf(command.id()))
            );
        }

        BusinessArea area = areaOpt.get();

        if (command.name() != null && !command.name().isBlank()) {
            String normalizedName = command.name().trim();
            if (!normalizedName.equalsIgnoreCase(area.getName()) &&
                    businessAreaRepository.existsByUserIdAndName(area.getUserId(), normalizedName)) {
                return Result.failure(
                        ApplicationError.conflict("BUSINESS_AREA", "A business area with this name already exists")
                );
            }
        }

        area.updateDetails(command.name(), command.floor(), command.description(), command.icon());
        BusinessArea updatedArea = businessAreaRepository.save(area);

        return Result.success(updatedArea);
    }

    @Override
    public Result<Void, ApplicationError> handle(DeleteBusinessAreaCommand command) {

        if (command.id() == null) {
            return Result.failure(
                    ApplicationError.validationError("id", "Business area identifier must be provided")
            );
        }

        var areaOpt = businessAreaRepository.findById(command.id());
        if (areaOpt.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("BUSINESS_AREA", String.valueOf(command.id()))
            );
        }

        businessAreaRepository.deleteById(command.id());
        return Result.success(null);
    }
}
