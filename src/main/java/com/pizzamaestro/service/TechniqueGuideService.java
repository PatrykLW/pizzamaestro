package com.pizzamaestro.service;

import com.pizzamaestro.exception.ResourceNotFoundException;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.TechniqueGuide;
import com.pizzamaestro.repository.TechniqueGuideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serwis zarządzania bazą wiedzy - przewodnikami po technikach.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TechniqueGuideService {
    
    private final TechniqueGuideRepository repository;
    
    /**
     * Pobiera wszystkie aktywne przewodniki.
     */
    @Cacheable("guides")
    public List<TechniqueGuide> getAllGuides() {
        log.info("📚 Pobieranie wszystkich przewodników");
        return repository.findByActiveTrueOrderByViewCountDesc();
    }
    
    /**
     * Pobiera przewodniki po kategorii.
     */
    @Cacheable(value = "guides", key = "#category")
    public List<TechniqueGuide> getByCategory(TechniqueGuide.TechniqueCategory category) {
        log.info("📚 Pobieranie przewodników dla kategorii: {}", category);
        return repository.findByCategoryAndActiveTrue(category);
    }
    
    /**
     * Pobiera przewodnik po slug.
     */
    @Transactional
    public TechniqueGuide getBySlug(String slug) {
        log.info("📖 Pobieranie przewodnika: {}", slug);
        TechniqueGuide guide = repository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Przewodnik nie znaleziony: " + slug));
        
        // Zwiększ licznik wyświetleń
        guide.setViewCount(guide.getViewCount() + 1);
        repository.save(guide);
        
        return guide;
    }
    
    /**
     * Pobiera przewodniki dla stylu pizzy.
     */
    public List<TechniqueGuide> getForStyle(PizzaStyle style) {
        log.info("📚 Pobieranie przewodników dla stylu: {}", style);
        return repository.findByRecommendedStyle(style);
    }
    
    /**
     * Pobiera przewodniki po poziomie trudności.
     */
    public List<TechniqueGuide> getByDifficulty(TechniqueGuide.DifficultyLevel difficulty) {
        log.info("📚 Pobieranie przewodników dla poziomu: {}", difficulty);
        return repository.findByDifficultyAndActiveTrue(difficulty);
    }
    
    /**
     * Pobiera darmowe przewodniki (dla FREE users).
     */
    @Cacheable("freeGuides")
    public List<TechniqueGuide> getFreeGuides() {
        log.info("📚 Pobieranie darmowych przewodników");
        return repository.findByPremiumFalseAndActiveTrue();
    }
    
    /**
     * Wyszukuje przewodniki.
     */
    public List<TechniqueGuide> search(String query) {
        log.info("🔍 Wyszukiwanie przewodników: {}", query);
        return repository.searchByTitleOrDescription(query);
    }
    
    /**
     * Pobiera popularne przewodniki.
     */
    @Cacheable("popularGuides")
    public List<TechniqueGuide> getPopular() {
        log.info("📚 Pobieranie popularnych przewodników");
        return repository.findTop10ByActiveTrueOrderByViewCountDesc();
    }
    
    /**
     * Pobiera powiązane przewodniki.
     */
    public List<TechniqueGuide> getRelated(String slug) {
        TechniqueGuide guide = getBySlug(slug);
        if (guide.getRelatedTechniques() == null || guide.getRelatedTechniques().isEmpty()) {
            return List.of();
        }
        return repository.findRelatedTechniques(guide.getRelatedTechniques());
    }
    
    /**
     * Dodaje nowy przewodnik (admin).
     */
    @CacheEvict(value = {"guides", "freeGuides", "popularGuides"}, allEntries = true)
    public TechniqueGuide create(TechniqueGuide guide) {
        log.info("➕ Tworzenie przewodnika: {}", guide.getTitle());
        guide.setActive(true);
        guide.setViewCount(0);
        return repository.save(guide);
    }
    
    /**
     * Aktualizuje przewodnik (admin).
     */
    @CacheEvict(value = {"guides", "freeGuides", "popularGuides"}, allEntries = true)
    public TechniqueGuide update(String id, TechniqueGuide updates) {
        log.info("✏️ Aktualizacja przewodnika: {}", id);
        TechniqueGuide guide = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przewodnik nie znaleziony"));
        
        if (updates.getTitle() != null) guide.setTitle(updates.getTitle());
        if (updates.getShortDescription() != null) guide.setShortDescription(updates.getShortDescription());
        if (updates.getFullDescription() != null) guide.setFullDescription(updates.getFullDescription());
        if (updates.getSteps() != null) guide.setSteps(updates.getSteps());
        if (updates.getProTips() != null) guide.setProTips(updates.getProTips());
        if (updates.getCommonMistakes() != null) guide.setCommonMistakes(updates.getCommonMistakes());
        
        return repository.save(guide);
    }
    
    /**
     * Usuwa przewodnik (soft delete).
     */
    @CacheEvict(value = {"guides", "freeGuides", "popularGuides"}, allEntries = true)
    public void delete(String id) {
        log.info("🗑️ Usuwanie przewodnika: {}", id);
        TechniqueGuide guide = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Przewodnik nie znaleziony"));
        guide.setActive(false);
        repository.save(guide);
    }
}
