package com.jouletracker.platform.businessenergy.application.internal.queryservices;

import com.jouletracker.platform.businessenergy.application.queryservices.BusinessAreaQueryService;
import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetAllBusinessAreasQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaByIdQuery;
import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service implementation for business area queries.
 */
@Service
@Transactional(readOnly = true)
public class BusinessAreaQueryServiceImpl implements BusinessAreaQueryService {

    private final BusinessAreaRepository businessAreaRepository;

    public BusinessAreaQueryServiceImpl(BusinessAreaRepository businessAreaRepository) {
        this.businessAreaRepository = businessAreaRepository;
    }

    @Override
    public List<BusinessArea> handle(GetAllBusinessAreasQuery query) {
        Long resolvedUserId = query.userId() != null ? query.userId() : 1L;
        return businessAreaRepository.findAllByUserId(resolvedUserId);
    }

    @Override
    public Optional<BusinessArea> handle(GetBusinessAreaByIdQuery query) {
        if (query.id() == null) {
            return Optional.empty();
        }
        return businessAreaRepository.findById(query.id());
    }
}
