package com.pizzamaestro.service.strategy;

import com.pizzamaestro.model.Recipe;
import org.springframework.stereotype.Component;

/**
 * Strategia obliczania drożdży dla szybkiej fermentacji tego samego dnia.
 * 
 * Używana gdy potrzebujemy pizzy w ciągu kilku godzin.
 * Wymaga więcej drożdży, ale kosztem smaku i tekstury.
 */
@Component
public class SameDayStrategy implements FermentationStrategy {
    
    private static final double BASE_YEAST_PERCENTAGE = 1.5;
    private static final double BASE_HOURS = 3.0;
    
    @Override
    public double calculateYeastPercentage(
            int totalFermentationHours,
            double roomTemperature,
            double fridgeTemperature,
            Recipe.FermentationMethod method) {
        
        // Dla szybkiej fermentacji liczy się głównie czas i temperatura
        double timeFactor = BASE_HOURS / Math.max(1, totalFermentationHours);
        
        // Temperatura ma duży wpływ przy szybkiej fermentacji
        double tempDiff = roomTemperature - 27.0;
        double tempFactor = Math.pow(2, tempDiff / 8.0); // Silniejszy wpływ temperatury
        
        double yeastPercentage = BASE_YEAST_PERCENTAGE * timeFactor / tempFactor;
        
        // Dla bardzo szybkich fermentacji (<2h) zwiększ drożdże
        if (totalFermentationHours < 2) {
            yeastPercentage *= 1.5;
        }
        
        return Math.max(0.5, Math.min(3.0, yeastPercentage));
    }
    
    @Override
    public Recipe.FermentationMethod getSupportedMethod() {
        return Recipe.FermentationMethod.SAME_DAY;
    }
}
