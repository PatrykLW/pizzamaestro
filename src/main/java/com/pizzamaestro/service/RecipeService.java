package com.pizzamaestro.service;

import com.pizzamaestro.dto.request.CalculationRequest;
import com.pizzamaestro.dto.request.RecipeUpdateRequest;
import com.pizzamaestro.dto.response.CalculationResponse;
import com.pizzamaestro.exception.ResourceNotFoundException;
import com.pizzamaestro.exception.UnauthorizedException;
import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis zarządzania recepturami/kalkulacjami.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    private final DoughCalculatorService calculatorService;
    
    /**
     * Tworzy nową kalkulację i opcjonalnie zapisuje jako recepturę.
     * 
     * @throws IllegalArgumentException gdy request lub userId jest null
     */
    @Transactional
    public CalculationResponse calculateAndSave(CalculationRequest request, String userId) {
        // Walidacja wejścia
        if (request == null) {
            log.error("❌ calculateAndSave: request jest null");
            throw new IllegalArgumentException("Request nie może być null");
        }
        if (userId == null || userId.trim().isEmpty()) {
            log.error("❌ calculateAndSave: userId jest null lub pusty");
            throw new IllegalArgumentException("UserId nie może być null lub pusty");
        }
        
        log.info("📝 Kalkulacja dla użytkownika {}, styl: {}", userId, request.getPizzaStyle());
        
        try {
            // Wykonaj kalkulację
            CalculationResponse response = calculatorService.calculate(request);
        
        // Zapisz jeśli wymagane
        if (request.isSaveRecipe()) {
            Recipe recipe = createRecipeFromRequest(request, userId);
            recipe.setCalculatedRecipe(toCalculatedRecipe(response));
            recipe.setFermentationSteps(toFermentationSteps(response.getSchedule()));
            
            Recipe savedRecipe = recipeRepository.save(recipe);
            response.setRecipeId(savedRecipe.getId());
            
            log.info("✅ Zapisano recepturę: {}", savedRecipe.getId());
        }
        
            log.info("✅ Kalkulacja zakończona pomyślnie");
            return response;
        } catch (Exception e) {
            log.error("❌ Błąd podczas kalkulacji: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Pobiera recepturę po ID.
     */
    @Transactional(readOnly = true)
    public Recipe findById(String id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptura nie znaleziona"));
    }
    
    /**
     * Pobiera recepturę użytkownika po ID.
     */
    @Transactional(readOnly = true)
    public Recipe findByIdAndUserId(String id, String userId) {
        return recipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Receptura nie znaleziona"));
    }
    
    /**
     * Pobiera wszystkie receptury użytkownika.
     */
    @Transactional(readOnly = true)
    public List<Recipe> findByUserId(String userId) {
        return recipeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Pobiera receptury użytkownika z paginacją.
     */
    @Transactional(readOnly = true)
    public Page<Recipe> findByUserId(String userId, Pageable pageable) {
        return recipeRepository.findByUserId(userId, pageable);
    }
    
    /**
     * Pobiera ulubione receptury użytkownika.
     */
    @Transactional(readOnly = true)
    public List<Recipe> findFavorites(String userId) {
        return recipeRepository.findByUserIdAndFavoriteTrue(userId);
    }
    
    /**
     * Aktualizuje recepturę.
     */
    @Transactional
    public Recipe update(String id, String userId, RecipeUpdateRequest updates) {
        Recipe recipe = findByIdAndUserId(id, userId);
        
        if (updates.getName() != null) recipe.setName(updates.getName());
        if (updates.getDescription() != null) recipe.setDescription(updates.getDescription());
        if (updates.getNotes() != null) recipe.setNotes(updates.getNotes());
        if (updates.getRating() != null) recipe.setRating(updates.getRating());
        if (updates.getFeedback() != null) recipe.setFeedback(updates.getFeedback());
        if (updates.getTags() != null) recipe.setTags(updates.getTags());
        
        if (updates.getFavorite() != null) recipe.setFavorite(updates.getFavorite());
        if (updates.getIsPublic() != null) recipe.setPublic(updates.getIsPublic());
        
        return recipeRepository.save(recipe);
    }
    
    /**
     * Oznacza recepturę jako ulubioną.
     */
    @Transactional
    public Recipe toggleFavorite(String id, String userId) {
        Recipe recipe = findByIdAndUserId(id, userId);
        recipe.setFavorite(!recipe.isFavorite());
        return recipeRepository.save(recipe);
    }
    
    /**
     * Usuwa recepturę.
     */
    @Transactional
    public void delete(String id, String userId) {
        Recipe recipe = findByIdAndUserId(id, userId);
        recipeRepository.delete(recipe);
        log.info("Usunięto recepturę: {}", id);
    }
    
    /**
     * Klonuje recepturę.
     */
    @Transactional
    public Recipe clone(String id, String userId) {
        Recipe original = findByIdAndUserId(id, userId);
        
        Recipe clone = Recipe.builder()
                .userId(userId)
                .name(original.getName() + " (kopia)")
                .description(original.getDescription())
                .pizzaStyle(original.getPizzaStyle())
                .numberOfPizzas(original.getNumberOfPizzas())
                .ballWeight(original.getBallWeight())
                .hydration(original.getHydration())
                .saltPercentage(original.getSaltPercentage())
                .oilPercentage(original.getOilPercentage())
                .sugarPercentage(original.getSugarPercentage())
                .yeastType(original.getYeastType())
                .yeastPercentage(original.getYeastPercentage())
                .fermentationMethod(original.getFermentationMethod())
                .totalFermentationHours(original.getTotalFermentationHours())
                .roomTemperature(original.getRoomTemperature())
                .fridgeTemperature(original.getFridgeTemperature())
                .ovenType(original.getOvenType())
                .ovenTemperature(original.getOvenTemperature())
                .usePreferment(original.isUsePreferment())
                .prefermentType(original.getPrefermentType())
                .prefermentPercentage(original.getPrefermentPercentage())
                .prefermentFermentationHours(original.getPrefermentFermentationHours())
                .flourId(original.getFlourId())
                .waterId(original.getWaterId())
                .additionalIngredients(new ArrayList<>(original.getAdditionalIngredients()))
                .calculatedRecipe(original.getCalculatedRecipe())
                .parentRecipeId(original.getId())
                .version(1)
                .build();
        
        return recipeRepository.save(clone);
    }
    
    /**
     * Aktualizuje status kroku fermentacji.
     * 
     * @throws IllegalArgumentException gdy recipeId/userId jest null lub stepNumber nieprawidłowy
     */
    @Transactional
    public Recipe completeStep(String recipeId, String userId, int stepNumber) {
        if (recipeId == null || recipeId.trim().isEmpty()) {
            log.error("❌ completeStep: recipeId jest null lub pusty");
            throw new IllegalArgumentException("RecipeId nie może być null lub pusty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            log.error("❌ completeStep: userId jest null lub pusty");
            throw new IllegalArgumentException("UserId nie może być null lub pusty");
        }
        if (stepNumber <= 0) {
            log.error("❌ completeStep: nieprawidłowy stepNumber: {}", stepNumber);
            throw new IllegalArgumentException("StepNumber musi być większy od 0");
        }
        
        log.debug("✓ Oznaczanie kroku {} jako ukończony dla receptury: {}", stepNumber, recipeId);
        
        Recipe recipe = findByIdAndUserId(recipeId, userId);
        
        List<Recipe.FermentationStep> steps = recipe.getFermentationSteps();
        if (steps == null || steps.isEmpty()) {
            log.warn("⚠️ completeStep: brak kroków fermentacji dla receptury: {}", recipeId);
            return recipe;
        }
        
        boolean stepFound = false;
        for (Recipe.FermentationStep step : steps) {
            if (step.getStepNumber() == stepNumber) {
                step.setCompleted(true);
                step.setCompletedAt(LocalDateTime.now());
                stepFound = true;
                log.info("✅ Oznaczono krok {} jako ukończony dla receptury: {}", stepNumber, recipeId);
                break;
            }
        }
        
        if (!stepFound) {
            log.warn("⚠️ completeStep: nie znaleziono kroku {} dla receptury: {}", stepNumber, recipeId);
        }
        
        return recipeRepository.save(recipe);
    }
    
    /**
     * Pobiera publiczne receptury.
     */
    public Page<Recipe> findPublicRecipes(Pageable pageable) {
        return recipeRepository.findByIsPublicTrue(pageable);
    }
    
    // ===== UDOSTĘPNIANIE =====
    
    /**
     * Generuje lub zwraca istniejący link do udostępniania przepisu.
     */
    @Transactional
    public String generateShareLink(String recipeId, String userId) {
        Recipe recipe = findByIdAndUserId(recipeId, userId);
        
        // Jeśli już ma token, zwróć go
        if (recipe.getShareToken() != null) {
            log.info("📤 Zwracam istniejący token udostępniania dla przepisu: {}", recipeId);
            return recipe.getShareToken();
        }
        
        // Wygeneruj nowy token
        String token = java.util.UUID.randomUUID().toString().replace("-", "")
                .substring(0, com.pizzamaestro.constants.CalculatorConstants.SHARE_TOKEN_LENGTH);
        recipe.setShareToken(token);
        recipe.setShareTokenExpiresAt(null); // Nie wygasa domyślnie
        
        recipeRepository.save(recipe);
        
        log.info("📤 Wygenerowano token udostępniania {}*** dla przepisu: {}", token.substring(0, 4), recipeId);
        return token;
    }
    
    /**
     * Usuwa token udostępniania (anuluje link).
     */
    @Transactional
    public void revokeShareLink(String recipeId, String userId) {
        Recipe recipe = findByIdAndUserId(recipeId, userId);
        
        recipe.setShareToken(null);
        recipe.setShareTokenExpiresAt(null);
        
        recipeRepository.save(recipe);
        
        log.info("🔒 Anulowano udostępnianie przepisu: {}", recipeId);
    }
    
    /**
     * Pobiera przepis przez token udostępniania (publiczny dostęp).
     */
    public Recipe findByShareToken(String shareToken) {
        if (shareToken == null || shareToken.isEmpty()) {
            throw new IllegalArgumentException("Token udostępniania jest wymagany");
        }
        
        return recipeRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException("Przepis nie znaleziony lub link wygasł"));
    }
    
    // ===== HELPER METHODS =====
    
    private Recipe createRecipeFromRequest(CalculationRequest request, String userId) {
        List<Recipe.AdditionalIngredient> additionalIngredients = new ArrayList<>();
        if (request.getAdditionalIngredients() != null) {
            for (CalculationRequest.AdditionalIngredientRequest ai : request.getAdditionalIngredients()) {
                additionalIngredients.add(Recipe.AdditionalIngredient.builder()
                        .ingredientId(ai.getIngredientId())
                        .name(ai.getName())
                        .percentage(ai.getPercentage())
                        .build());
            }
        }
        
        return Recipe.builder()
                .userId(userId)
                .name(request.getRecipeName() != null ? request.getRecipeName() : 
                        "Pizza " + request.getPizzaStyle().getDisplayName())
                .description(request.getRecipeDescription())
                .pizzaStyle(request.getPizzaStyle())
                .numberOfPizzas(request.getNumberOfPizzas())
                .ballWeight(request.getBallWeight())
                .hydration(request.getHydration())
                .saltPercentage(request.getSaltPercentage())
                .oilPercentage(request.getOilPercentage())
                .sugarPercentage(request.getSugarPercentage())
                .yeastType(request.getYeastType())
                .yeastPercentage(request.getYeastPercentage())
                .fermentationMethod(request.getFermentationMethod())
                .totalFermentationHours(request.getTotalFermentationHours())
                .roomTemperature(request.getRoomTemperature())
                .fridgeTemperature(request.getFridgeTemperature())
                .ovenType(request.getOvenType())
                .ovenTemperature(request.getOvenTemperature())
                .usePreferment(request.isUsePreferment())
                .prefermentType(request.getPrefermentType())
                .prefermentPercentage(request.getPrefermentPercentage())
                .prefermentFermentationHours(request.getPrefermentFermentationHours())
                .flourId(request.getFlourId())
                .waterId(request.getWaterId())
                .additionalIngredients(additionalIngredients)
                .plannedBakeTime(request.getPlannedBakeTime())
                .version(1)
                .build();
    }
    
    private Recipe.CalculatedRecipe toCalculatedRecipe(CalculationResponse response) {
        CalculationResponse.IngredientsResult ing = response.getIngredients();
        CalculationResponse.BakerPercentagesResult bp = response.getBakerPercentages();
        
        return Recipe.CalculatedRecipe.builder()
                .totalDoughWeight(ing.getTotalDoughWeight())
                .flourGrams(ing.getFlourGrams())
                .waterGrams(ing.getWaterGrams())
                .saltGrams(ing.getSaltGrams())
                .yeastGrams(ing.getYeastGrams())
                .oilGrams(ing.getOilGrams())
                .sugarGrams(ing.getSugarGrams())
                .bakerPercentages(Recipe.BakerPercentages.builder()
                        .flour(bp.getFlour())
                        .water(bp.getWater())
                        .salt(bp.getSalt())
                        .yeast(bp.getYeast())
                        .oil(bp.getOil())
                        .sugar(bp.getSugar())
                        .build())
                .build();
    }
    
    private List<Recipe.FermentationStep> toFermentationSteps(List<CalculationResponse.ScheduleStep> schedule) {
        if (schedule == null) return new ArrayList<>();
        
        return schedule.stream()
                .map(step -> Recipe.FermentationStep.builder()
                        .stepNumber(step.getStepNumber())
                        .stepType(step.getStepType())
                        .title(step.getTitle())
                        .description(step.getDescription())
                        .scheduledTime(step.getScheduledTime())
                        .durationMinutes(step.getDurationMinutes())
                        .temperature(step.getTemperature())
                        .completed(false)
                        .notificationSent(false)
                        .build())
                .collect(Collectors.toList());
    }
}
