package com.pizzamaestro.controller;

import com.pizzamaestro.dto.request.CalculationRequest;
import com.pizzamaestro.dto.response.CalculationResponse;
import com.pizzamaestro.model.OvenType;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.security.CurrentUser;
import com.pizzamaestro.service.DoughCalculatorService;
import com.pizzamaestro.service.RecipeService;
import com.pizzamaestro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kontroler kalkulatora ciasta na pizzę.
 * Główny endpoint aplikacji do obliczania receptur.
 */
@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kalkulator", description = "Obliczanie receptur ciasta na pizzę")
public class CalculatorController {
    
    private final DoughCalculatorService calculatorService;
    private final RecipeService recipeService;
    private final UserService userService;
    
    @Value("${pizzamaestro.free-tier.max-calculations-per-month}")
    private int maxFreeCalculations;
    
    /**
     * Publiczna kalkulacja bez zapisywania (dla niezalogowanych).
     */
    @PostMapping("/public/calculate")
    @Operation(summary = "Publiczna kalkulacja receptury (bez zapisywania)")
    public ResponseEntity<CalculationResponse> calculatePublic(
            @Valid @RequestBody CalculationRequest request) {
        
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║ 🍕 PUBLICZNA KALKULACJA CIASTA NA PIZZĘ                  ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📊 PARAMETRY WEJŚCIOWE:");
        log.info("   🍕 Styl pizzy: {}", request.getPizzaStyle());
        log.info("   🔢 Liczba pizz: {}", request.getNumberOfPizzas());
        log.info("   ⚖️  Waga kulki: {}g", request.getBallWeight());
        log.info("   💧 Hydratacja: {}%", request.getHydration());
        log.info("   🧂 Sól: {}%", request.getSaltPercentage());
        log.info("   🫒 Oliwa: {}%", request.getOilPercentage());
        log.info("   🍬 Cukier: {}%", request.getSugarPercentage());
        log.info("   🦠 Drożdże: {}", request.getYeastType());
        log.info("   ⏱️  Fermentacja: {}h ({})", request.getTotalFermentationHours(), request.getFermentationMethod());
        log.info("   🌡️  Temp. pokojowa: {}°C", request.getRoomTemperature());
        log.info("   ❄️  Temp. lodówki: {}°C", request.getFridgeTemperature());
        
        // Wymuszamy brak zapisu dla publicznych kalkulacji
        request.setSaveRecipe(false);
        
        long startTime = System.currentTimeMillis();
        CalculationResponse response = calculatorService.calculate(request);
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("═══════════════════════════════════════════════════════════");
        log.info("📊 WYNIKI KALKULACJI:");
        log.info("   🌾 Mąka: {}g", response.getIngredients().getFlourGrams());
        log.info("   💧 Woda: {}g", response.getIngredients().getWaterGrams());
        log.info("   🧂 Sól: {}g", response.getIngredients().getSaltGrams());
        log.info("   🦠 Drożdże: {}g ({})", response.getIngredients().getYeastGrams(), response.getIngredients().getYeastType());
        log.info("   🫒 Oliwa: {}g", response.getIngredients().getOilGrams());
        log.info("   📦 Całkowita waga ciasta: {}g", response.getIngredients().getTotalDoughWeight());
        log.info("   ⏱️  Czas kalkulacji: {}ms", duration);
        log.info("═══════════════════════════════════════════════════════════");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kalkulacja dla zalogowanych użytkowników z opcją zapisu.
     */
    @PostMapping("/calculate")
    @Operation(summary = "Kalkulacja receptury z opcją zapisania", 
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CalculationResponse> calculate(
            @Valid @RequestBody CalculationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        log.info("Kalkulacja dla użytkownika {}: {} pizz, styl: {}", 
                userId, request.getNumberOfPizzas(), request.getPizzaStyle());
        
        // Sprawdź limit kalkulacji dla darmowych użytkowników
        if (!userService.canPerformCalculation(userId, maxFreeCalculations)) {
            return ResponseEntity.status(429)
                    .body(null); // Rate limit exceeded
        }
        
        CalculationResponse response = recipeService.calculateAndSave(request, userId);
        
        // Zwiększ licznik kalkulacji
        userService.incrementCalculationCount(userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Publiczny endpoint - style pizzy (dla niezalogowanych).
     */
    @GetMapping("/public/styles")
    @Operation(summary = "Lista dostępnych stylów pizzy (publiczny)")
    public ResponseEntity<List<Map<String, Object>>> getStylesPublic() {
        return getStyles();
    }
    
    /**
     * Pobiera dostępne style pizzy.
     */
    @GetMapping("/styles")
    @Operation(summary = "Lista dostępnych stylów pizzy")
    public ResponseEntity<List<Map<String, Object>>> getStyles() {
        List<Map<String, Object>> styles = Arrays.stream(PizzaStyle.values())
                .map(style -> Map.<String, Object>of(
                        "id", style.name(),
                        "name", style.getDisplayName(),
                        "description", style.getDescription(),
                        "defaults", Map.of(
                                "hydration", style.getDefaultHydration(),
                                "hydrationMin", style.getMinHydration(),
                                "hydrationMax", style.getMaxHydration(),
                                "ballWeight", style.getDefaultBallWeight(),
                                "fermentationHours", style.getDefaultFermentationHours(),
                                "saltPercentage", style.getDefaultSaltPercentage(),
                                "oilPercentage", style.getDefaultOilPercentage(),
                                "sugarPercentage", style.getDefaultSugarPercentage()
                        ),
                        "recommendedOven", Map.of(
                                "type", style.getRecommendedOven().name(),
                                "name", style.getRecommendedOven().getDisplayName(),
                                "temperature", style.getOvenTemperature(),
                                "bakingTime", style.getBakingTimeSeconds()
                        )
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(styles);
    }
    
    /**
     * Publiczny endpoint - typy pieców (dla niezalogowanych).
     */
    @GetMapping("/public/ovens")
    @Operation(summary = "Lista typów pieców (publiczny)")
    public ResponseEntity<List<Map<String, Object>>> getOvensPublic() {
        return getOvens();
    }
    
    /**
     * Pobiera dostępne typy pieców z informacjami o temperaturach góra/dół.
     */
    @GetMapping("/ovens")
    @Operation(summary = "Lista dostępnych typów pieców")
    public ResponseEntity<List<Map<String, Object>>> getOvens() {
        List<Map<String, Object>> ovens = Arrays.stream(OvenType.values())
                .map(oven -> {
                    java.util.Map<String, Object> ovenMap = new java.util.HashMap<>();
                    ovenMap.put("id", oven.name());
                    ovenMap.put("name", oven.getDisplayName());
                    ovenMap.put("description", oven.getDescription());
                    ovenMap.put("minTemperature", oven.getMinTemperature());
                    ovenMap.put("maxTemperature", oven.getMaxTemperature());
                    ovenMap.put("recommendedTemperature", oven.getRecommendedTemperature());
                    ovenMap.put("preheatingRequired", oven.isPreheatingRequired());
                    ovenMap.put("hasSeparateTopBottom", oven.isHasSeparateTopBottom());
                    ovenMap.put("tips", oven.getTips());
                    
                    // Dodaj temperatury góra/dół jeśli piec je obsługuje
                    if (oven.isHasSeparateTopBottom()) {
                        ovenMap.put("topTempMin", oven.getTopTempMin());
                        ovenMap.put("topTempMax", oven.getTopTempMax());
                        ovenMap.put("bottomTempMin", oven.getBottomTempMin());
                        ovenMap.put("bottomTempMax", oven.getBottomTempMax());
                        ovenMap.put("recommendedTopTemperature", oven.getRecommendedTopTemperature());
                        ovenMap.put("recommendedBottomTemperature", oven.getRecommendedBottomTemperature());
                    }
                    
                    return ovenMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ovens);
    }
    
    /**
     * Publiczny endpoint - typy drożdży.
     */
    @GetMapping("/public/yeast-types")
    @Operation(summary = "Lista typów drożdży (publiczny)")
    public ResponseEntity<List<Map<String, Object>>> getYeastTypesPublic() {
        return getYeastTypes();
    }
    
    /**
     * Pobiera typy drożdży.
     */
    @GetMapping("/yeast-types")
    @Operation(summary = "Lista typów drożdży")
    public ResponseEntity<List<Map<String, Object>>> getYeastTypes() {
        List<Map<String, Object>> types = Arrays.stream(Recipe.YeastType.values())
                .map(type -> Map.<String, Object>of(
                        "id", type.name(),
                        "name", type.getDisplayName(),
                        "conversionFactor", type.getConversionFactor()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(types);
    }
    
    /**
     * Publiczny endpoint - metody fermentacji.
     */
    @GetMapping("/public/fermentation-methods")
    @Operation(summary = "Lista metod fermentacji (publiczny)")
    public ResponseEntity<List<Map<String, Object>>> getFermentationMethodsPublic() {
        return getFermentationMethods();
    }
    
    /**
     * Pobiera metody fermentacji.
     */
    @GetMapping("/fermentation-methods")
    @Operation(summary = "Lista metod fermentacji")
    public ResponseEntity<List<Map<String, Object>>> getFermentationMethods() {
        List<Map<String, Object>> methods = Arrays.stream(Recipe.FermentationMethod.values())
                .map(method -> Map.<String, Object>of(
                        "id", method.name(),
                        "name", method.getDisplayName(),
                        "description", method.getDescription()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(methods);
    }
    
    /**
     * Pobiera typy prefermentów.
     */
    @GetMapping("/preferment-types")
    @Operation(summary = "Lista typów prefermentów")
    public ResponseEntity<List<Map<String, Object>>> getPrefermentTypes() {
        List<Map<String, Object>> types = Arrays.stream(Recipe.PrefermentType.values())
                .map(type -> Map.<String, Object>of(
                        "id", type.name(),
                        "name", type.getDisplayName(),
                        "description", type.getDescription(),
                        "hydration", type.getHydration()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(types);
    }
    
    private String getUserId(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername()).getId();
    }
}
