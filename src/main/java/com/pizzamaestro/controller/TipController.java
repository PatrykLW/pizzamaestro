package com.pizzamaestro.controller;

import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.service.TipEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler interaktywnych wskazówek.
 * 
 * Dostarcza kontekstowe tipy w czasie rzeczywistym
 * podczas tworzenia/edycji receptury.
 */
@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Wskazówki", description = "Interaktywne tipy i rekomendacje")
public class TipController {
    
    private final TipEngineService tipEngineService;
    
    /**
     * Pobiera wszystkie tipy dla aktualnej konfiguracji.
     */
    @PostMapping("/all")
    @Operation(summary = "Pobierz wszystkie tipy dla konfiguracji")
    public ResponseEntity<TipEngineService.TipCollection> getAllTips(
            @Valid @RequestBody TipRequest request) {
        
        log.info("💡 Żądanie tipów dla: style={}, hydration={}, fermentation={}h",
                request.getPizzaStyle(), request.getHydration(), request.getFermentationHours());
        log.debug("   Pełny request: preferment={}, yeast={}, roomTemp={}°C",
                request.isUsePreferment(), request.getYeastType(), request.getRoomTemperature());
        
        TipEngineService.CalculationContext context = buildContext(request);
        TipEngineService.TipCollection tips = tipEngineService.generateAllTips(context);
        
        log.debug("   Wygenerowano: {} tipów, {} ostrzeżeń, {} rekomendacji",
                tips.getTips().size(), tips.getWarnings().size(), tips.getRecommendations().size());
        
        return ResponseEntity.ok(tips);
    }
    
    /**
     * Pobiera tipy dla konkretnej zmiany parametru.
     */
    @PostMapping("/change")
    @Operation(summary = "Pobierz tipy dla zmiany parametru")
    public ResponseEntity<List<TipEngineService.Tip>> getTipsForChange(
            @Valid @RequestBody ChangeRequest request) {
        
        log.info("🔄 Zmiana parametru: {} = {} → {}", 
                request.getParameterName(), request.getOldValue(), request.getNewValue());
        
        if (request.getContext() == null) {
            log.warn("   ⚠️ Brak kontekstu - używam domyślnych wartości");
        }
        
        TipEngineService.CalculationContext context = buildContext(request.getContext());
        List<TipEngineService.Tip> tips = tipEngineService.generateTipsForChange(
                request.getParameterName(),
                request.getOldValue(),
                request.getNewValue(),
                context
        );
        
        log.debug("   Wygenerowano {} tipów dla zmiany", tips.size());
        
        return ResponseEntity.ok(tips);
    }
    
    /**
     * Szybki endpoint - tipy dla hydratacji.
     */
    @GetMapping("/hydration/{value}")
    @Operation(summary = "Tipy dla hydratacji")
    public ResponseEntity<List<TipEngineService.Tip>> getHydrationTips(
            @PathVariable 
            @DecimalMin(value = "40.0", message = "Hydratacja musi być >= 40%")
            @DecimalMax(value = "100.0", message = "Hydratacja musi być <= 100%")
            @Parameter(description = "Procent hydratacji (40-100)")
            double value,
            
            @RequestParam(required = false) 
            @Parameter(description = "Styl pizzy (np. NEAPOLITAN, NEW_YORK)")
            String style) {
        
        log.debug("🌊 Tipy dla hydratacji: {}%, style={}", value, style);
        
        TipEngineService.CalculationContext context = TipEngineService.CalculationContext.builder()
                .hydration(value)
                .pizzaStyle(style != null ? PizzaStyle.valueOf(style.toUpperCase()) : PizzaStyle.NEAPOLITAN)
                .fermentationHours(24)
                .build();
        
        TipEngineService.TipCollection tips = tipEngineService.generateAllTips(context);
        return ResponseEntity.ok(tips.getTips());
    }
    
    /**
     * Szybki endpoint - tipy dla fermentacji.
     */
    @GetMapping("/fermentation/{hours}")
    @Operation(summary = "Tipy dla fermentacji")
    public ResponseEntity<List<TipEngineService.Tip>> getFermentationTips(
            @PathVariable 
            @Min(value = 1, message = "Czas fermentacji musi być >= 1h")
            @Max(value = 168, message = "Czas fermentacji musi być <= 168h (7 dni)")
            @Parameter(description = "Czas fermentacji w godzinach (1-168)")
            int hours,
            
            @RequestParam(required = false) 
            @Parameter(description = "Metoda fermentacji (np. COLD_FERMENTATION, ROOM_TEMPERATURE)")
            String method) {
        
        log.debug("⏰ Tipy dla fermentacji: {}h, method={}", hours, method);
        
        TipEngineService.CalculationContext context = TipEngineService.CalculationContext.builder()
                .fermentationHours(hours)
                .fermentationMethod(method != null ? 
                        Recipe.FermentationMethod.valueOf(method.toUpperCase()) : 
                        Recipe.FermentationMethod.COLD_FERMENTATION)
                .hydration(65)
                .build();
        
        TipEngineService.TipCollection tips = tipEngineService.generateAllTips(context);
        return ResponseEntity.ok(tips.getTips());
    }
    
    /**
     * Szybki endpoint - tipy dla stylu.
     */
    @GetMapping("/style/{style}")
    @Operation(summary = "Tipy dla stylu pizzy")
    public ResponseEntity<List<TipEngineService.Tip>> getStyleTips(
            @PathVariable 
            @NotBlank(message = "Styl pizzy nie może być pusty")
            @Parameter(description = "Styl pizzy (np. NEAPOLITAN, NEW_YORK, ROMAN)")
            String style) {
        
        log.debug("🍕 Tipy dla stylu: {}", style);
        
        try {
            TipEngineService.CalculationContext context = TipEngineService.CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.valueOf(style.toUpperCase()))
                    .hydration(65)
                    .fermentationHours(24)
                    .build();
            
            TipEngineService.TipCollection tips = tipEngineService.generateAllTips(context);
            return ResponseEntity.ok(tips.getTips());
            
        } catch (IllegalArgumentException e) {
            log.warn("   ⚠️ Nieznany styl pizzy: {}", style);
            throw new IllegalArgumentException("Nieznany styl pizzy: " + style + 
                    ". Dostępne: NEAPOLITAN, NEW_YORK, ROMAN, SICILIAN, DETROIT, FOCACCIA, THIN_CRUST, TAVERN_STYLE, PINSA_ROMANA");
        }
    }
    
    /**
     * Szybki endpoint - tipy dla mąki.
     */
    @GetMapping("/flour")
    @Operation(summary = "Tipy dla parametrów mąki")
    public ResponseEntity<List<TipEngineService.Tip>> getFlourTips(
            @RequestParam(required = false) 
            @Min(value = 100, message = "Siła mąki (W) musi być >= 100")
            @Max(value = 500, message = "Siła mąki (W) musi być <= 500")
            @Parameter(description = "Siła mąki W (100-500)")
            Integer strength,
            
            @RequestParam(required = false) 
            @DecimalMin(value = "8.0", message = "Zawartość białka musi być >= 8%")
            @DecimalMax(value = "18.0", message = "Zawartość białka musi być <= 18%")
            @Parameter(description = "Zawartość białka w % (8-18)")
            Double protein) {
        
        log.debug("🌾 Tipy dla mąki: W={}, protein={}%", strength, protein);
        
        TipEngineService.CalculationContext context = TipEngineService.CalculationContext.builder()
                .flourStrength(strength)
                .flourProtein(protein)
                .hydration(65)
                .fermentationHours(24)
                .build();
        
        TipEngineService.TipCollection tips = tipEngineService.generateAllTips(context);
        return ResponseEntity.ok(tips.getTips());
    }
    
    private TipEngineService.CalculationContext buildContext(TipRequest request) {
        return TipEngineService.CalculationContext.builder()
                .pizzaStyle(request.getPizzaStyle() != null ? 
                        PizzaStyle.valueOf(request.getPizzaStyle()) : null)
                .hydration(request.getHydration() != null ? request.getHydration() : 65)
                .fermentationHours(request.getFermentationHours() != null ? 
                        request.getFermentationHours() : 24)
                .fermentationMethod(request.getFermentationMethod() != null ? 
                        Recipe.FermentationMethod.valueOf(request.getFermentationMethod()) : null)
                .roomTemperature(request.getRoomTemperature())
                .fridgeTemperature(request.getFridgeTemperature())
                .flourStrength(request.getFlourStrength())
                .flourProtein(request.getFlourProtein())
                .yeastType(request.getYeastType())
                .usePreferment(request.isUsePreferment())
                .weatherTemperature(request.getWeatherTemperature())
                .weatherHumidity(request.getWeatherHumidity())
                .build();
    }
    
    // ========== DTOs ==========
    
    @lombok.Data
    public static class TipRequest {
        @Size(max = 50, message = "Nazwa stylu zbyt długa")
        private String pizzaStyle;
        
        @DecimalMin(value = "40.0", message = "Hydratacja musi być >= 40%")
        @DecimalMax(value = "100.0", message = "Hydratacja musi być <= 100%")
        private Double hydration;
        
        @Min(value = 1, message = "Czas fermentacji musi być >= 1h")
        @Max(value = 168, message = "Czas fermentacji musi być <= 168h")
        private Integer fermentationHours;
        
        @Size(max = 50, message = "Nazwa metody zbyt długa")
        private String fermentationMethod;
        
        @DecimalMin(value = "5.0", message = "Temperatura pokojowa musi być >= 5°C")
        @DecimalMax(value = "40.0", message = "Temperatura pokojowa musi być <= 40°C")
        private Double roomTemperature;
        
        @DecimalMin(value = "0.0", message = "Temperatura lodówki musi być >= 0°C")
        @DecimalMax(value = "10.0", message = "Temperatura lodówki musi być <= 10°C")
        private Double fridgeTemperature;
        
        @Min(value = 100, message = "Siła mąki musi być >= 100")
        @Max(value = 500, message = "Siła mąki musi być <= 500")
        private Integer flourStrength;
        
        @DecimalMin(value = "8.0", message = "Zawartość białka musi być >= 8%")
        @DecimalMax(value = "18.0", message = "Zawartość białka musi być <= 18%")
        private Double flourProtein;
        
        @Size(max = 50, message = "Nazwa typu drożdży zbyt długa")
        private String yeastType;
        
        private boolean usePreferment;
        
        @DecimalMin(value = "-50.0", message = "Temperatura pogodowa musi być >= -50°C")
        @DecimalMax(value = "60.0", message = "Temperatura pogodowa musi być <= 60°C")
        private Double weatherTemperature;
        
        @DecimalMin(value = "0.0", message = "Wilgotność musi być >= 0%")
        @DecimalMax(value = "100.0", message = "Wilgotność musi być <= 100%")
        private Double weatherHumidity;
    }
    
    @lombok.Data
    public static class ChangeRequest {
        @NotBlank(message = "Nazwa parametru jest wymagana")
        @Size(max = 100, message = "Nazwa parametru zbyt długa")
        private String parameterName;
        
        private Object oldValue;
        
        private Object newValue;
        
        private TipRequest context;
    }
}
