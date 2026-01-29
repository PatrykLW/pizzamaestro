package com.pizzamaestro.service.strategy;

import com.pizzamaestro.model.Recipe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Fabryka strategii fermentacji.
 * Automatycznie rejestruje wszystkie strategie i udostępnia je na żądanie.
 */
@Component
@RequiredArgsConstructor
public class FermentationStrategyFactory {
    
    private final List<FermentationStrategy> strategies;
    private final Map<Recipe.FermentationMethod, FermentationStrategy> strategyMap = new EnumMap<>(Recipe.FermentationMethod.class);
    
    @PostConstruct
    public void init() {
        for (FermentationStrategy strategy : strategies) {
            strategyMap.put(strategy.getSupportedMethod(), strategy);
        }
    }
    
    /**
     * Zwraca strategię dla danej metody fermentacji.
     * 
     * @param method metoda fermentacji
     * @return odpowiednia strategia
     * @throws IllegalArgumentException jeśli metoda nie jest obsługiwana
     */
    public FermentationStrategy getStrategy(Recipe.FermentationMethod method) {
        FermentationStrategy strategy = strategyMap.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Nieobsługiwana metoda fermentacji: " + method);
        }
        return strategy;
    }
}
