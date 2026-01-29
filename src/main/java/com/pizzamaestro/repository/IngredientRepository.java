package com.pizzamaestro.repository;

import com.pizzamaestro.model.Ingredient;
import com.pizzamaestro.model.PizzaStyle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repozytorium składników (mąki, wody, itp.).
 */
@Repository
public interface IngredientRepository extends MongoRepository<Ingredient, String> {
    
    // Wyszukiwanie po typie
    List<Ingredient> findByTypeAndActiveTrue(Ingredient.IngredientType type);
    
    List<Ingredient> findByTypeAndVerifiedTrueAndActiveTrue(Ingredient.IngredientType type);
    
    // Mąki
    @Query("{'type': 'FLOUR', 'active': true}")
    List<Ingredient> findAllFlours();
    
    @Query("{'type': 'FLOUR', 'active': true, 'flourParameters.flourType': ?0}")
    List<Ingredient> findFloursByType(Ingredient.FlourType flourType);
    
    @Query("{'type': 'FLOUR', 'active': true, 'flourParameters.recommendedStyles': ?0}")
    List<Ingredient> findFloursRecommendedForStyle(PizzaStyle style);
    
    @Query("{'type': 'FLOUR', 'active': true, 'flourParameters.proteinContent': {$gte: ?0, $lte: ?1}}")
    List<Ingredient> findFloursByProteinRange(double minProtein, double maxProtein);
    
    // Wody
    @Query("{'type': 'WATER', 'active': true}")
    List<Ingredient> findAllWaters();
    
    @Query("{'type': 'WATER', 'active': true, 'waterParameters.hardnessLevel': ?0}")
    List<Ingredient> findWatersByHardness(Ingredient.HardnessLevel hardnessLevel);
    
    // Wyszukiwanie po nazwie
    List<Ingredient> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    
    List<Ingredient> findByBrandContainingIgnoreCaseAndActiveTrue(String brand);
    
    // Sprawdzenie czy składnik istnieje
    boolean existsByNameAndBrandAndType(String name, String brand, Ingredient.IngredientType type);
    
    // Popularne składniki
    Optional<Ingredient> findFirstByTypeAndNameContainingIgnoreCase(Ingredient.IngredientType type, String name);
}
