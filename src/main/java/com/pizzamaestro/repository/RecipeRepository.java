package com.pizzamaestro.repository;

import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repozytorium receptur/kalkulacji.
 */
@Repository
public interface RecipeRepository extends MongoRepository<Recipe, String> {
    
    // Receptury użytkownika
    List<Recipe> findByUserIdOrderByCreatedAtDesc(String userId);
    
    Page<Recipe> findByUserId(String userId, Pageable pageable);
    
    List<Recipe> findByUserIdAndFavoriteTrue(String userId);
    
    Optional<Recipe> findByIdAndUserId(String id, String userId);
    
    // Wyszukiwanie po stylu
    List<Recipe> findByUserIdAndPizzaStyle(String userId, PizzaStyle pizzaStyle);
    
    // Publiczne receptury
    Page<Recipe> findByIsPublicTrue(Pageable pageable);
    
    // Udostępnianie przez token
    Optional<Recipe> findByShareToken(String shareToken);
    
    @Query("{'isPublic': true, 'rating': {$gte: ?0}}")
    Page<Recipe> findPublicWithMinRating(int minRating, Pageable pageable);
    
    // Statystyki
    long countByUserId(String userId);
    
    long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime after);
    
    @Query(value = "{'userId': ?0}", count = true)
    long countUserRecipes(String userId);
    
    // Harmonogram - aktywne receptury z zaplanowanym pieczeniem
    @Query("{'userId': ?0, 'plannedBakeTime': {$gte: ?1, $lte: ?2}}")
    List<Recipe> findActiveRecipesWithPlannedBake(String userId, LocalDateTime from, LocalDateTime to);
    
    // Receptury z niezakończonymi krokami
    @Query("{'userId': ?0, 'fermentationSteps': {$elemMatch: {'completed': false, 'scheduledTime': {$lte: ?1}}}}")
    List<Recipe> findRecipesWithPendingSteps(String userId, LocalDateTime before);
    
    // Usuwanie
    void deleteByIdAndUserId(String id, String userId);
    
    // Najczęściej używane style
    @Query(value = "{'userId': ?0}", fields = "{'pizzaStyle': 1}")
    List<Recipe> findStylesByUserId(String userId);
}
