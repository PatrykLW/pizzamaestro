package com.pizzamaestro.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Encja składnika - mąki lub wody.
 * Przechowuje parametry techniczne składników używanych w kalkulacjach.
 */
@Document(collection = "ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    
    @Id
    private String id;
    
    private IngredientType type;
    
    private String name;
    
    private String brand;
    
    private String description;
    
    private String country;
    
    private String imageUrl;
    
    private boolean verified; // zweryfikowany przez admina
    
    private boolean active;
    
    // ===== PARAMETRY DLA MĄKI =====
    
    private FlourParameters flourParameters;
    
    // ===== PARAMETRY DLA WODY =====
    
    private WaterParameters waterParameters;
    
    // ===== PARAMETRY DLA DROŻDŻY =====
    
    private YeastParameters yeastParameters;
    
    // ===== PARAMETRY DLA SOLI =====
    
    private SaltParameters saltParameters;
    
    // ===== PARAMETRY DLA INNYCH SKŁADNIKÓW =====
    
    private AdditionalIngredientParameters additionalParameters;
    
    /**
     * Typ składnika.
     */
    public enum IngredientType {
        FLOUR,
        WATER,
        YEAST,
        SALT,
        OIL,
        SUGAR,
        MILK_POWDER,
        MALT,
        OTHER
    }
    
    /**
     * Parametry drożdży.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YeastParameters {
        private YeastVariety yeastVariety;
        private double conversionFactor; // współczynnik konwersji względem świeżych (1.0 = świeże)
        private int shelfLifeDays; // termin ważności w dniach
        private boolean requiresRefrigeration;
        private Double optimalTempMin; // optymalna temp. fermentacji
        private Double optimalTempMax;
        private String activationNotes; // instrukcje aktywacji
        private String storageNotes; // instrukcje przechowywania
    }
    
    /**
     * Rodzaj drożdży.
     */
    @Getter
    public enum YeastVariety {
        FRESH("Świeże (prasowane)", "Najaktywniejsze, najlepszy smak. Przechowuj w lodówce.", 1.0),
        INSTANT_DRY("Instant suszone", "Dodawać bezpośrednio do mąki. Długi termin.", 0.33),
        ACTIVE_DRY("Aktywne suszone", "Wymagają aktywacji w ciepłej wodzie.", 0.40),
        SOURDOUGH("Zakwas", "Naturalna fermentacja, najdłuższy proces.", 0.0);
        
        private final String displayName;
        private final String description;
        private final double conversionFactor;
        
        YeastVariety(String displayName, String description, double conversionFactor) {
            this.displayName = displayName;
            this.description = description;
            this.conversionFactor = conversionFactor;
        }
    }
    
    /**
     * Parametry soli.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaltParameters {
        private SaltType saltType;
        private double sodiumContent; // zawartość sodu w g/100g
        private boolean isFlaky; // czy płatkowa (wpływa na odmierzanie)
        private Double grainSize; // rozmiar ziarna w mm
        private String origin; // pochodzenie
        private String flavorNotes; // notatki smakowe
    }
    
    /**
     * Rodzaj soli.
     */
    @Getter
    public enum SaltType {
        SEA_SALT("Sól morska", "Naturalna sól z wody morskiej"),
        HIMALAYAN("Sól himalajska", "Różowa sól z Pakistanu, bogata w minerały"),
        KOSHER("Sól koszerna", "Grube płatki, popularna w USA"),
        FLEUR_DE_SEL("Fleur de sel", "Luksusowa francuska sól ręcznie zbierana"),
        TABLE_SALT("Sól kuchenna", "Standardowa oczyszczona sól"),
        ROCK_SALT("Sól kamienna", "Sól wydobywana z kopalni");
        
        private final String displayName;
        private final String description;
        
        SaltType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }
    
    /**
     * Parametry mąki.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlourParameters {
        private FlourType flourType;
        private String grainType; // pszenna, żytnia, etc.
        private double proteinContent; // zawartość białka w %
        private Double strength; // siła W (opcjonalne)
        private Double extensibility; // rozciągliwość P/L (opcjonalne)
        private Double ashContent; // zawartość popiołu w %
        private double recommendedHydrationMin;
        private double recommendedHydrationMax;
        private List<PizzaStyle> recommendedStyles;
        private String notes;
    }
    
    /**
     * Typ mąki.
     */
    @Getter
    public enum FlourType {
        TYPE_00("Tipo 00", "Najdrobniejsza włoska mąka, idealna do pizzy neapolitańskiej"),
        TYPE_0("Tipo 0", "Włoska mąka, trochę grubsza od 00"),
        TYPE_1("Tipo 1", "Włoska mąka z większą zawartością otrębów"),
        TYPE_2("Tipo 2", "Włoska mąka półpełnoziarnista"),
        BREAD_FLOUR("Chlebowa", "Mocna mąka o wysokiej zawartości białka"),
        HIGH_GLUTEN("High-Gluten", "Bardzo mocna mąka 14%+ białka dla żuistej tekstury"),
        ALL_PURPOSE("Uniwersalna", "Standardowa mąka pszenna"),
        WHOLE_WHEAT("Pełnoziarnista", "Mąka z pełnego przemiału"),
        SEMOLINA("Semolina", "Mąka z pszenicy durum"),
        RYE("Żytnia", "Mąka żytnia"),
        SPELT("Orkiszowa", "Mąka z orkiszu"),
        MANITOBA("Manitoba", "Bardzo mocna mąka kanadyjska W=380+"),
        GLUTEN_FREE("Bezglutenowa", "Mąka bez glutenu dla osób z celiakią");
        
        private final String displayName;
        private final String description;
        
        FlourType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }
    
    /**
     * Parametry wody.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaterParameters {
        private double hardness; // twardość w mg/l CaCO3
        private HardnessLevel hardnessLevel;
        private double ph;
        private double mineralContent; // mineralizacja ogólna mg/l
        private double calcium; // Ca mg/l
        private double magnesium; // Mg mg/l
        private double sodium; // Na mg/l
        private double chloride; // Cl mg/l
        private String source; // źródło (kranowa, źródlana, etc.)
        private String notes;
    }
    
    /**
     * Poziom twardości wody.
     */
    @Getter
    public enum HardnessLevel {
        VERY_SOFT("Bardzo miękka", 0, 60),
        SOFT("Miękka", 60, 120),
        MEDIUM("Średnio twarda", 120, 180),
        HARD("Twarda", 180, 300),
        VERY_HARD("Bardzo twarda", 300, 1000);
        
        private final String displayName;
        private final int minHardness;
        private final int maxHardness;
        
        HardnessLevel(String displayName, int minHardness, int maxHardness) {
            this.displayName = displayName;
            this.minHardness = minHardness;
            this.maxHardness = maxHardness;
        }
        
        public static HardnessLevel fromHardness(double hardness) {
            for (HardnessLevel level : values()) {
                if (hardness >= level.minHardness && hardness < level.maxHardness) {
                    return level;
                }
            }
            return VERY_HARD;
        }
    }
    
    /**
     * Parametry dodatkowego składnika.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalIngredientParameters {
        private double defaultPercentage; // domyślny % względem mąki
        private double minPercentage;
        private double maxPercentage;
        private String unit; // jednostka (g, ml, etc.)
        private boolean affectsFermentation;
        private double fermentationImpact; // wpływ na fermentację (-1 do 1)
        private String usage; // jak używać
    }
}
