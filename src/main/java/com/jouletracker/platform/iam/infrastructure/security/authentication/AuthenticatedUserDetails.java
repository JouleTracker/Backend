package com.jouletracker.platform.iam.infrastructure.security.authentication;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Security {@link UserDetails} adapter wrapping the IAM {@link User} domain aggregate.
 *
 * <p>Reflects email verification, soft-deactivation, and moderation bans/suspensions so
 * Spring Security immediately blocks inactive or restricted accounts.</p>
 */
@NullMarked
public class AuthenticatedUserDetails implements UserDetails {

    private final @Nullable UUID id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final UserStatus status;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUserDetails(User user) {
        this.id = user.getId().value();
        this.username = user.getEmail().value();
        this.password = user.getPasswordHash().value();
        this.status = user.getStatus();
        this.enabled = user.canAuthenticate();
        this.accountNonLocked = !user.isRestricted();
        this.authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.name()))
                .toList();
    }

    public @Nullable UUID getId() {
        return id;
    }

    public UserStatus getStatus() {
        return status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
