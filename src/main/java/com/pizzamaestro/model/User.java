package com.pizzamaestro.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Encja użytkownika systemu PizzaMaestro.
 * Przechowuje dane logowania, profil i preferencje.
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    private String id;
    
    @Version
    private Long version;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    private String firstName;
    
    private String lastName;
    
    private String phoneNumber;
    
    private boolean phoneVerified;
    
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @Builder.Default
    private AccountType accountType = AccountType.FREE;
    
    private LocalDateTime premiumExpiresAt;
    
    @Builder.Default
    private UserPreferences preferences = new UserPreferences();
    
    @Builder.Default
    private UsageStats usageStats = new UsageStats();
    
    private boolean enabled;
    
    private boolean emailVerified;
    
    private String verificationToken;
    
    private String resetPasswordToken;
    
    private LocalDateTime resetPasswordExpires;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
    
    /**
     * Role użytkownika w systemie.
     */
    public enum Role {
        ROLE_USER,
        ROLE_PREMIUM,
        ROLE_ADMIN
    }
    
    /**
     * Typ konta użytkownika.
     */
    public enum AccountType {
        FREE,
        PREMIUM,
        PRO
    }
    
    /**
     * Preferencje użytkownika.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferences {
        // === Ustawienia ogólne ===
        @Builder.Default
        private String language = "pl";
        
        @Builder.Default
        private String theme = "light";
        
        @Builder.Default
        private TemperatureUnit temperatureUnit = TemperatureUnit.CELSIUS;
        
        @Builder.Default
        private WeightUnit weightUnit = WeightUnit.GRAMS;
        
        // === Powiadomienia ===
        @Builder.Default
        private boolean emailNotifications = true;
        
        @Builder.Default
        private boolean smsNotifications = false;
        
        @Builder.Default
        private boolean pushNotifications = true;
        
        @Builder.Default
        private Integer smsReminderMinutesBefore = 15; // ile minut przed krokiem wysłać SMS
        
        // === Domyślny styl pizzy ===
        @Builder.Default
        private PizzaStyle defaultPizzaStyle = PizzaStyle.NEAPOLITAN;
        
        // === Domyślny sprzęt ===
        private OvenType defaultOvenType;
        
        private MixerType defaultMixerType;
        
        private Integer mixerWattage; // moc miksera w watach
        
        // === Dostępne składniki ===
        @Builder.Default
        private java.util.List<String> availableFlourIds = new java.util.ArrayList<>(); // ID mąk które użytkownik ma
        
        private String defaultWaterId; // domyślny typ wody
        
        // === Warunki środowiskowe ===
        @Builder.Default
        private Double typicalRoomTemperature = 22.0; // typowa temperatura pokojowa
        
        @Builder.Default
        private Double typicalFridgeTemperature = 4.0; // typowa temperatura lodówki
        
        private String defaultCity; // domyślne miasto dla pogody
        
        private Double defaultLatitude; // szerokość geograficzna
        
        private Double defaultLongitude; // długość geograficzna
    }
    
    /**
     * Statystyki użycia aplikacji.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageStats {
        @Builder.Default
        private int totalCalculations = 0;
        
        @Builder.Default
        private int calculationsThisMonth = 0;
        
        @Builder.Default
        private int totalPizzasBaked = 0;
        
        @Builder.Default
        private int smsUsedThisMonth = 0;
        
        private LocalDateTime lastCalculationAt;
        
        private LocalDateTime monthResetAt;
    }
    
    public enum TemperatureUnit {
        CELSIUS, FAHRENHEIT
    }
    
    public enum WeightUnit {
        GRAMS, OUNCES
    }
    
    /**
     * Sprawdza czy użytkownik ma aktywne premium.
     */
    public boolean isPremium() {
        return accountType == AccountType.PREMIUM || accountType == AccountType.PRO ||
               (premiumExpiresAt != null && premiumExpiresAt.isAfter(LocalDateTime.now()));
    }
    
    /**
     * Sprawdza czy użytkownik ma rolę admina.
     */
    public boolean isAdmin() {
        return roles.contains(Role.ROLE_ADMIN);
    }
}
