package com.pizzamaestro.service.tips;

import com.pizzamaestro.service.TipEngineService.CalculationContext;
import com.pizzamaestro.service.TipEngineService.Tip;

import java.util.List;

/**
 * Interfejs dla generatorów wskazówek.
 * Pozwala na wydzielenie logiki generowania tipów do osobnych klas
 * zgodnie z Single Responsibility Principle.
 */
public interface TipGenerator {
    
    /**
     * Generuje listę wskazówek na podstawie kontekstu kalkulacji.
     * @param context kontekst zawierający parametry kalkulacji
     * @return lista wygenerowanych wskazówek
     */
    List<Tip> generateTips(CalculationContext context);
    
    /**
     * Generuje listę ostrzeżeń na podstawie kontekstu kalkulacji.
     * @param context kontekst zawierający parametry kalkulacji
     * @return lista wygenerowanych ostrzeżeń
     */
    List<Tip> generateWarnings(CalculationContext context);
    
    /**
     * Sprawdza czy generator jest odpowiedni dla danego kontekstu.
     * @param context kontekst zawierający parametry kalkulacji
     * @return true jeśli generator powinien być użyty
     */
    boolean isApplicable(CalculationContext context);
    
    /**
     * Zwraca kategorię wskazówek obsługiwanych przez ten generator.
     * @return kategoria wskazówek
     */
    String getCategory();
}
