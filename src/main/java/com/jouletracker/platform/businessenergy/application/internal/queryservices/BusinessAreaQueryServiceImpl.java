package com.jouletracker.platform.businessenergy.application.internal.queryservices;

import com.jouletracker.platform.businessenergy.application.queryservices.BusinessAreaQueryService;
import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetAllBusinessAreasQuery;
import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        if (query.userId() == null) {
            throw new IllegalArgumentException("User identifier must be provided");
        }

        return businessAreaRepository.findAllByUserId(query.userId());
    }
}
