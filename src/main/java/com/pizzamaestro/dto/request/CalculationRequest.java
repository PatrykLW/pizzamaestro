package com.pizzamaestro.dto.request;

import com.pizzamaestro.model.MixerType;
import com.pizzamaestro.model.OvenType;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO żądania kalkulacji receptury ciasta.
 * 
 * Wspiera zarówno podstawowe jak i zaawansowane kalkulacje z parametrami:
 * - DDT (Desired Dough Temperature)
 * - Parametry mąki (siła W, białko)
 * - Parametry wody (twardość, pH)
 * - Typ miksera (dla obliczeń tarcia)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequest {
    
    // ========================================
    // PODSTAWOWE PARAMETRY
    // ========================================
    
    @NotNull(message = "Styl pizzy jest wymagany")
    private PizzaStyle pizzaStyle;
    
    @Min(value = 1, message = "Liczba pizz musi być co najmniej 1")
    @Max(value = 100, message = "Liczba pizz nie może przekroczyć 100")
    private int numberOfPizzas;
    
    @Min(value = 100, message = "Waga kulki musi wynosić co najmniej 100g")
    @Max(value = 1000, message = "Waga kulki nie może przekroczyć 1000g")
    private int ballWeight;
    
    @DecimalMin(value = "45.0", message = "Nawodnienie musi wynosić co najmniej 45%")
    @DecimalMax(value = "95.0", message = "Nawodnienie nie może przekroczyć 95%")
    private double hydration;
    
    @DecimalMin(value = "1.0", message = "Zawartość soli musi wynosić co najmniej 1%")
    @DecimalMax(value = "5.0", message = "Zawartość soli nie może przekroczyć 5%")
    private double saltPercentage;
    
    @DecimalMin(value = "0.0", message = "Zawartość oliwy nie może być ujemna")
    @DecimalMax(value = "15.0", message = "Zawartość oliwy nie może przekroczyć 15%")
    private double oilPercentage;
    
    @DecimalMin(value = "0.0", message = "Zawartość cukru nie może być ujemna")
    @DecimalMax(value = "10.0", message = "Zawartość cukru nie może przekroczyć 10%")
    private double sugarPercentage;
    
    // ========================================
    // DROŻDŻE I FERMENTACJA
    // ========================================
    
    @NotNull(message = "Typ drożdży jest wymagany")
    private Recipe.YeastType yeastType;
    
    // Jeśli null - drożdże będą obliczone automatycznie na podstawie czasu/temperatury
    @DecimalMin(value = "0.01", message = "Ilość drożdży musi być dodatnia")
    @DecimalMax(value = "5.0", message = "Ilość drożdży nie może przekroczyć 5%")
    private Double yeastPercentage;
    
    @NotNull(message = "Metoda fermentacji jest wymagana")
    private Recipe.FermentationMethod fermentationMethod;
    
    @Min(value = 1, message = "Czas fermentacji musi wynosić co najmniej 1 godzinę")
    @Max(value = 168, message = "Czas fermentacji nie może przekroczyć 168 godzin (7 dni)")
    private int totalFermentationHours;
    
    @DecimalMin(value = "10.0", message = "Temperatura pokojowa musi wynosić co najmniej 10°C")
    @DecimalMax(value = "40.0", message = "Temperatura pokojowa nie może przekroczyć 40°C")
    private Double roomTemperature;
    
    @DecimalMin(value = "0.0", message = "Temperatura lodówki musi wynosić co najmniej 0°C")
    @DecimalMax(value = "10.0", message = "Temperatura lodówki nie może przekroczyć 10°C")
    private Double fridgeTemperature;
    
    // ========================================
    // ZAAWANSOWANE PARAMETRY DDT
    // ========================================
    
    /** Typ miksera - wpływa na obliczenia tarcia w DDT */
    private MixerType mixerType;
    
    /** Temperatura mąki w °C (domyślnie = temp. pokojowa) */
    @DecimalMin(value = "5.0", message = "Temperatura mąki musi wynosić co najmniej 5°C")
    @DecimalMax(value = "35.0", message = "Temperatura mąki nie może przekroczyć 35°C")
    private Double flourTemperature;
    
    /** Temperatura prefermentu w °C (dla metody 4-czynnikowej DDT) */
    @DecimalMin(value = "5.0", message = "Temperatura prefermentu musi wynosić co najmniej 5°C")
    @DecimalMax(value = "35.0", message = "Temperatura prefermentu nie może przekroczyć 35°C")
    private Double prefermentTemperature;
    
    // ========================================
    // PARAMETRY MĄKI
    // ========================================
    
    /** Siła mąki W (np. 260 dla Caputo Pizzeria) */
    @DecimalMin(value = "100.0", message = "Siła W musi wynosić co najmniej 100")
    @DecimalMax(value = "450.0", message = "Siła W nie może przekroczyć 450")
    private Double flourStrength;
    
    /** Zawartość białka w % (np. 12.5) */
    @DecimalMin(value = "5.0", message = "Zawartość białka musi wynosić co najmniej 5%")
    @DecimalMax(value = "18.0", message = "Zawartość białka nie może przekroczyć 18%")
    private Double flourProtein;
    
    // ========================================
    // PARAMETRY WODY
    // ========================================
    
    /** Twardość wody w mg/l CaCO3 */
    @DecimalMin(value = "0.0", message = "Twardość wody nie może być ujemna")
    @DecimalMax(value = "1000.0", message = "Twardość wody nie może przekroczyć 1000 mg/l")
    private Double waterHardness;
    
    /** pH wody */
    @DecimalMin(value = "5.0", message = "pH wody musi wynosić co najmniej 5.0")
    @DecimalMax(value = "9.0", message = "pH wody nie może przekroczyć 9.0")
    private Double waterPh;
    
    // ========================================
    // PIEC I PIECZENIE
    // ========================================
    
    private OvenType ovenType;
    
    private Integer ovenTemperature;
    
    // Preferment
    private boolean usePreferment;
    
    private Recipe.PrefermentType prefermentType;
    
    @DecimalMin(value = "10.0", message = "Preferment musi stanowić co najmniej 10% mąki")
    @DecimalMax(value = "50.0", message = "Preferment nie może przekroczyć 50% mąki")
    private Double prefermentPercentage;
    
    private Integer prefermentFermentationHours;
    
    // Wybrane składniki
    private String flourId;
    
    private String waterId;
    
    private List<AdditionalIngredientRequest> additionalIngredients;
    
    // Harmonogram
    private LocalDateTime plannedBakeTime;
    
    private boolean generateSchedule;
    
    // Opcje zapisu
    private boolean saveRecipe;
    
    private String recipeName;
    
    private String recipeDescription;
    
    /**
     * Żądanie dodatkowego składnika.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalIngredientRequest {
        private String ingredientId;
        private String name;
        private double percentage;
    }
}
