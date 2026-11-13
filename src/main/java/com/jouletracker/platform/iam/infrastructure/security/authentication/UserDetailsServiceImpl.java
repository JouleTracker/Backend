package com.jouletracker.platform.iam.infrastructure.security.authentication;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter implementing Spring Security's {@link UserDetailsService} by querying
 * the domain {@link UserRepository}.
 */
@Service
@NullMarked
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            var emailAddress = EmailAddress.of(username);
            return userRepository.findByEmail(emailAddress)
                    .map(AuthenticatedUserDetails::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("Invalid email format: " + username, ex);
        }
    }
}
