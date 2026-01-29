package com.pizzamaestro.dto.response;

import com.pizzamaestro.model.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTOs odpowiedzi związane z autentykacją.
 */
public class AuthResponse {
    
    /**
     * Odpowiedź z tokenami JWT.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserInfo user;
    }
    
    /**
     * Informacje o użytkowniku.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private boolean phoneVerified;
        private Set<User.Role> roles;
        private User.AccountType accountType;
        private boolean isPremium;
        private LocalDateTime premiumExpiresAt;
        private UserPreferencesInfo preferences;
        private UserStatsInfo stats;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
    }
    
    /**
     * Informacje o preferencjach użytkownika.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferencesInfo {
        // Ustawienia ogólne
        private String language;
        private String theme;
        private String temperatureUnit;
        private String weightUnit;
        
        // Powiadomienia
        private boolean emailNotifications;
        private boolean smsNotifications;
        private boolean pushNotifications;
        private Integer smsReminderMinutesBefore;
        
        // Domyślny styl pizzy
        private String defaultPizzaStyle;
        
        // Domyślny sprzęt
        private String defaultOvenType;
        private String defaultMixerType;
        private Integer mixerWattage;
        
        // Dostępne składniki
        private List<String> availableFlourIds;
        private String defaultWaterId;
        
        // Warunki środowiskowe
        private Double typicalRoomTemperature;
        private Double typicalFridgeTemperature;
        private String defaultCity;
        private Double defaultLatitude;
        private Double defaultLongitude;
    }
    
    /**
     * Informacje o statystykach użytkownika.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatsInfo {
        private int totalCalculations;
        private int calculationsThisMonth;
        private int totalPizzasBaked;
        private int smsUsedThisMonth;
        private LocalDateTime lastCalculationAt;
    }
    
    /**
     * Odpowiedź generyczna z komunikatem.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String message;
        private boolean success;
    }
}
