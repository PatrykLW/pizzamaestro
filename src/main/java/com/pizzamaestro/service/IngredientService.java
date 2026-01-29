package com.pizzamaestro.service;

import com.pizzamaestro.exception.ResourceNotFoundException;
import com.pizzamaestro.model.Ingredient;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serwis zarządzania składnikami (mąki, wody, itp.).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientService {
    
    private final IngredientRepository ingredientRepository;
    
    /**
     * Pobiera wszystkie mąki.
     */
    @Cacheable("flours")
    public List<Ingredient> getAllFlours() {
        return ingredientRepository.findAllFlours();
    }
    
    /**
     * Pobiera mąki zweryfikowane.
     */
    public List<Ingredient> getVerifiedFlours() {
        return ingredientRepository.findByTypeAndVerifiedTrueAndActiveTrue(Ingredient.IngredientType.FLOUR);
    }
    
    /**
     * Pobiera mąki rekomendowane dla stylu.
     */
    public List<Ingredient> getFloursForStyle(PizzaStyle style) {
        return ingredientRepository.findFloursRecommendedForStyle(style);
    }
    
    /**
     * Pobiera mąki po zawartości białka.
     */
    public List<Ingredient> getFloursByProtein(double minProtein, double maxProtein) {
        return ingredientRepository.findFloursByProteinRange(minProtein, maxProtein);
    }
    
    /**
     * Pobiera wszystkie wody.
     */
    @Cacheable("waters")
    public List<Ingredient> getAllWaters() {
        return ingredientRepository.findAllWaters();
    }
    
    /**
     * Pobiera wody po twardości.
     */
    public List<Ingredient> getWatersByHardness(Ingredient.HardnessLevel hardnessLevel) {
        return ingredientRepository.findWatersByHardness(hardnessLevel);
    }
    
    /**
     * Pobiera wszystkie drożdże.
     */
    @Cacheable("yeasts")
    public List<Ingredient> getAllYeasts() {
        log.info("📦 Pobieranie wszystkich drożdży z bazy");
        return ingredientRepository.findByTypeAndActiveTrue(Ingredient.IngredientType.YEAST);
    }
    
    /**
     * Pobiera wszystkie sole.
     */
    @Cacheable("salts")
    public List<Ingredient> getAllSalts() {
        log.info("📦 Pobieranie wszystkich soli z bazy");
        return ingredientRepository.findByTypeAndActiveTrue(Ingredient.IngredientType.SALT);
    }
    
    /**
     * Pobiera wody rekomendowane dla stylu pizzy.
     * Dla pizzy neapolitańskiej - miękka woda
     * Dla NY style - twarda woda (NYC style)
     */
    public List<Ingredient> getRecommendedWatersForStyle(PizzaStyle style) {
        log.info("🔍 Szukam rekomendowanych wód dla stylu: {}", style);
        
        // NYC style pizza wymaga twardej wody
        if (style == PizzaStyle.NEW_YORK) {
            return ingredientRepository.findWatersByHardness(Ingredient.HardnessLevel.HARD);
        }
        
        // Dla większości stylów - miękka do średniej
        List<Ingredient> softWaters = ingredientRepository.findWatersByHardness(Ingredient.HardnessLevel.SOFT);
        List<Ingredient> mediumWaters = ingredientRepository.findWatersByHardness(Ingredient.HardnessLevel.MEDIUM);
        
        java.util.ArrayList<Ingredient> result = new java.util.ArrayList<>();
        result.addAll(softWaters);
        result.addAll(mediumWaters);
        return result;
    }
    
    /**
     * Pobiera składnik po ID.
     */
    public Ingredient findById(String id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Składnik nie znaleziony"));
    }
    
    /**
     * Wyszukuje składniki po nazwie.
     */
    public List<Ingredient> searchByName(String name) {
        return ingredientRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }
    
    /**
     * Dodaje nowy składnik (admin).
     */
    public Ingredient addIngredient(Ingredient ingredient) {
        ingredient.setActive(true);
        ingredient.setVerified(false);
        return ingredientRepository.save(ingredient);
    }
    
    /**
     * Aktualizuje składnik (admin).
     */
    public Ingredient updateIngredient(String id, Ingredient updates) {
        Ingredient ingredient = findById(id);
        
        if (updates.getName() != null) ingredient.setName(updates.getName());
        if (updates.getBrand() != null) ingredient.setBrand(updates.getBrand());
        if (updates.getDescription() != null) ingredient.setDescription(updates.getDescription());
        if (updates.getFlourParameters() != null) ingredient.setFlourParameters(updates.getFlourParameters());
        if (updates.getWaterParameters() != null) ingredient.setWaterParameters(updates.getWaterParameters());
        
        return ingredientRepository.save(ingredient);
    }
    
    /**
     * Usuwa składnik (soft delete).
     */
    public void deleteIngredient(String id) {
        Ingredient ingredient = findById(id);
        ingredient.setActive(false);
        ingredientRepository.save(ingredient);
    }
    
    /**
     * Weryfikuje składnik (admin).
     */
    public Ingredient verifyIngredient(String id) {
        Ingredient ingredient = findById(id);
        ingredient.setVerified(true);
        return ingredientRepository.save(ingredient);
    }
}
