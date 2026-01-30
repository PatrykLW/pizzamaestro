package com.pizzamaestro.security;

import com.pizzamaestro.model.User;
import com.pizzamaestro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Serwis ładowania szczegółów użytkownika dla Spring Security.
 * 
 * Sprawdza:
 * - Czy konto istnieje
 * - Czy konto jest aktywne
 * - Czy konto nie jest zablokowane
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Cacheable(value = "userDetails", key = "#email.toLowerCase()", unless = "#result == null")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.trim().isEmpty()) {
            log.warn("⚠️ loadUserByUsername: email jest null lub pusty");
            throw new UsernameNotFoundException("Email jest wymagany");
        }
        
        log.debug("🔐 Ładowanie użytkownika: {}", email);
        
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> {
                    log.warn("⚠️ Nie znaleziono użytkownika: {}", email);
                    return new UsernameNotFoundException("Nie znaleziono użytkownika: " + email);
                });
        
        // Sprawdź stan konta
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = !isAccountLocked(user);
        
        if (!user.isEnabled()) {
            log.warn("⚠️ Konto nieaktywne: {}", email);
        }
        if (!accountNonLocked) {
            log.warn("⚠️ Konto zablokowane: {}", email);
        }
        
        log.debug("✓ Załadowano użytkownika: {}, role: {}", email, user.getRoles());
        
        // Return UserPrincipal with userId to avoid N+1 query in controllers
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );
    }
    
    /**
     * Sprawdza czy konto użytkownika jest zablokowane.
     * Konto jest zablokowane jeśli ma więcej niż 5 nieudanych prób logowania w ostatniej godzinie.
     */
    private boolean isAccountLocked(User user) {
        // Blokowanie konta po nieudanych próbach logowania
        // Aktualnie zwraca false - pełna implementacja wymaga śledzenia prób logowania
        return false;
    }
}
