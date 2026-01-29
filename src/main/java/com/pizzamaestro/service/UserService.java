package com.pizzamaestro.service;

import com.pizzamaestro.dto.request.AuthRequest;
import com.pizzamaestro.dto.response.AuthResponse;
import com.pizzamaestro.exception.ResourceNotFoundException;
import com.pizzamaestro.exception.UserAlreadyExistsException;
import com.pizzamaestro.model.User;
import com.pizzamaestro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Serwis zarządzania użytkownikami.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Rejestruje nowego użytkownika.
     * 
     * @param request dane rejestracyjne
     * @return utworzony użytkownik
     * @throws IllegalArgumentException gdy dane są nieprawidłowe
     * @throws UserAlreadyExistsException gdy email jest już zajęty
     */
    @Transactional
    public User registerUser(AuthRequest.RegisterRequest request) {
        // Walidacja wejścia
        if (request == null) {
            log.error("❌ Request rejestracji nie może być null");
            throw new IllegalArgumentException("Dane rejestracyjne są wymagane");
        }
        
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());
        
        log.info("📝 Rejestracja nowego użytkownika: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            log.warn("⚠️ Email już istnieje: {}", request.getEmail());
            throw new UserAlreadyExistsException("Użytkownik o tym adresie email już istnieje");
        }
        
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of(User.Role.ROLE_USER))
                .accountType(User.AccountType.FREE)
                .enabled(true)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .preferences(User.UserPreferences.builder()
                        .language(request.getLanguage() != null ? request.getLanguage() : "pl")
                        .build())
                .usageStats(new User.UsageStats())
                .build();
        
        return userRepository.save(user);
    }
    
    /**
     * Znajduje użytkownika po ID.
     * 
     * @param id identyfikator użytkownika
     * @return użytkownik
     * @throws IllegalArgumentException gdy id jest null/puste
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     */
    public User findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.error("❌ ID użytkownika nie może być puste");
            throw new IllegalArgumentException("ID użytkownika jest wymagane");
        }
        
        log.debug("🔍 Szukam użytkownika po ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Nie znaleziono użytkownika o ID: {}", id);
                    return new ResourceNotFoundException("Użytkownik nie znaleziony");
                });
    }
    
    /**
     * Znajduje użytkownika po email.
     * 
     * @param email adres email
     * @return użytkownik
     * @throws IllegalArgumentException gdy email jest null/pusty
     * @throws ResourceNotFoundException gdy użytkownik nie istnieje
     */
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.error("❌ Email nie może być pusty");
            throw new IllegalArgumentException("Email jest wymagany");
        }
        
        log.debug("🔍 Szukam użytkownika po email: {}", email);
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> {
                    log.warn("⚠️ Nie znaleziono użytkownika o email: {}", email);
                    return new ResourceNotFoundException("Użytkownik nie znaleziony");
                });
    }
    
    /**
     * Aktualizuje datę ostatniego logowania.
     */
    @Transactional
    public void updateLastLogin(String userId) {
        User user = findById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Aktualizuje preferencje użytkownika.
     */
    @Transactional
    public User updatePreferences(String userId, User.UserPreferences preferences) {
        User user = findById(userId);
        user.setPreferences(preferences);
        return userRepository.save(user);
    }
    
    /**
     * Zwiększa licznik kalkulacji użytkownika.
     */
    @Transactional
    public void incrementCalculationCount(String userId) {
        User user = findById(userId);
        User.UsageStats stats = user.getUsageStats();
        
        // Reset miesięcznego licznika jeśli potrzeba
        if (stats.getMonthResetAt() == null || 
            stats.getMonthResetAt().isBefore(LocalDateTime.now().withDayOfMonth(1).withHour(0))) {
            stats.setCalculationsThisMonth(0);
            stats.setSmsUsedThisMonth(0);
            stats.setMonthResetAt(LocalDateTime.now());
        }
        
        stats.setTotalCalculations(stats.getTotalCalculations() + 1);
        stats.setCalculationsThisMonth(stats.getCalculationsThisMonth() + 1);
        stats.setLastCalculationAt(LocalDateTime.now());
        
        userRepository.save(user);
    }
    
    /**
     * Sprawdza czy użytkownik może wykonać kalkulację (limit free tier).
     */
    public boolean canPerformCalculation(String userId, int maxFreeCalculations) {
        User user = findById(userId);
        
        if (user.isPremium()) {
            return true;
        }
        
        User.UsageStats stats = user.getUsageStats();
        
        // Reset jeśli nowy miesiąc
        if (stats.getMonthResetAt() == null || 
            stats.getMonthResetAt().isBefore(LocalDateTime.now().withDayOfMonth(1).withHour(0))) {
            return true;
        }
        
        return stats.getCalculationsThisMonth() < maxFreeCalculations;
    }
    
    /**
     * Zmienia hasło użytkownika.
     */
    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Nieprawidłowe obecne hasło");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * Generuje token resetu hasła.
     */
    @Transactional
    public String generatePasswordResetToken(String email) {
        User user = findByEmail(email);
        
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpires(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        return token;
    }
    
    /**
     * Resetuje hasło przy użyciu tokenu.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Nieprawidłowy token resetu"));
        
        if (user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token resetu wygasł");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);
        userRepository.save(user);
    }
    
    /**
     * Konwertuje użytkownika na DTO.
     */
    public AuthResponse.UserInfo toUserInfo(User user) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .phoneVerified(user.isPhoneVerified())
                .roles(user.getRoles())
                .accountType(user.getAccountType())
                .isPremium(user.isPremium())
                .premiumExpiresAt(user.getPremiumExpiresAt())
                .preferences(toPreferencesInfo(user.getPreferences()))
                .stats(toStatsInfo(user.getUsageStats()))
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
    
    private AuthResponse.UserPreferencesInfo toPreferencesInfo(User.UserPreferences prefs) {
        return AuthResponse.UserPreferencesInfo.builder()
                // Ustawienia ogólne
                .language(prefs.getLanguage())
                .theme(prefs.getTheme())
                .temperatureUnit(prefs.getTemperatureUnit().name())
                .weightUnit(prefs.getWeightUnit().name())
                // Powiadomienia
                .emailNotifications(prefs.isEmailNotifications())
                .smsNotifications(prefs.isSmsNotifications())
                .pushNotifications(prefs.isPushNotifications())
                .smsReminderMinutesBefore(prefs.getSmsReminderMinutesBefore())
                // Domyślny styl pizzy
                .defaultPizzaStyle(prefs.getDefaultPizzaStyle().name())
                // Domyślny sprzęt
                .defaultOvenType(prefs.getDefaultOvenType() != null ? prefs.getDefaultOvenType().name() : null)
                .defaultMixerType(prefs.getDefaultMixerType() != null ? prefs.getDefaultMixerType().name() : null)
                .mixerWattage(prefs.getMixerWattage())
                // Dostępne składniki
                .availableFlourIds(prefs.getAvailableFlourIds())
                .defaultWaterId(prefs.getDefaultWaterId())
                // Warunki środowiskowe
                .typicalRoomTemperature(prefs.getTypicalRoomTemperature())
                .typicalFridgeTemperature(prefs.getTypicalFridgeTemperature())
                .defaultCity(prefs.getDefaultCity())
                .defaultLatitude(prefs.getDefaultLatitude())
                .defaultLongitude(prefs.getDefaultLongitude())
                .build();
    }
    
    private AuthResponse.UserStatsInfo toStatsInfo(User.UsageStats stats) {
        return AuthResponse.UserStatsInfo.builder()
                .totalCalculations(stats.getTotalCalculations())
                .calculationsThisMonth(stats.getCalculationsThisMonth())
                .totalPizzasBaked(stats.getTotalPizzasBaked())
                .smsUsedThisMonth(stats.getSmsUsedThisMonth())
                .lastCalculationAt(stats.getLastCalculationAt())
                .build();
    }
    
    // ==================== Walidacja ====================
    
    /**
     * Waliduje adres email.
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.error("❌ Email nie może być pusty");
            throw new IllegalArgumentException("Email jest wymagany");
        }
        
        String trimmedEmail = email.trim();
        
        if (trimmedEmail.length() > 255) {
            log.error("❌ Email zbyt długi: {} znaków", trimmedEmail.length());
            throw new IllegalArgumentException("Email nie może przekraczać 255 znaków");
        }
        
        // Podstawowa walidacja formatu email
        if (!trimmedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            log.error("❌ Nieprawidłowy format email: {}", trimmedEmail);
            throw new IllegalArgumentException("Nieprawidłowy format adresu email");
        }
        
        log.debug("✅ Email zwalidowany: {}", trimmedEmail);
    }
    
    /**
     * Waliduje hasło.
     */
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            log.error("❌ Hasło nie może być puste");
            throw new IllegalArgumentException("Hasło jest wymagane");
        }
        
        if (password.length() < 8) {
            log.error("❌ Hasło zbyt krótkie: {} znaków", password.length());
            throw new IllegalArgumentException("Hasło musi mieć co najmniej 8 znaków");
        }
        
        if (password.length() > 128) {
            log.error("❌ Hasło zbyt długie: {} znaków", password.length());
            throw new IllegalArgumentException("Hasło nie może przekraczać 128 znaków");
        }
        
        // Sprawdź czy hasło zawiera różne typy znaków
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLower || !hasUpper || !hasDigit) {
            log.warn("⚠️ Słabe hasło - brakuje: lower={}, upper={}, digit={}", hasLower, hasUpper, hasDigit);
            throw new IllegalArgumentException(
                    "Hasło musi zawierać co najmniej jedną małą literę, dużą literę i cyfrę");
        }
        
        log.debug("✅ Hasło zwalidowane");
    }
}
