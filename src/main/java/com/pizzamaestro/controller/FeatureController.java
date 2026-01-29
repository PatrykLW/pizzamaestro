package com.pizzamaestro.controller;

import com.pizzamaestro.model.User;
import com.pizzamaestro.service.FeatureAccessService;
import com.pizzamaestro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler zarządzania dostępem do funkcji.
 */
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Funkcje", description = "Dostęp do funkcji w zależności od konta")
public class FeatureController {
    
    private final FeatureAccessService featureAccessService;
    private final UserService userService;
    
    /**
     * Pobiera dostępne funkcje dla aktualnego użytkownika.
     */
    @GetMapping("/my-access")
    @Operation(summary = "Moje uprawnienia i dostępne funkcje")
    public ResponseEntity<FeatureAccessService.UserFeatureAccess> getMyAccess() {
        User user = getCurrentUser();
        
        log.info("🔐 Sprawdzam uprawnienia dla: {} ({})", user.getEmail(), user.getAccountType());
        
        FeatureAccessService.UserFeatureAccess access = featureAccessService.getUserFeatureAccess(user);
        return ResponseEntity.ok(access);
    }
    
    /**
     * Sprawdza czy użytkownik może wykonać kalkulację.
     */
    @GetMapping("/check/calculation")
    @Operation(summary = "Sprawdź czy można wykonać kalkulację")
    public ResponseEntity<FeatureAccessService.FeatureCheckResult> checkCalculation() {
        User user = getCurrentUser();
        
        FeatureAccessService.FeatureCheckResult result = featureAccessService.canPerformCalculation(user);
        
        if (!result.isAllowed()) {
            log.warn("⚠️ Odmowa kalkulacji dla: {} - {}", user.getEmail(), result.getReason());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Sprawdza czy użytkownik może używać integracji pogodowej.
     */
    @GetMapping("/check/weather")
    @Operation(summary = "Sprawdź czy można używać pogody")
    public ResponseEntity<FeatureAccessService.FeatureCheckResult> checkWeather() {
        User user = getCurrentUser();
        
        FeatureAccessService.FeatureCheckResult result = featureAccessService.canUseWeatherIntegration(user);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Sprawdza czy użytkownik może używać prefermentu.
     */
    @GetMapping("/check/preferment")
    @Operation(summary = "Sprawdź czy można używać prefermentu")
    public ResponseEntity<FeatureAccessService.FeatureCheckResult> checkPreferment() {
        User user = getCurrentUser();
        
        FeatureAccessService.FeatureCheckResult result = featureAccessService.canUsePreferment(user);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Pobiera informacje o upgrade do PREMIUM.
     */
    @GetMapping("/upgrade-info")
    @Operation(summary = "Informacje o upgrade konta")
    public ResponseEntity<UpgradeInfo> getUpgradeInfo() {
        User user = getCurrentUser();
        FeatureAccessService.UserFeatureAccess access = featureAccessService.getUserFeatureAccess(user);
        
        UpgradeInfo info = UpgradeInfo.builder()
                .currentPlan(access.getAccountTypeName())
                .canUpgrade(access.isCanUpgrade())
                .upgradeMessage(access.getUpgradeMessage())
                .premiumFeatures(java.util.List.of(
                        "Wszystkie style pizzy (włącznie z Detroit, Roman Al Taglio)",
                        "Fermentacja chłodnicza i mieszana",
                        "Prefermenty (poolish, biga, zakwas)",
                        "Integracja pogodowa - automatyczne dostosowanie receptury",
                        "Zaawansowane algorytmy (DDT, analiza mąki)",
                        "Powiadomienia SMS",
                        "100 kalkulacji miesięcznie",
                        "50 zapisanych receptur",
                        "Eksport do PDF",
                        "Bez reklam"
                ))
                .proFeatures(java.util.List.of(
                        "Wszystkie funkcje PREMIUM",
                        "Nieograniczone kalkulacje",
                        "Nieograniczone receptury",
                        "Dostęp API",
                        "Priorytetowe wsparcie",
                        "Własne składniki w bazie"
                ))
                .build();
        
        return ResponseEntity.ok(info);
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email);
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpgradeInfo {
        private String currentPlan;
        private boolean canUpgrade;
        private String upgradeMessage;
        private java.util.List<String> premiumFeatures;
        private java.util.List<String> proFeatures;
    }
}
