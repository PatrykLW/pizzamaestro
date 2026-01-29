package com.pizzamaestro.service.strategy;

import com.pizzamaestro.model.Recipe;
import org.springframework.stereotype.Component;

/**
 * Strategia obliczania drożdży dla fermentacji chłodniczej.
 * 
 * Fermentacja w lodówce (2-6°C) znacząco spowalnia aktywność drożdży,
 * ale pozwala na rozwój głębszego smaku i lepszą teksturę.
 * 
 * Algorytm uwzględnia:
 * - Początkową fermentację w temp. pokojowej (2-4h)
 * - Długą fermentację w lodówce
 * - Końcowy odpoczynek po wyjęciu z lodówki
 */
@Component
public class ColdFermentationStrategy implements FermentationStrategy {
    
    // Bazowy procent drożdży dla 24h fermentacji w lodówce
    private static final double BASE_YEAST_PERCENTAGE = 0.15;
    private static final double BASE_COLD_HOURS = 24.0;
    private static final double COLD_ACTIVITY_FACTOR = 0.1; // 10% aktywności w lodówce
    
    @Override
    public double calculateYeastPercentage(
            int totalFermentationHours,
            double roomTemperature,
            double fridgeTemperature,
            Recipe.FermentationMethod method) {
        
        // Zakładamy 2-4h w temp. pokojowej przed lodówką
        int roomHours = Math.min(4, totalFermentationHours / 6);
        int coldHours = totalFermentationHours - roomHours - 2; // 2h po wyjęciu
        
        // Przelicz godziny w lodówce na ekwiwalent godzin w temp. pokojowej
        // Aktywność w lodówce (4°C) to ok. 10% aktywności w 24°C
        double coldActivityFactor = calculateColdActivityFactor(fridgeTemperature);
        double equivalentHours = roomHours + (coldHours * coldActivityFactor) + 2;
        
        // Współczynnik czasu
        double timeFactor = BASE_COLD_HOURS / equivalentHours;
        
        // Współczynnik temperatury pokojowej (wpływa na początkową fazę)
        double tempDiff = roomTemperature - 24.0;
        double tempFactor = Math.pow(2, tempDiff / 10.0);
        
        // Oblicz procent drożdży
        double yeastPercentage = BASE_YEAST_PERCENTAGE * timeFactor * Math.sqrt(tempFactor);
        
        // Dla bardzo długich fermentacji (>48h) zmniejsz jeszcze
        if (totalFermentationHours > 48) {
            yeastPercentage *= 0.7;
        }
        if (totalFermentationHours > 72) {
            yeastPercentage *= 0.8;
        }
        
        // Ogranicz do rozsądnych wartości
        return Math.max(0.02, Math.min(0.5, yeastPercentage));
    }
    
    /**
     * Oblicza współczynnik aktywności drożdży w danej temperaturze lodówki.
     */
    private double calculateColdActivityFactor(double fridgeTemp) {
        // W 4°C aktywność to ok. 10%
        // W 0°C aktywność to ok. 5%
        // W 8°C aktywność to ok. 20%
        return 0.05 + (fridgeTemp * 0.025);
    }
    
    @Override
    public Recipe.FermentationMethod getSupportedMethod() {
        return Recipe.FermentationMethod.COLD_FERMENTATION;
    }
}
