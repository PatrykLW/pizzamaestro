package com.pizzamaestro.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reprezentuje aktywną pizzę w trakcie przygotowania.
 * Pozwala śledzić postęp, zarządzać harmonogramem i otrzymywać powiadomienia.
 */
@Document(collection = "active_pizzas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivePizza {

    @Id
    private String id;

    @Indexed
    private String userId;

    /**
     * ID zapisanego przepisu (opcjonalne - może być nowy przepis bez zapisania)
     */
    private String recipeId;

    /**
     * Nazwa pizzy/przepisu
     */
    private String name;

    /**
     * Styl pizzy
     */
    private PizzaStyle pizzaStyle;

    /**
     * Liczba pizz
     */
    private Integer numberOfPizzas;

    /**
     * Docelowa godzina wypieku
     */
    @Indexed
    private LocalDateTime targetBakeTime;

    /**
     * Skorygowana godzina wypieku (po przesunięciach)
     */
    private LocalDateTime adjustedBakeTime;

    /**
     * Lista kroków z harmonogramem
     */
    @Builder.Default
    private List<ScheduledStep> steps = new ArrayList<>();

    /**
     * Status aktywnej pizzy
     */
    @Indexed
    @Builder.Default
    private ActivePizzaStatus status = ActivePizzaStatus.PLANNING;

    /**
     * Notatki użytkownika
     */
    private String notes;

    /**
     * Numer telefonu do powiadomień SMS
     */
    private String notificationPhone;

    /**
     * Czy wysyłać powiadomienia SMS
     */
    @Builder.Default
    private boolean smsNotificationsEnabled = false;

    /**
     * Ile minut przed krokiem wysłać przypomnienie
     */
    @Builder.Default
    private Integer reminderMinutesBefore = 15;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;

    /**
     * Status aktywnej pizzy
     */
    public enum ActivePizzaStatus {
        PLANNING("Planowanie"),
        IN_PROGRESS("W trakcie"),
        PAUSED("Wstrzymana"),
        COMPLETED("Zakończona"),
        CANCELLED("Anulowana");

        private final String displayName;

        ActivePizzaStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Pojedynczy krok w harmonogramie pizzy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduledStep {
        
        private int stepNumber;
        
        private StepType type;
        
        private String title;
        
        private String description;
        
        /**
         * Zaplanowana godzina wykonania
         */
        private LocalDateTime scheduledTime;
        
        /**
         * Faktyczna godzina wykonania (jeśli ukończono)
         */
        private LocalDateTime actualTime;
        
        /**
         * Szacowany czas trwania kroku w minutach
         */
        private Integer durationMinutes;
        
        /**
         * Temperatura dla tego kroku (np. fermentacji)
         */
        private Double temperature;
        
        /**
         * Status kroku
         */
        @Builder.Default
        private StepStatus status = StepStatus.PENDING;
        
        /**
         * Czy powiadomienie SMS zostało wysłane
         */
        @Builder.Default
        private boolean notificationSent = false;
        
        /**
         * Czas wysłania powiadomienia
         */
        private LocalDateTime notificationSentAt;
        
        /**
         * Notatka do kroku
         */
        private String note;
        
        /**
         * Ikona dla UI
         */
        private String icon;
    }

    /**
     * Typy kroków w procesie wypieku pizzy
     */
    public enum StepType {
        PREPARE_INGREDIENTS("Przygotowanie składników", "inventory_2"),
        MIX_DOUGH("Mieszanie ciasta", "blender"),
        AUTOLYSE("Autoliza", "timer"),
        KNEAD("Wyrabianie", "sports_handball"),
        BULK_FERMENTATION("Fermentacja w bloku", "expand"),
        FOLD("Składanie ciasta", "layers"),
        DIVIDE_AND_BALL("Dzielenie i kulkowanie", "pie_chart"),
        COLD_PROOF("Fermentacja w lodówce", "ac_unit"),
        ROOM_TEMP_PROOF("Fermentacja w temp. pokojowej", "thermostat"),
        REMOVE_FROM_FRIDGE("Wyjęcie z lodówki", "kitchen"),
        WARM_UP("Rozgrzewanie ciasta", "whatshot"),
        PREHEAT_OVEN("Rozgrzewanie pieca", "local_fire_department"),
        SHAPE("Formowanie pizzy", "crop_free"),
        TOP("Nakładanie sosów i dodatków", "add_circle"),
        BAKE("Wypiek", "local_pizza"),
        REST("Odpoczynek pizzy", "hourglass_empty"),
        SERVE("Podawanie", "restaurant"),
        CUSTOM("Własny krok", "edit");

        private final String displayName;
        private final String icon;

        StepType(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIcon() {
            return icon;
        }
    }

    /**
     * Status pojedynczego kroku
     */
    public enum StepStatus {
        PENDING("Oczekuje"),
        IN_PROGRESS("W trakcie"),
        COMPLETED("Ukończony"),
        COMPLETED_EARLY("Ukończony wcześniej"),
        COMPLETED_LATE("Ukończony później"),
        SKIPPED("Pominięty");

        private final String displayName;

        StepStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ==================== Metody pomocnicze ====================

    /**
     * Zwraca następny oczekujący krok
     */
    public ScheduledStep getNextPendingStep() {
        return steps.stream()
                .filter(step -> step.getStatus() == StepStatus.PENDING || step.getStatus() == StepStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);
    }

    /**
     * Zwraca procent ukończenia
     */
    public int getCompletionPercentage() {
        if (steps.isEmpty()) return 0;
        
        long completedSteps = steps.stream()
                .filter(step -> step.getStatus() == StepStatus.COMPLETED 
                        || step.getStatus() == StepStatus.COMPLETED_EARLY 
                        || step.getStatus() == StepStatus.COMPLETED_LATE
                        || step.getStatus() == StepStatus.SKIPPED)
                .count();
        
        return (int) ((completedSteps * 100) / steps.size());
    }

    /**
     * Sprawdza czy wszystkie kroki są ukończone
     */
    public boolean isCompleted() {
        return steps.stream()
                .allMatch(step -> step.getStatus() != StepStatus.PENDING && step.getStatus() != StepStatus.IN_PROGRESS);
    }

    /**
     * Zwraca czas do następnego kroku w minutach
     */
    public Long getMinutesToNextStep() {
        ScheduledStep next = getNextPendingStep();
        if (next == null || next.getScheduledTime() == null) {
            return null;
        }
        
        return java.time.Duration.between(LocalDateTime.now(), next.getScheduledTime()).toMinutes();
    }
}
