package com.pizzamaestro.service.strategy;

import com.pizzamaestro.model.Recipe;
import org.springframework.stereotype.Component;

/**
 * Strategia obliczania drożdży dla fermentacji w temperaturze pokojowej.
 * 
 * Algorytm bazuje na modelu aktywności drożdży w zależności od temperatury.
 * Temperatura optymalna dla drożdży to ok. 27°C.
 * Poniżej i powyżej tej temperatury aktywność maleje.
 */
@Component
public class RoomTemperatureStrategy implements FermentationStrategy {
    
    // Bazowy procent drożdży dla 6h fermentacji w 24°C
    private static final double BASE_YEAST_PERCENTAGE = 0.5;
    private static final double OPTIMAL_TEMP = 27.0;
    private static final double BASE_HOURS = 6.0;
    
    @Override
    public double calculateYeastPercentage(
            int totalFermentationHours,
            double roomTemperature,
            double fridgeTemperature,
            Recipe.FermentationMethod method) {
        
        // Współczynnik czasu - im dłuższa fermentacja, tym mniej drożdży
        double timeFactor = BASE_HOURS / totalFermentationHours;
        
        // Współczynnik temperatury - wzór Arrheniusa uproszczony
        // Q10 dla drożdży to ok. 2 (podwojenie aktywności na każde 10°C)
        double tempDiff = roomTemperature - OPTIMAL_TEMP;
        double tempFactor = Math.pow(2, tempDiff / 10.0);
        
        // Oblicz procent drożdży
        double yeastPercentage = BASE_YEAST_PERCENTAGE * timeFactor / tempFactor;
        
        // Ogranicz do rozsądnych wartości
        return Math.max(0.05, Math.min(3.0, yeastPercentage));
    }
    
    @Override
    public Recipe.FermentationMethod getSupportedMethod() {
        return Recipe.FermentationMethod.ROOM_TEMPERATURE;
    }
}
