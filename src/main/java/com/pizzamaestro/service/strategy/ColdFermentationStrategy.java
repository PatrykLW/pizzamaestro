package com.pizzamaestro.service.strategy;

import com.pizzamaestro.constants.CalculatorConstants;
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
    
    @Override
    public double calculateYeastPercentage(
            int totalFermentationHours,
            double roomTemperature,
            double fridgeTemperature,
            Recipe.FermentationMethod method) {
        
        // Zakładamy 2-4h w temp. pokojowej przed lodówką
        int roomHours = Math.min(CalculatorConstants.MAX_ROOM_HOURS_BEFORE_COLD, 
                totalFermentationHours / CalculatorConstants.ROOM_HOURS_DIVISOR);
        int coldHours = totalFermentationHours - roomHours - CalculatorConstants.POST_COLD_REST_HOURS;
        
        // Przelicz godziny w lodówce na ekwiwalent godzin w temp. pokojowej
        double coldActivityFactor = calculateColdActivityFactor(fridgeTemperature);
        double equivalentHours = roomHours + (coldHours * coldActivityFactor) + CalculatorConstants.POST_COLD_REST_HOURS;
        
        // Współczynnik czasu
        double timeFactor = CalculatorConstants.COLD_BASE_HOURS / equivalentHours;
        
        // Współczynnik temperatury pokojowej (wpływa na początkową fazę)
        double tempDiff = roomTemperature - CalculatorConstants.REFERENCE_ROOM_TEMP;
        double tempFactor = Math.pow(CalculatorConstants.Q10_FACTOR, tempDiff / CalculatorConstants.TEMP_BASE_DIFF);
        
        // Oblicz procent drożdży
        double yeastPercentage = CalculatorConstants.COLD_BASE_YEAST_PERCENTAGE * timeFactor * Math.sqrt(tempFactor);
        
        // Dla bardzo długich fermentacji (>48h) zmniejsz jeszcze
        if (totalFermentationHours > CalculatorConstants.LONG_FERMENTATION_HOURS) {
            yeastPercentage *= CalculatorConstants.LONG_COLD_FERMENTATION_FACTOR;
        }
        if (totalFermentationHours > CalculatorConstants.VERY_LONG_FERMENTATION_HOURS) {
            yeastPercentage *= CalculatorConstants.VERY_LONG_COLD_FERMENTATION_FACTOR;
        }
        
        // Ogranicz do rozsądnych wartości
        return Math.max(CalculatorConstants.MIN_YEAST_PERCENTAGE, 
                Math.min(CalculatorConstants.MAX_YEAST_PERCENTAGE, yeastPercentage));
    }
    
    /**
     * Oblicza współczynnik aktywności drożdży w danej temperaturze lodówki.
     */
    private double calculateColdActivityFactor(double fridgeTemp) {
        // W 4°C aktywność to ok. 10%
        // W 0°C aktywność to ok. 5%
        // W 8°C aktywność to ok. 20%
        return CalculatorConstants.COLD_ACTIVITY_BASE + (fridgeTemp * CalculatorConstants.COLD_ACTIVITY_MULTIPLIER);
    }
    
    @Override
    public Recipe.FermentationMethod getSupportedMethod() {
        return Recipe.FermentationMethod.COLD_FERMENTATION;
    }
}
