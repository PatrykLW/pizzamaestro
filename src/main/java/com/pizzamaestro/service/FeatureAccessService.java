package com.pizzamaestro.service;

import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.model.User;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Serwis zarządzania dostępem do funkcji aplikacji.
 * 
 * Poziomy dostępu:
 * - FREE: Podstawowe funkcje, ograniczenia
 * - PREMIUM: Pełne funkcje, więcej zapisów, SMS
 * - PRO: Wszystkie funkcje, API, bez reklam
 * - ADMIN: Pełny dostęp + zarządzanie
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureAccessService {
    
    // ========================================
    // DEFINICJE LIMITÓW
    // ========================================
    
    private static final Map<User.AccountType, AccountLimits> ACCOUNT_LIMITS = Map.of(
            User.AccountType.FREE, AccountLimits.builder()
                    .maxCalculationsPerMonth(10)
                    .maxSavedRecipes(5)
                    .maxPhotosPerRecipe(1)
                    .smsNotificationsEnabled(false)
                    .maxSmsPerMonth(0)
                    .weatherIntegration(false)
                    .advancedAlgorithms(false)
                    .exportToPdf(false)
                    .calendarIntegration(false)
                    .customIngredients(false)
                    .recipeSharing(false)
                    .showAds(true)
                    .build(),
                    
            User.AccountType.PREMIUM, AccountLimits.builder()
                    .maxCalculationsPerMonth(100)
                    .maxSavedRecipes(50)
                    .maxPhotosPerRecipe(5)
                    .smsNotificationsEnabled(true)
                    .maxSmsPerMonth(30)
                    .weatherIntegration(true)
                    .advancedAlgorithms(true)
                    .exportToPdf(true)
                    .calendarIntegration(true)
                    .customIngredients(true)
                    .recipeSharing(true)
                    .showAds(false)
                    .build(),
                    
            User.AccountType.PRO, AccountLimits.builder()
                    .maxCalculationsPerMonth(-1) // bez limitu
                    .maxSavedRecipes(-1)
                    .maxPhotosPerRecipe(20)
                    .smsNotificationsEnabled(true)
                    .maxSmsPerMonth(100)
                    .weatherIntegration(true)
                    .advancedAlgorithms(true)
                    .exportToPdf(true)
                    .calendarIntegration(true)
                    .customIngredients(true)
                    .recipeSharing(true)
                    .showAds(false)
                    .apiAccess(true)
                    .prioritySupport(true)
                    .build()
    );
    
    // Style dostępne dla FREE
    private static final Set<PizzaStyle> FREE_STYLES = Set.of(
            PizzaStyle.NEAPOLITAN,
            PizzaStyle.NEW_YORK,
            PizzaStyle.FOCACCIA,
            PizzaStyle.PAN
    );
    
    // Metody fermentacji dla FREE
    private static final Set<Recipe.FermentationMethod> FREE_FERMENTATION_METHODS = Set.of(
            Recipe.FermentationMethod.ROOM_TEMPERATURE,
            Recipe.FermentationMethod.SAME_DAY
    );
    
    // Prefermenty tylko dla PREMIUM+
    private static final Set<Recipe.PrefermentType> PREMIUM_PREFERMENTS = Set.of(
            Recipe.PrefermentType.POOLISH,
            Recipe.PrefermentType.BIGA,
            Recipe.PrefermentType.LIEVITO_MADRE
    );
    
    // ========================================
    // SPRAWDZANIE DOSTĘPU
    // ========================================
    
    /**
     * Pobiera pełne informacje o dostępnych funkcjach dla użytkownika.
     */
    public UserFeatureAccess getUserFeatureAccess(User user) {
        log.info("🔐 Sprawdzam uprawnienia użytkownika: {} ({})", 
                user.getEmail(), user.getAccountType());
        
        User.AccountType accountType = user.getAccountType();
        AccountLimits limits = ACCOUNT_LIMITS.getOrDefault(accountType, ACCOUNT_LIMITS.get(User.AccountType.FREE));
        
        // Sprawdź czy premium nie wygasło
        if (accountType == User.AccountType.PREMIUM || accountType == User.AccountType.PRO) {
            if (user.getPremiumExpiresAt() != null && 
                user.getPremiumExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                log.warn("⚠️ Premium wygasło dla użytkownika: {}", user.getEmail());
                limits = ACCOUNT_LIMITS.get(User.AccountType.FREE);
                accountType = User.AccountType.FREE;
            }
        }
        
        // Oblicz użycie
        int calculationsUsed = user.getUsageStats() != null ? 
                user.getUsageStats().getCalculationsThisMonth() : 0;
        int recipesUsed = 0; // TODO: pobierz z repozytorium
        int smsUsed = user.getUsageStats() != null ? 
                user.getUsageStats().getSmsUsedThisMonth() : 0;
        
        return UserFeatureAccess.builder()
                .accountType(accountType)
                .accountTypeName(getAccountTypeName(accountType))
                .limits(limits)
                
                // Style pizzy
                .availablePizzaStyles(getAvailableStyles(accountType))
                .lockedPizzaStyles(getLockedStyles(accountType))
                
                // Metody fermentacji
                .availableFermentationMethods(getAvailableFermentationMethods(accountType))
                .lockedFermentationMethods(getLockedFermentationMethods(accountType))
                
                // Prefermenty
                .prefermentAvailable(accountType != User.AccountType.FREE)
                .availablePreferments(getAvailablePreferments(accountType))
                
                // Użycie
                .calculationsUsed(calculationsUsed)
                .calculationsRemaining(limits.getMaxCalculationsPerMonth() == -1 ? 
                        -1 : limits.getMaxCalculationsPerMonth() - calculationsUsed)
                .recipesUsed(recipesUsed)
                .recipesRemaining(limits.getMaxSavedRecipes() == -1 ? 
                        -1 : limits.getMaxSavedRecipes() - recipesUsed)
                .smsUsed(smsUsed)
                .smsRemaining(limits.getMaxSmsPerMonth() - smsUsed)
                
                // Funkcje
                .features(buildFeatureList(accountType, limits))
                
                // Upgrade info
                .canUpgrade(accountType == User.AccountType.FREE || accountType == User.AccountType.PREMIUM)
                .upgradeMessage(getUpgradeMessage(accountType))
                
                .build();
    }
    
    /**
     * Sprawdza czy użytkownik może wykonać kalkulację.
     */
    public FeatureCheckResult canPerformCalculation(User user) {
        UserFeatureAccess access = getUserFeatureAccess(user);
        
        if (access.getLimits().getMaxCalculationsPerMonth() == -1) {
            return FeatureCheckResult.allowed();
        }
        
        if (access.getCalculationsRemaining() <= 0) {
            return FeatureCheckResult.denied(
                    "Wykorzystano limit kalkulacji w tym miesiącu",
                    "Przejdź na konto PREMIUM aby uzyskać więcej kalkulacji"
            );
        }
        
        return FeatureCheckResult.allowed();
    }
    
    /**
     * Sprawdza czy użytkownik może używać danego stylu pizzy.
     */
    public FeatureCheckResult canUsePizzaStyle(User user, PizzaStyle style) {
        if (user.getAccountType() != User.AccountType.FREE) {
            return FeatureCheckResult.allowed();
        }
        
        if (FREE_STYLES.contains(style)) {
            return FeatureCheckResult.allowed();
        }
        
        return FeatureCheckResult.denied(
                String.format("Styl %s jest dostępny tylko dla użytkowników PREMIUM", style.getDisplayName()),
                "Przejdź na PREMIUM aby odblokować wszystkie style pizzy"
        );
    }
    
    /**
     * Sprawdza czy użytkownik może używać danej metody fermentacji.
     */
    public FeatureCheckResult canUseFermentationMethod(User user, Recipe.FermentationMethod method) {
        if (user.getAccountType() != User.AccountType.FREE) {
            return FeatureCheckResult.allowed();
        }
        
        if (FREE_FERMENTATION_METHODS.contains(method)) {
            return FeatureCheckResult.allowed();
        }
        
        return FeatureCheckResult.denied(
                String.format("Metoda %s jest dostępna tylko dla użytkowników PREMIUM", method.getDisplayName()),
                "Fermentacja chłodnicza i mieszana wymaga konta PREMIUM"
        );
    }
    
    /**
     * Sprawdza czy użytkownik może używać prefermentu.
     */
    public FeatureCheckResult canUsePreferment(User user) {
        if (user.getAccountType() == User.AccountType.FREE) {
            return FeatureCheckResult.denied(
                    "Prefermenty (poolish, biga, zakwas) są dostępne tylko dla PREMIUM",
                    "Przejdź na PREMIUM aby odblokować zaawansowane techniki"
            );
        }
        return FeatureCheckResult.allowed();
    }
    
    /**
     * Sprawdza czy użytkownik może zapisać recepturę.
     */
    public FeatureCheckResult canSaveRecipe(User user, int currentRecipeCount) {
        AccountLimits limits = ACCOUNT_LIMITS.getOrDefault(
                user.getAccountType(), ACCOUNT_LIMITS.get(User.AccountType.FREE));
        
        if (limits.getMaxSavedRecipes() == -1) {
            return FeatureCheckResult.allowed();
        }
        
        if (currentRecipeCount >= limits.getMaxSavedRecipes()) {
            return FeatureCheckResult.denied(
                    String.format("Osiągnięto limit %d zapisanych receptur", limits.getMaxSavedRecipes()),
                    "Przejdź na PREMIUM aby zapisywać więcej receptur"
            );
        }
        
        return FeatureCheckResult.allowed();
    }
    
    /**
     * Sprawdza czy użytkownik ma dostęp do pogody.
     */
    public FeatureCheckResult canUseWeatherIntegration(User user) {
        AccountLimits limits = ACCOUNT_LIMITS.getOrDefault(
                user.getAccountType(), ACCOUNT_LIMITS.get(User.AccountType.FREE));
        
        if (!limits.isWeatherIntegration()) {
            return FeatureCheckResult.denied(
                    "Integracja pogodowa dostępna tylko dla PREMIUM",
                    "Przejdź na PREMIUM aby automatycznie dostosowywać recepturę do pogody"
            );
        }
        
        return FeatureCheckResult.allowed();
    }
    
    // ========================================
    // HELPERS
    // ========================================
    
    private String getAccountTypeName(User.AccountType type) {
        return switch (type) {
            case FREE -> "Konto Darmowe";
            case PREMIUM -> "Konto Premium";
            case PRO -> "Konto Pro";
        };
    }
    
    private List<PizzaStyleInfo> getAvailableStyles(User.AccountType type) {
        List<PizzaStyleInfo> styles = new ArrayList<>();
        
        for (PizzaStyle style : PizzaStyle.values()) {
            if (type != User.AccountType.FREE || FREE_STYLES.contains(style)) {
                styles.add(PizzaStyleInfo.builder()
                        .style(style)
                        .name(style.getDisplayName())
                        .description(style.getDescription())
                        .locked(false)
                        .build());
            }
        }
        
        return styles;
    }
    
    private List<PizzaStyleInfo> getLockedStyles(User.AccountType type) {
        if (type != User.AccountType.FREE) {
            return List.of();
        }
        
        List<PizzaStyleInfo> styles = new ArrayList<>();
        for (PizzaStyle style : PizzaStyle.values()) {
            if (!FREE_STYLES.contains(style)) {
                styles.add(PizzaStyleInfo.builder()
                        .style(style)
                        .name(style.getDisplayName())
                        .description(style.getDescription())
                        .locked(true)
                        .unlockMessage("Dostępne w PREMIUM")
                        .build());
            }
        }
        return styles;
    }
    
    private List<Recipe.FermentationMethod> getAvailableFermentationMethods(User.AccountType type) {
        if (type != User.AccountType.FREE) {
            return Arrays.asList(Recipe.FermentationMethod.values());
        }
        return new ArrayList<>(FREE_FERMENTATION_METHODS);
    }
    
    private List<Recipe.FermentationMethod> getLockedFermentationMethods(User.AccountType type) {
        if (type != User.AccountType.FREE) {
            return List.of();
        }
        
        List<Recipe.FermentationMethod> locked = new ArrayList<>();
        for (Recipe.FermentationMethod method : Recipe.FermentationMethod.values()) {
            if (!FREE_FERMENTATION_METHODS.contains(method)) {
                locked.add(method);
            }
        }
        return locked;
    }
    
    private List<Recipe.PrefermentType> getAvailablePreferments(User.AccountType type) {
        if (type == User.AccountType.FREE) {
            return List.of();
        }
        return Arrays.asList(Recipe.PrefermentType.values());
    }
    
    private List<FeatureInfo> buildFeatureList(User.AccountType type, AccountLimits limits) {
        List<FeatureInfo> features = new ArrayList<>();
        
        features.add(FeatureInfo.builder()
                .name("Kalkulacje")
                .description("Obliczanie receptur ciasta")
                .available(true)
                .limit(limits.getMaxCalculationsPerMonth() == -1 ? 
                        "Bez limitu" : limits.getMaxCalculationsPerMonth() + "/miesiąc")
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Zapisywanie receptur")
                .description("Zapisywanie i przeglądanie historii")
                .available(true)
                .limit(limits.getMaxSavedRecipes() == -1 ? 
                        "Bez limitu" : String.valueOf(limits.getMaxSavedRecipes()))
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Powiadomienia SMS")
                .description("Przypomnienia o etapach fermentacji")
                .available(limits.isSmsNotificationsEnabled())
                .limit(limits.getMaxSmsPerMonth() + "/miesiąc")
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Integracja pogodowa")
                .description("Automatyczne dostosowanie do warunków")
                .available(limits.isWeatherIntegration())
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Zaawansowane algorytmy")
                .description("DDT, analiza mąki, korekty chemiczne")
                .available(limits.isAdvancedAlgorithms())
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Eksport do PDF")
                .description("Eksportuj receptury do druku")
                .available(limits.isExportToPdf())
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Integracja z kalendarzem")
                .description("Dodawanie harmonogramu do kalendarza")
                .available(limits.isCalendarIntegration())
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Własne składniki")
                .description("Dodawanie własnych mąk i składników")
                .available(limits.isCustomIngredients())
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Udostępnianie receptur")
                .description("Publikowanie receptur społeczności")
                .available(limits.isRecipeSharing())
                .build());
        
        features.add(FeatureInfo.builder()
                .name("Bez reklam")
                .description("Aplikacja bez reklam")
                .available(!limits.isShowAds())
                .build());
        
        return features;
    }
    
    private String getUpgradeMessage(User.AccountType type) {
        return switch (type) {
            case FREE -> "Przejdź na PREMIUM i odblokuj wszystkie style pizzy, fermentację chłodniczą, prefermenty i więcej!";
            case PREMIUM -> "Przejdź na PRO i uzyskaj nieograniczone kalkulacje, API oraz priorytetowe wsparcie!";
            case PRO -> "Masz już najwyższy poziom konta!";
        };
    }
    
    // ========================================
    // DTOs
    // ========================================
    
    @Data
    @Builder
    public static class AccountLimits {
        private int maxCalculationsPerMonth;
        private int maxSavedRecipes;
        private int maxPhotosPerRecipe;
        private boolean smsNotificationsEnabled;
        private int maxSmsPerMonth;
        private boolean weatherIntegration;
        private boolean advancedAlgorithms;
        private boolean exportToPdf;
        private boolean calendarIntegration;
        private boolean customIngredients;
        private boolean recipeSharing;
        private boolean showAds;
        private boolean apiAccess;
        private boolean prioritySupport;
    }
    
    @Data
    @Builder
    public static class UserFeatureAccess {
        private User.AccountType accountType;
        private String accountTypeName;
        private AccountLimits limits;
        
        private List<PizzaStyleInfo> availablePizzaStyles;
        private List<PizzaStyleInfo> lockedPizzaStyles;
        
        private List<Recipe.FermentationMethod> availableFermentationMethods;
        private List<Recipe.FermentationMethod> lockedFermentationMethods;
        
        private boolean prefermentAvailable;
        private List<Recipe.PrefermentType> availablePreferments;
        
        private int calculationsUsed;
        private int calculationsRemaining;
        private int recipesUsed;
        private int recipesRemaining;
        private int smsUsed;
        private int smsRemaining;
        
        private List<FeatureInfo> features;
        
        private boolean canUpgrade;
        private String upgradeMessage;
    }
    
    @Data
    @Builder
    public static class PizzaStyleInfo {
        private PizzaStyle style;
        private String name;
        private String description;
        private boolean locked;
        private String unlockMessage;
    }
    
    @Data
    @Builder
    public static class FeatureInfo {
        private String name;
        private String description;
        private boolean available;
        private String limit;
    }
    
    @Data
    @Builder
    public static class FeatureCheckResult {
        private boolean allowed;
        private String reason;
        private String upgradeHint;
        
        public static FeatureCheckResult allowed() {
            return FeatureCheckResult.builder().allowed(true).build();
        }
        
        public static FeatureCheckResult denied(String reason, String upgradeHint) {
            return FeatureCheckResult.builder()
                    .allowed(false)
                    .reason(reason)
                    .upgradeHint(upgradeHint)
                    .build();
        }
    }
}
