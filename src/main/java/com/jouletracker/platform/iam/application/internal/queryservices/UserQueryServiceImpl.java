package com.jouletracker.platform.iam.application.internal.queryservices;

import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.iam.application.queryservices.UserQueryService;
import com.jouletracker.platform.iam.domain.model.queries.GetAllUsersQuery;
import com.jouletracker.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link UserQueryService} for querying user profiles.
 */
@Service
@NullMarked
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResult> handle(GetAllUsersQuery query) {
        return userRepository.findAll().stream()
                .map(UserResult::fromUser)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResult> handle(GetUserByIdQuery query) {
        if (query.userId() == null) {
            return Optional.empty();
        }
        return userRepository.findById(UserId.of(query.userId()))
                .map(UserResult::fromUser);
    }
}
