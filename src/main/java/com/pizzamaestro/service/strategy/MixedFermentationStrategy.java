package com.pizzamaestro.service.strategy;

import com.pizzamaestro.model.Recipe;
import org.springframework.stereotype.Component;

/**
 * Strategia obliczania drożdży dla fermentacji mieszanej.
 * 
 * Metoda mieszana łączy krótszą fermentację w temp. pokojowej
 * z fermentacją w lodówce, oferując kompromis między
 * wygodą a jakością.
 */
@Component
public class MixedFermentationStrategy implements FermentationStrategy {
    
    private static final double BASE_YEAST_PERCENTAGE = 0.25;
    
    @Override
    public double calculateYeastPercentage(
            int totalFermentationHours,
            double roomTemperature,
            double fridgeTemperature,
            Recipe.FermentationMethod method) {
        
        // Typowy podział: 30% czasu w temp. pokojowej, 70% w lodówce
        double roomRatio = 0.3;
        double coldRatio = 0.7;
        
        int roomHours = (int) (totalFermentationHours * roomRatio);
        int coldHours = (int) (totalFermentationHours * coldRatio);
        
        // Ekwiwalentne godziny fermentacji
        double coldActivityFactor = 0.05 + (fridgeTemperature * 0.025);
        double equivalentHours = roomHours + (coldHours * coldActivityFactor);
        
        // Normalizuj względem bazowych 8h ekwiwalentnych
        double timeFactor = 8.0 / equivalentHours;
        
        // Wpływ temperatury pokojowej
        double tempDiff = roomTemperature - 24.0;
        double tempFactor = Math.pow(2, tempDiff / 10.0);
        
        double yeastPercentage = BASE_YEAST_PERCENTAGE * timeFactor / tempFactor;
        
        return Math.max(0.05, Math.min(1.0, yeastPercentage));
    }
    
    @Override
    public Recipe.FermentationMethod getSupportedMethod() {
        return Recipe.FermentationMethod.MIXED;
    }
}
