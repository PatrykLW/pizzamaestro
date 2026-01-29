package com.pizzamaestro.controller;

import com.pizzamaestro.model.Ingredient;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler składników (mąki, wody, itp.).
 */
@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Składniki", description = "Baza mąk, wód i innych składników")
public class IngredientController {
    
    private final IngredientService ingredientService;
    
    /**
     * Pobiera wszystkie mąki (publiczny).
     */
    @GetMapping("/public/flours")
    @Operation(summary = "Lista mąk")
    public ResponseEntity<List<Ingredient>> getFlours() {
        return ResponseEntity.ok(ingredientService.getAllFlours());
    }
    
    /**
     * Pobiera mąki rekomendowane dla stylu.
     */
    @GetMapping("/public/flours/style/{style}")
    @Operation(summary = "Mąki rekomendowane dla stylu pizzy")
    public ResponseEntity<List<Ingredient>> getFloursForStyle(@PathVariable PizzaStyle style) {
        return ResponseEntity.ok(ingredientService.getFloursForStyle(style));
    }
    
    /**
     * Pobiera mąki po zawartości białka.
     */
    @GetMapping("/public/flours/protein")
    @Operation(summary = "Mąki o zadanej zawartości białka")
    public ResponseEntity<List<Ingredient>> getFloursByProtein(
            @RequestParam 
            @DecimalMin(value = "0.0", message = "Minimalna zawartość białka >= 0")
            @DecimalMax(value = "25.0", message = "Minimalna zawartość białka <= 25")
            @Parameter(description = "Minimalna zawartość białka w %")
            double min,
            
            @RequestParam 
            @DecimalMin(value = "0.0", message = "Maksymalna zawartość białka >= 0")
            @DecimalMax(value = "25.0", message = "Maksymalna zawartość białka <= 25")
            @Parameter(description = "Maksymalna zawartość białka w %")
            double max) {
        
        log.debug("🌾 Wyszukiwanie mąk: białko {}% - {}%", min, max);
        
        if (min > max) {
            throw new IllegalArgumentException("Minimalna wartość nie może być większa od maksymalnej");
        }
        
        List<Ingredient> flours = ingredientService.getFloursByProtein(min, max);
        log.debug("   Znaleziono {} mąk", flours.size());
        
        return ResponseEntity.ok(flours);
    }
    
    /**
     * Pobiera wszystkie wody (publiczny).
     */
    @GetMapping("/public/waters")
    @Operation(summary = "Lista wód")
    public ResponseEntity<List<Ingredient>> getWaters() {
        return ResponseEntity.ok(ingredientService.getAllWaters());
    }
    
    /**
     * Pobiera wody po twardości.
     */
    @GetMapping("/public/waters/hardness/{level}")
    @Operation(summary = "Wody o zadanej twardości")
    public ResponseEntity<List<Ingredient>> getWatersByHardness(
            @PathVariable Ingredient.HardnessLevel level) {
        return ResponseEntity.ok(ingredientService.getWatersByHardness(level));
    }
    
    /**
     * Pobiera wszystkie drożdże (publiczny).
     */
    @GetMapping("/public/yeasts")
    @Operation(summary = "Lista drożdży")
    public ResponseEntity<List<Ingredient>> getYeasts() {
        log.info("📦 Pobieranie listy drożdży");
        return ResponseEntity.ok(ingredientService.getAllYeasts());
    }
    
    /**
     * Pobiera wszystkie sole (publiczny).
     */
    @GetMapping("/public/salts")
    @Operation(summary = "Lista soli")
    public ResponseEntity<List<Ingredient>> getSalts() {
        log.info("📦 Pobieranie listy soli");
        return ResponseEntity.ok(ingredientService.getAllSalts());
    }
    
    /**
     * Pobiera rekomendowane składniki dla stylu pizzy.
     */
    @GetMapping("/public/recommendations/{style}")
    @Operation(summary = "Rekomendowane składniki dla stylu")
    public ResponseEntity<IngredientRecommendations> getRecommendations(@PathVariable PizzaStyle style) {
        log.info("🎯 Pobieranie rekomendacji dla stylu: {}", style);
        
        IngredientRecommendations recommendations = IngredientRecommendations.builder()
                .pizzaStyle(style)
                .recommendedFlours(ingredientService.getFloursForStyle(style))
                .allFlours(ingredientService.getAllFlours())
                .recommendedWaters(ingredientService.getRecommendedWatersForStyle(style))
                .allWaters(ingredientService.getAllWaters())
                .yeasts(ingredientService.getAllYeasts())
                .salts(ingredientService.getAllSalts())
                .build();
        
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Wyszukuje składniki.
     */
    @GetMapping("/public/search")
    @Operation(summary = "Wyszukiwanie składników")
    public ResponseEntity<List<Ingredient>> searchIngredients(
            @RequestParam 
            @NotBlank(message = "Fraza wyszukiwania nie może być pusta")
            @Size(min = 2, max = 100, message = "Fraza musi mieć od 2 do 100 znaków")
            @Parameter(description = "Fraza wyszukiwania (min. 2 znaki)")
            String query) {
        
        log.debug("🔍 Wyszukiwanie składników: '{}'", query);
        List<Ingredient> results = ingredientService.searchByName(query);
        log.debug("   Znaleziono {} wyników", results.size());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Pobiera szczegóły składnika.
     */
    @GetMapping("/public/{id}")
    @Operation(summary = "Szczegóły składnika")
    public ResponseEntity<Ingredient> getIngredient(
            @PathVariable 
            @NotBlank(message = "ID składnika nie może być puste")
            @Size(max = 50, message = "ID zbyt długie")
            @Parameter(description = "ID składnika")
            String id) {
        
        log.debug("📦 Pobieranie składnika: {}", id);
        return ResponseEntity.ok(ingredientService.findById(id));
    }
    
    // DTO dla rekomendacji
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IngredientRecommendations {
        private PizzaStyle pizzaStyle;
        private List<Ingredient> recommendedFlours;
        private List<Ingredient> allFlours;
        private List<Ingredient> recommendedWaters;
        private List<Ingredient> allWaters;
        private List<Ingredient> yeasts;
        private List<Ingredient> salts;
    }
}
