package com.pizzamaestro.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom UserDetails implementation that includes user ID.
 * This avoids N+1 query problem when getUserId() is needed in controllers.
 */
@Getter
public class UserPrincipal extends User {
    
    private final String userId;
    
    public UserPrincipal(String userId, String email, String password, boolean enabled,
                         boolean accountNonExpired, boolean credentialsNonExpired,
                         boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(email, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
    }
    
    /**
     * Get user email (alias for getUsername())
     */
    public String getEmail() {
        return getUsername();
    }
}
