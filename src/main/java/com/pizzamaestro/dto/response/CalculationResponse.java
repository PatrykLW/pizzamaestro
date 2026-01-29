package com.pizzamaestro.dto.response;

import com.pizzamaestro.model.OvenType;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO odpowiedzi kalkulacji receptury.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResponse {
    
    private String recipeId; // jeśli zapisano
    
    private PizzaStyle pizzaStyle;
    
    private String pizzaStyleName;
    
    private int numberOfPizzas;
    
    private int ballWeight;
    
    // ===== OBLICZONE SKŁADNIKI =====
    
    private IngredientsResult ingredients;
    
    // ===== PROCENTY PIEKARSKIE =====
    
    private BakerPercentagesResult bakerPercentages;
    
    // ===== PREFERMENT (jeśli używany) =====
    
    private PrefermentResult preferment;
    
    // ===== CIASTO GŁÓWNE (po odjęciu prefermentu) =====
    
    private MainDoughResult mainDough;
    
    // ===== HARMONOGRAM =====
    
    private List<ScheduleStep> schedule;
    
    // ===== WSKAZÓWKI =====
    
    private List<String> tips;
    
    // ===== INFORMACJE O PIECU =====
    
    private OvenInfo ovenInfo;
    
    /**
     * Wynik - składniki.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientsResult {
        private double totalDoughWeight;
        private double flourGrams;
        private double waterGrams;
        private double saltGrams;
        private double yeastGrams;
        private String yeastType;
        private double oilGrams;
        private double sugarGrams;
        private List<AdditionalIngredientResult> additionalIngredients;
    }
    
    /**
     * Wynik - dodatkowy składnik.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalIngredientResult {
        private String name;
        private double grams;
        private double percentage;
    }
    
    /**
     * Wynik - procenty piekarskie.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BakerPercentagesResult {
        private double flour; // zawsze 100%
        private double water;
        private double salt;
        private double yeast;
        private double oil;
        private double sugar;
    }
    
    /**
     * Wynik - preferment.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrefermentResult {
        private Recipe.PrefermentType type;
        private String typeName;
        private double flourGrams;
        private double waterGrams;
        private double yeastGrams;
        private int fermentationHours;
        private String instructions;
    }
    
    /**
     * Wynik - ciasto główne.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MainDoughResult {
        private double flourGrams;
        private double waterGrams;
        private double saltGrams;
        private double yeastGrams;
        private double oilGrams;
        private double sugarGrams;
    }
    
    /**
     * Krok harmonogramu.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleStep {
        private int stepNumber;
        private Recipe.StepType stepType;
        private String title;
        private String description;
        private LocalDateTime scheduledTime;
        private String relativeTime; // np. "Za 2h 30min"
        private int durationMinutes;
        private Double temperature;
        private String icon;
    }
    
    /**
     * Informacje o piecu.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OvenInfo {
        private OvenType ovenType;
        private String ovenName;
        private int temperature;
        private int bakingTimeSeconds;
        private String tips;
    }
}
