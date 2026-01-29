package com.pizzamaestro.dto.response;

import com.pizzamaestro.model.MixerType;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Zaawansowana odpowiedź kalkulacji z profesjonalnymi szczegółami.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedCalculationResponse {
    
    // Podstawowe wyniki kalkulacji
    private CalculationResponse basicCalculation;
    
    // Obliczenia DDT (Desired Dough Temperature)
    private DDTCalculation ddtCalculation;
    
    // Zaawansowane obliczenia drożdży
    private YeastCalculation yeastCalculation;
    
    // Analiza mąki
    private FlourAnalysis flourAnalysis;
    
    // Analiza wody
    private WaterAnalysis waterAnalysis;
    
    // Szczegółowy harmonogram
    private List<DetailedScheduleStep> detailedSchedule;
    
    // Profesjonalne wskazówki
    private List<ProTip> proTips;
    
    // Kalkulacja kosztów (opcjonalna)
    private CostCalculation costCalculation;
    
    // Wartości odżywcze (opcjonalne)
    private NutritionInfo nutritionInfo;
    
    /**
     * Obliczenia DDT - temperatura wody.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DDTCalculation {
        private double targetDoughTemperature;
        private double roomTemperature;
        private double flourTemperature;
        private double frictionFactor;
        private double calculatedWaterTemperature;
        private String formula;
        private MixerType mixerType;
        private String mixerTypeName;
        private List<String> warnings;
        private List<String> recommendations;
    }
    
    /**
     * Zaawansowane obliczenia drożdży.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YeastCalculation {
        private Recipe.YeastType yeastType;
        private String yeastTypeName;
        private double freshYeastGrams;
        private double convertedYeastGrams;
        private double yeastPercentage;
        private double effectiveFermentationHours;
        private Map<String, Object> calculationDetails;
        private List<String> adjustments;
    }
    
    /**
     * Analiza mąki.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlourAnalysis {
        private Double flourStrength;
        private Double proteinContent;
        private double hydration;
        private PizzaStyle pizzaStyle;
        private List<String> recommendations;
        private List<String> warnings;
    }
    
    /**
     * Analiza wody.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaterAnalysis {
        private Double hardness;
        private Double ph;
        private double fermentationModifier;
        private double glutenModifier;
        private List<String> effects;
        private List<String> recommendations;
    }
    
    /**
     * Szczegółowy krok harmonogramu.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedScheduleStep {
        private int stepNumber;
        private String title;
        private String description;
        private LocalDateTime scheduledTime;
        private int durationMinutes;
        private Double temperature;
        private String icon;
        private List<String> tips;
        private String importance; // "critical", "important", "normal"
        private boolean completed;
        private String relativeTime;
    }
    
    /**
     * Profesjonalna wskazówka.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProTip {
        private String category; // "fermentation", "kneading", "baking", etc.
        private String title;
        private String content;
        private String source; // źródło informacji
        private boolean premium; // czy dostępna tylko dla premium
    }
    
    /**
     * Kalkulacja kosztów.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostCalculation {
        private double flourCost;
        private double yeastCost;
        private double saltCost;
        private double oilCost;
        private double waterCost;
        private double totalCost;
        private double costPerPizza;
        private String currency;
    }
    
    /**
     * Informacje o wartościach odżywczych.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionInfo {
        private double caloriesPerPizza;
        private double proteinGrams;
        private double carbsGrams;
        private double fatGrams;
        private double fiberGrams;
        private double sodiumMg;
        private String disclaimer;
    }
}
