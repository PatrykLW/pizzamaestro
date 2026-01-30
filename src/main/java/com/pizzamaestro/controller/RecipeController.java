package com.pizzamaestro.controller;

import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.service.PdfExportService;
import com.pizzamaestro.service.RecipeService;
import com.pizzamaestro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler zarządzania recepturami użytkownika.
 */
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Receptury", description = "Zarządzanie zapisanymi recepturami")
@SecurityRequirement(name = "bearerAuth")
public class RecipeController {
    
    private final RecipeService recipeService;
    private final UserService userService;
    private final PdfExportService pdfExportService;
    
    /**
     * Pobiera wszystkie receptury użytkownika.
     */
    @GetMapping
    @Operation(summary = "Lista receptur użytkownika")
    public ResponseEntity<Page<Recipe>> getRecipes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        String userId = getUserId(userDetails);
        Page<Recipe> recipes = recipeService.findByUserId(userId, pageable);
        return ResponseEntity.ok(recipes);
    }
    
    /**
     * Pobiera ulubione receptury.
     */
    @GetMapping("/favorites")
    @Operation(summary = "Ulubione receptury")
    public ResponseEntity<List<Recipe>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        List<Recipe> favorites = recipeService.findFavorites(userId);
        return ResponseEntity.ok(favorites);
    }
    
    /**
     * Pobiera pojedynczą recepturę.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Szczegóły receptury")
    public ResponseEntity<Recipe> getRecipe(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        Recipe recipe = recipeService.findByIdAndUserId(id, userId);
        return ResponseEntity.ok(recipe);
    }
    
    /**
     * Aktualizuje recepturę.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Aktualizacja receptury")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable String id,
            @Valid @RequestBody Recipe updates,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        Recipe updated = recipeService.update(id, userId, updates);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Przełącza status ulubionej.
     */
    @PostMapping("/{id}/favorite")
    @Operation(summary = "Dodaj/usuń z ulubionych")
    public ResponseEntity<Recipe> toggleFavorite(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        Recipe recipe = recipeService.toggleFavorite(id, userId);
        return ResponseEntity.ok(recipe);
    }
    
    /**
     * Klonuje recepturę.
     */
    @PostMapping("/{id}/clone")
    @Operation(summary = "Kopiuj recepturę")
    public ResponseEntity<Recipe> cloneRecipe(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        Recipe clone = recipeService.clone(id, userId);
        return ResponseEntity.ok(clone);
    }
    
    /**
     * Oznacza krok jako ukończony.
     */
    @PostMapping("/{id}/steps/{stepNumber}/complete")
    @Operation(summary = "Oznacz krok jako wykonany")
    public ResponseEntity<Recipe> completeStep(
            @PathVariable String id,
            @PathVariable int stepNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        Recipe recipe = recipeService.completeStep(id, userId, stepNumber);
        return ResponseEntity.ok(recipe);
    }
    
    /**
     * Usuwa recepturę.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń recepturę")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        recipeService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Pobiera publiczne receptury (dla wszystkich).
     */
    @GetMapping("/public")
    @Operation(summary = "Publiczne receptury")
    public ResponseEntity<Page<Recipe>> getPublicRecipes(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<Recipe> recipes = recipeService.findPublicRecipes(pageable);
        return ResponseEntity.ok(recipes);
    }
    
    /**
     * Eksportuje przepis do PDF.
     */
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Eksportuj przepis do PDF")
    public ResponseEntity<byte[]> exportToPdf(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("📄 Eksport przepisu {} do PDF", id);
        
        String userId = getUserId(userDetails);
        Recipe recipe = recipeService.findByIdAndUserId(id, userId);
        
        try {
            byte[] pdfBytes = pdfExportService.generateRecipePdf(recipe);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "przepis-" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("❌ Błąd generowania PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Generuje link do udostępniania przepisu.
     */
    @PostMapping("/{id}/share")
    @Operation(summary = "Wygeneruj link do udostępniania przepisu")
    public ResponseEntity<ShareResponse> shareRecipe(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("📤 Generowanie linku do udostępniania przepisu: {}", id);
        
        String userId = getUserId(userDetails);
        String token = recipeService.generateShareLink(id, userId);
        
        // Zwróć URL do udostępniania
        String shareUrl = "/shared/" + token; // Frontend powinien obsłużyć tę ścieżkę
        
        return ResponseEntity.ok(new ShareResponse(token, shareUrl));
    }
    
    /**
     * Anuluje udostępnianie przepisu.
     */
    @DeleteMapping("/{id}/share")
    @Operation(summary = "Anuluj udostępnianie przepisu")
    public ResponseEntity<Void> revokeShare(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("🔒 Anulowanie udostępniania przepisu: {}", id);
        
        String userId = getUserId(userDetails);
        recipeService.revokeShareLink(id, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Pobiera przepis przez token udostępniania (publiczny).
     */
    @GetMapping("/shared/{token}")
    @Operation(summary = "Pobierz udostępniony przepis (publiczny)")
    public ResponseEntity<Recipe> getSharedRecipe(@PathVariable String token) {
        log.info("📥 Pobieranie udostępnionego przepisu: {}***", token.length() > 4 ? token.substring(0, 4) : "****");
        
        Recipe recipe = recipeService.findByShareToken(token);
        return ResponseEntity.ok(recipe);
    }
    
    // DTO dla odpowiedzi share
    public record ShareResponse(String token, String shareUrl) {}
    
    private String getUserId(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername()).getId();
    }
}
