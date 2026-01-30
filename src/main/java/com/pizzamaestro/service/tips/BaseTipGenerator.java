package com.pizzamaestro.service.tips;

import com.pizzamaestro.service.TipEngineService.CalculationContext;
import com.pizzamaestro.service.TipEngineService.Tip;
import com.pizzamaestro.service.TipEngineService.TipType;
import com.pizzamaestro.service.TipEngineService.TipCategory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Bazowa klasa dla generatorów wskazówek.
 * Dostarcza wspólne metody pomocnicze.
 */
@Slf4j
public abstract class BaseTipGenerator implements TipGenerator {
    
    /**
     * Tworzy wskazówkę informacyjną.
     */
    protected Tip createInfoTip(TipCategory category, String title, String content, String icon, int priority) {
        return Tip.builder()
                .type(TipType.INFO)
                .category(category)
                .title(title)
                .content(content)
                .icon(icon)
                .priority(priority)
                .build();
    }
    
    /**
     * Tworzy wskazówkę informacyjną ze szczegółami.
     */
    protected Tip createInfoTip(TipCategory category, String title, String content, String details, String icon, int priority) {
        return Tip.builder()
                .type(TipType.INFO)
                .category(category)
                .title(title)
                .content(content)
                .details(details)
                .icon(icon)
                .priority(priority)
                .build();
    }
    
    /**
     * Tworzy ostrzeżenie.
     */
    protected Tip createWarningTip(TipCategory category, String title, String content, String suggestion, int priority) {
        return Tip.builder()
                .type(TipType.WARNING)
                .category(category)
                .title(title)
                .content(content)
                .suggestion(suggestion)
                .icon("⚠️")
                .priority(priority)
                .build();
    }
    
    /**
     * Tworzy rekomendację.
     */
    protected Tip createRecommendation(TipCategory category, String title, String content, String suggestion, int priority) {
        return Tip.builder()
                .type(TipType.RECOMMENDATION)
                .category(category)
                .title(title)
                .content(content)
                .suggestion(suggestion)
                .icon("💡")
                .priority(priority)
                .build();
    }
    
    /**
     * Tworzy wskazówkę naukową.
     */
    protected Tip createScienceTip(TipCategory category, String title, String content, int priority) {
        return Tip.builder()
                .type(TipType.SCIENCE)
                .category(category)
                .title(title)
                .content(content)
                .icon("🔬")
                .priority(priority)
                .build();
    }
    
    @Override
    public List<Tip> generateWarnings(CalculationContext context) {
        return new ArrayList<>();
    }
    
    @Override
    public boolean isApplicable(CalculationContext context) {
        return true;
    }
}
