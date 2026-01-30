package com.pizzamaestro.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca zapisaną recepturę/kalkulację ciasta na pizzę.
 * Przechowuje wszystkie parametry wejściowe oraz obliczone wyniki.
 */
@Document(collection = "recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    
    @Id
    private String id;
    
    @Version
    private Long version;
    
    @Indexed
    private String userId;
    
    private String name;
    
    private String description;
    
    @Builder.Default
    private boolean favorite = false;
    
    @Builder.Default
    private boolean isPublic = false;
    
    /**
     * Token do udostępniania przepisu przez link.
     * Generowany przy pierwszym udostępnieniu.
     */
    @Indexed(sparse = true)
    private String shareToken;
    
    /**
     * Data wygaśnięcia linku udostępniania (null = nie wygasa).
     */
    private LocalDateTime shareTokenExpiresAt;
    
    // ===== PARAMETRY WEJŚCIOWE =====
    
    @Indexed
    private PizzaStyle pizzaStyle;
    
    private int numberOfPizzas;
    
    private int ballWeight; // waga jednej kulki w gramach
    
    private double hydration; // nawodnienie w %
    
    private double saltPercentage; // sól w %
    
    private double oilPercentage; // oliwa/olej w %
    
    private double sugarPercentage; // cukier w %
    
    private YeastType yeastType;
    
    private Double yeastPercentage; // null = obliczane automatycznie
    
    private FermentationMethod fermentationMethod;
    
    private int totalFermentationHours;
    
    private double roomTemperature; // temperatura pokojowa w °C
    
    private double fridgeTemperature; // temperatura lodówki w °C
    
    private OvenType ovenType;
    
    private int ovenTemperature;
    
    // Preferment
    private boolean usePreferment;
    
    private PrefermentType prefermentType;
    
    private Double prefermentPercentage; // % mąki w preferment
    
    private Integer prefermentFermentationHours;
    
    // Wybrane składniki
    private String flourId; // ID wybranej mąki
    
    private String waterId; // ID wybranej wody
    
    @Builder.Default
    private List<AdditionalIngredient> additionalIngredients = new ArrayList<>();
    
    // ===== OBLICZONE WYNIKI =====
    
    private CalculatedRecipe calculatedRecipe;
    
    // ===== HARMONOGRAM =====
    
    @Builder.Default
    private List<FermentationStep> fermentationSteps = new ArrayList<>();
    
    private LocalDateTime plannedBakeTime;
    
    private LocalDateTime startTime;
    
    // ===== NOTATKI I OCENA =====
    
    private String notes;
    
    private Integer rating; // 1-5
    
    private String feedback; // notatki po wypieku
    
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    
    // ===== METADATA =====
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private String parentRecipeId; // jeśli to kopia/wersja innej receptury
    
    private int version;
    
    // ===== INNER CLASSES =====
    
    /**
     * Typ drożdży.
     */
    @Getter
    public enum YeastType {
        FRESH("Świeże", 1.0),
        INSTANT_DRY("Suszone instant", 0.33),
        ACTIVE_DRY("Suszone aktywne", 0.4),
        SOURDOUGH("Zakwas", 0.0); // zakwas wymaga innej kalkulacji
        
        private final String displayName;
        private final double conversionFactor; // przelicznik względem świeżych
        
        YeastType(String displayName, double conversionFactor) {
            this.displayName = displayName;
            this.conversionFactor = conversionFactor;
        }
    }
    
    /**
     * Metoda fermentacji.
     */
    @Getter
    public enum FermentationMethod {
        ROOM_TEMPERATURE("Temperatura pokojowa", "Całość fermentacji w temp. pokojowej"),
        COLD_FERMENTATION("Fermentacja chłodnicza", "Długie wyrastanie w lodówce"),
        MIXED("Mieszana", "Początek w temp. pokojowej, następnie lodówka"),
        SAME_DAY("Tego samego dnia", "Szybka fermentacja - pizza tego samego dnia");
        
        private final String displayName;
        private final String description;
        
        FermentationMethod(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }
    
    /**
     * Typ prefermentu.
     */
    @Getter
    public enum PrefermentType {
        POOLISH("Poolish", "Płynny preferment 1:1 mąka:woda, mało drożdży", 100.0),
        BIGA("Biga", "Sztywny preferment, niska hydratacja ~50-60%", 55.0),
        LIEVITO_MADRE("Lievito madre", "Włoski zakwas pszenny", 45.0);
        
        private final String displayName;
        private final String description;
        private final double hydration;
        
        PrefermentType(String displayName, String description, double hydration) {
            this.displayName = displayName;
            this.description = description;
            this.hydration = hydration;
        }
    }
    
    /**
     * Dodatkowy składnik.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalIngredient {
        private String ingredientId;
        private String name;
        private double percentage; // % względem mąki
        private double calculatedGrams;
    }
    
    /**
     * Obliczona receptura - wynik kalkulacji.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculatedRecipe {
        private double totalDoughWeight;
        private double flourGrams;
        private double waterGrams;
        private double saltGrams;
        private double yeastGrams;
        private double oilGrams;
        private double sugarGrams;
        
        // Podział mąk jeśli używamy mieszanki
        @Builder.Default
        private List<FlourPortion> flourPortions = new ArrayList<>();
        
        // Preferment jeśli używany
        private PrefermentRecipe prefermentRecipe;
        
        // Ciasto główne (bez prefermentu)
        private MainDoughRecipe mainDoughRecipe;
        
        // Baker's percentages
        private BakerPercentages bakerPercentages;
    }
    
    /**
     * Porcja mąki w mieszance.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlourPortion {
        private String flourId;
        private String flourName;
        private double percentage;
        private double grams;
    }
    
    /**
     * Receptura prefermentu.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrefermentRecipe {
        private PrefermentType type;
        private double flourGrams;
        private double waterGrams;
        private double yeastGrams;
        private int fermentationHours;
    }
    
    /**
     * Receptura głównego ciasta (po odjęciu prefermentu).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MainDoughRecipe {
        private double flourGrams;
        private double waterGrams;
        private double saltGrams;
        private double yeastGrams;
        private double oilGrams;
        private double sugarGrams;
    }
    
    /**
     * Procenty piekarskie.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BakerPercentages {
        private double flour; // zawsze 100%
        private double water;
        private double salt;
        private double yeast;
        private double oil;
        private double sugar;
    }
    
    /**
     * Krok w harmonogramie fermentacji.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FermentationStep {
        private int stepNumber;
        private StepType stepType;
        private String title;
        private String description;
        private LocalDateTime scheduledTime;
        private int durationMinutes;
        private double temperature;
        private boolean completed;
        private LocalDateTime completedAt;
        private boolean notificationSent;
    }
    
    /**
     * Typ kroku w harmonogramie.
     */
    public enum StepType {
        MIX_PREFERMENT,     // Przygotowanie prefermentu
        MIX_DOUGH,          // Mieszanie składników
        AUTOLYSE,           // Autoliza
        ADD_SALT,           // Dodanie soli
        KNEAD,              // Wyrabianie
        BULK_FERMENTATION,  // Fermentacja zbiorcza
        FOLD,               // Składanie ciasta
        DIVIDE,             // Dzielenie na kulki
        BALL_FORMING,       // Formowanie kulek
        COLD_PROOF,         // Fermentacja w lodówce
        ROOM_TEMP_PROOF,    // Fermentacja w temp. pokojowej
        REMOVE_FROM_FRIDGE, // Wyjęcie z lodówki
        FINAL_PROOF,        // Końcowy odpoczynek
        SHAPE,              // Rozciąganie/formowanie placka
        BAKE,               // Pieczenie
        CUSTOM              // Własny krok
    }
}
