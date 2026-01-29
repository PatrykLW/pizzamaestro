package com.pizzamaestro.service.strategy;

import com.pizzamaestro.model.Recipe;

/**
 * Interfejs strategii obliczania ilości drożdży
 * w zależności od metody fermentacji.
 * 
 * Implementacja wzorca Strategy.
 */
public interface FermentationStrategy {
    
    /**
     * Oblicza optymalny procent drożdży (względem mąki)
     * na podstawie parametrów fermentacji.
     *
     * @param totalFermentationHours całkowity czas fermentacji w godzinach
     * @param roomTemperature temperatura pokojowa w °C
     * @param fridgeTemperature temperatura lodówki w °C
     * @param method metoda fermentacji
     * @return procent drożdży świeżych względem mąki
     */
    double calculateYeastPercentage(
            int totalFermentationHours,
            double roomTemperature,
            double fridgeTemperature,
            Recipe.FermentationMethod method
    );
    
    /**
     * Zwraca typ metody fermentacji obsługiwanej przez tę strategię.
     */
    Recipe.FermentationMethod getSupportedMethod();
}
