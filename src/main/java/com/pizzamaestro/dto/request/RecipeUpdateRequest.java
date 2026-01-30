package com.pizzamaestro.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO do aktualizacji receptury.
 * Używamy DTO zamiast encji dla bezpieczeństwa (SonarCloud rule).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeUpdateRequest {
    
    @Size(max = 100, message = "Nazwa receptury nie może przekraczać 100 znaków")
    private String name;
    
    @Size(max = 500, message = "Opis nie może przekraczać 500 znaków")
    private String description;
    
    private Boolean favorite;
    
    private Boolean isPublic;
    
    @Size(max = 2000, message = "Notatki nie mogą przekraczać 2000 znaków")
    private String notes;
    
    private List<String> tags;
    
    private Integer rating;
    
    @Size(max = 1000, message = "Feedback nie może przekraczać 1000 znaków")
    private String feedback;
}
