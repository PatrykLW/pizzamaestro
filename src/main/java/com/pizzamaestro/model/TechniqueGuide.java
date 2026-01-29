package com.pizzamaestro.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Baza wiedzy - przewodniki po technikach pizzy.
 * 
 * Zawiera szczegółowe instrukcje dotyczące:
 * - Prefermentów (poolish, biga, zakwas)
 * - Technik składania ciasta
 * - Technik kulkowania
 * - Parametrów mąk
 * - Metod fermentacji
 */
@Document(collection = "technique_guides")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechniqueGuide {
    
    @Id
    private String id;
    
    @Indexed
    private TechniqueCategory category;
    
    @Indexed
    private String slug; // URL-friendly identifier
    
    private String title;
    
    private String titleEn; // English title
    
    private String shortDescription;
    
    private String fullDescription;
    
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.INTERMEDIATE;
    
    private Integer estimatedTimeMinutes;
    
    private List<String> requiredEquipment;
    
    private List<InstructionStep> steps;
    
    private List<ProTip> proTips;
    
    private List<CommonMistake> commonMistakes;
    
    private ScienceExplanation science;
    
    private List<String> relatedTechniques;
    
    private List<PizzaStyle> recommendedForStyles;
    
    private String videoUrl;
    
    private List<String> imageUrls;
    
    @Builder.Default
    private boolean premium = false;
    
    @Builder.Default
    private boolean active = true;
    
    @Builder.Default
    private int viewCount = 0;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // ========== ENUMS ==========
    
    public enum TechniqueCategory {
        PREFERMENT("Prefermenty", "Poolish, Biga, Zakwas"),
        MIXING("Mieszanie ciasta", "Techniki łączenia składników"),
        FOLDING("Składanie ciasta", "Stretch & fold, coil fold"),
        SHAPING("Formowanie kulek", "Techniki kulkowania"),
        STRETCHING("Rozciąganie ciasta", "Metody formowania pizzy"),
        FERMENTATION("Fermentacja", "Zarządzanie procesem fermentacji"),
        BAKING("Pieczenie", "Techniki wypiekania"),
        FLOUR_SCIENCE("Nauka o mące", "Parametry W, P/L, białko"),
        HYDRATION("Hydratacja", "Zarządzanie nawodnieniem"),
        TEMPERATURE("Temperatura", "Kontrola temperatury");
        
        private final String displayName;
        private final String description;
        
        TechniqueCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum DifficultyLevel {
        BEGINNER("Początkujący", 1, "Idealne na start"),
        INTERMEDIATE("Średniozaawansowany", 2, "Wymaga podstawowej wprawy"),
        ADVANCED("Zaawansowany", 3, "Dla doświadczonych pizzaioli"),
        EXPERT("Ekspert", 4, "Techniki profesjonalne");
        
        private final String displayName;
        private final int level;
        private final String description;
        
        DifficultyLevel(String displayName, int level, String description) {
            this.displayName = displayName;
            this.level = level;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }
    
    // ========== NESTED CLASSES ==========
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructionStep {
        private int stepNumber;
        private String title;
        private String description;
        private String detailedExplanation;
        private Integer durationSeconds;
        private String imageUrl;
        private String videoTimestamp; // np. "2:35"
        private List<String> tips;
        private boolean critical; // czy to kluczowy krok
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProTip {
        private String title;
        private String content;
        private TipCategory category;
        private boolean premiumOnly;
        
        public enum TipCategory {
            TIME_SAVER, QUALITY_BOOST, COMMON_FIX, ADVANCED, EQUIPMENT
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonMistake {
        private String mistake;
        private String consequence;
        private String solution;
        private String prevention;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScienceExplanation {
        private String mainPrinciple;
        private List<String> chemicalProcesses;
        private List<String> physicalProcesses;
        private String whyItWorks;
        private List<String> references;
    }
}
