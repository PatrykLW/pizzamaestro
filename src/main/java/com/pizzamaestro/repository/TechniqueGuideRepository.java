package com.pizzamaestro.repository;

import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.TechniqueGuide;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repozytorium przewodników po technikach.
 */
@Repository
public interface TechniqueGuideRepository extends MongoRepository<TechniqueGuide, String> {
    
    // Po kategorii
    List<TechniqueGuide> findByCategoryAndActiveTrue(TechniqueGuide.TechniqueCategory category);
    
    // Po slug (URL-friendly)
    Optional<TechniqueGuide> findBySlugAndActiveTrue(String slug);
    
    // Dla stylu pizzy
    @Query("{'recommendedForStyles': ?0, 'active': true}")
    List<TechniqueGuide> findByRecommendedStyle(PizzaStyle style);
    
    // Po poziomie trudności
    List<TechniqueGuide> findByDifficultyAndActiveTrue(TechniqueGuide.DifficultyLevel difficulty);
    
    // Darmowe (nie-premium)
    List<TechniqueGuide> findByPremiumFalseAndActiveTrue();
    
    // Premium
    List<TechniqueGuide> findByPremiumTrueAndActiveTrue();
    
    // Wszystkie aktywne
    List<TechniqueGuide> findByActiveTrueOrderByViewCountDesc();
    
    // Wyszukiwanie po tytule
    @Query("{'$or': [{'title': {$regex: ?0, $options: 'i'}}, {'shortDescription': {$regex: ?0, $options: 'i'}}], 'active': true}")
    List<TechniqueGuide> searchByTitleOrDescription(String query);
    
    // Powiązane techniki
    @Query("{'slug': {$in: ?0}, 'active': true}")
    List<TechniqueGuide> findRelatedTechniques(List<String> slugs);
    
    // Popularne (top N)
    List<TechniqueGuide> findTop10ByActiveTrueOrderByViewCountDesc();
}
