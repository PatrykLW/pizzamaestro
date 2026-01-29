package com.pizzamaestro.service;

import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.service.TipEngineService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testy jednostkowe dla TipEngineService.
 */
@DisplayName("TipEngineService Tests")
class TipEngineServiceTest {
    
    private TipEngineService tipEngineService;
    
    @BeforeEach
    void setUp() {
        tipEngineService = new TipEngineService();
    }
    
    // ========================================
    // TESTY WALIDACJI
    // ========================================
    
    @Nested
    @DisplayName("Walidacja wejścia")
    class ValidationTests {
        
        @Test
        @DisplayName("Powinien rzucić wyjątek gdy context jest null")
        void shouldThrowWhenContextIsNull() {
            assertThatThrownBy(() -> tipEngineService.generateAllTips(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
        
        @Test
        @DisplayName("Powinien rzucić wyjątek gdy parameterName jest null w generateTipsForChange")
        void shouldThrowWhenParameterNameIsNull() {
            CalculationContext context = CalculationContext.builder()
                    .hydration(65).fermentationHours(24).build();
            
            assertThatThrownBy(() -> tipEngineService.generateTipsForChange(null, 60, 65, context))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
    
    // ========================================
    // TESTY GENEROWANIA TIPÓW
    // ========================================
    
    @Nested
    @DisplayName("Generowanie tipów")
    class TipGenerationTests {
        
        @Test
        @DisplayName("Powinien wygenerować tipy dla podstawowego kontekstu")
        void shouldGenerateTipsForBasicContext() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(24)
                    .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                    .roomTemperature(22.0)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            assertThat(tips).isNotNull();
            assertThat(tips.getTips()).isNotEmpty();
            assertThat(tips.getContextSummary()).isNotNull();
        }
        
        @Test
        @DisplayName("Powinien wygenerować tipy dla stylu pizzy")
        void shouldGenerateStyleTips() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(24)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasStyleTip = tips.getTips().stream()
                    .anyMatch(tip -> tip.getCategory() == TipCategory.STYLE);
            
            assertThat(hasStyleTip).isTrue();
        }
        
        @Test
        @DisplayName("Powinien wygenerować tipy dla hydratacji")
        void shouldGenerateHydrationTips() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(75)
                    .fermentationHours(24)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasHydrationTip = tips.getTips().stream()
                    .anyMatch(tip -> tip.getCategory() == TipCategory.HYDRATION);
            
            assertThat(hasHydrationTip).isTrue();
        }
    }
    
    // ========================================
    // TESTY OSTRZEŻEŃ
    // ========================================
    
    @Nested
    @DisplayName("Generowanie ostrzeżeń")
    class WarningTests {
        
        @Test
        @DisplayName("Powinien wygenerować ostrzeżenie dla wysokiej hydratacji neapolitańskiej")
        void shouldWarnAboutHighHydrationForNeapolitan() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(75) // Za wysoka dla neapolitańskiej
                    .fermentationHours(24)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            assertThat(tips.getWarnings()).isNotEmpty();
            boolean hasHydrationWarning = tips.getWarnings().stream()
                    .anyMatch(w -> w.getCategory() == TipCategory.HYDRATION);
            assertThat(hasHydrationWarning).isTrue();
        }
        
        @Test
        @DisplayName("Powinien wygenerować ostrzeżenie dla słabej mąki i wysokiej hydratacji")
        void shouldWarnAboutWeakFlourAndHighHydration() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.ROMAN)
                    .hydration(80)
                    .fermentationHours(48)
                    .flourStrength(220) // Słaba mąka
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasFlourWarning = tips.getWarnings().stream()
                    .anyMatch(w -> w.getCategory() == TipCategory.FLOUR);
            assertThat(hasFlourWarning).isTrue();
        }
        
        @Test
        @DisplayName("Powinien wygenerować ostrzeżenie dla długiej fermentacji w wysokiej temperaturze")
        void shouldWarnAboutLongFermentationInHighTemp() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(24)
                    .fermentationMethod(Recipe.FermentationMethod.ROOM_TEMPERATURE)
                    .roomTemperature(30.0) // Wysoka temperatura
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasTemperatureWarning = tips.getWarnings().stream()
                    .anyMatch(w -> w.getCategory() == TipCategory.TEMPERATURE);
            assertThat(hasTemperatureWarning).isTrue();
        }
        
        @Test
        @DisplayName("Powinien wygenerować ostrzeżenie dla zbyt krótkiej zimnej fermentacji")
        void shouldWarnAboutTooShortColdFermentation() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(8) // Za krótko dla zimnej
                    .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasFermentationWarning = tips.getWarnings().stream()
                    .anyMatch(w -> w.getCategory() == TipCategory.FERMENTATION);
            assertThat(hasFermentationWarning).isTrue();
        }
    }
    
    // ========================================
    // TESTY TIPÓW DLA ZMIAN PARAMETRÓW
    // ========================================
    
    @Nested
    @DisplayName("Tipy dla zmian parametrów")
    class ChangeExplanationTests {
        
        @Test
        @DisplayName("Powinien wyjaśnić zmianę hydratacji w górę")
        void shouldExplainHydrationIncrease() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(70)
                    .fermentationHours(24)
                    .build();
            
            List<Tip> tips = tipEngineService.generateTipsForChange(
                    "hydration", 60, 70, context);
            
            assertThat(tips).isNotEmpty();
            boolean hasChangeExplanation = tips.stream()
                    .anyMatch(t -> t.getType() == TipType.CHANGE_EXPLANATION);
            assertThat(hasChangeExplanation).isTrue();
        }
        
        @Test
        @DisplayName("Powinien wyjaśnić wydłużenie fermentacji")
        void shouldExplainFermentationExtension() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(48)
                    .build();
            
            List<Tip> tips = tipEngineService.generateTipsForChange(
                    "fermentationhours", 24, 48, context);
            
            assertThat(tips).isNotEmpty();
            // Powinien wspomnieć o mniejszej ilości drożdży
            boolean mentionsYeast = tips.stream()
                    .anyMatch(t -> t.getContent().toLowerCase().contains("drożdż"));
            assertThat(mentionsYeast).isTrue();
        }
        
        @Test
        @DisplayName("Powinien wyjaśnić zmianę siły mąki")
        void shouldExplainFlourStrengthChange() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(24)
                    .flourStrength(300)
                    .build();
            
            List<Tip> tips = tipEngineService.generateTipsForChange(
                    "flourstrength", 260, 300, context);
            
            assertThat(tips).isNotEmpty();
        }
        
        @Test
        @DisplayName("Powinien wyjaśnić włączenie prefermentu")
        void shouldExplainPrefermentEnabled() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(24)
                    .usePreferment(true)
                    .build();
            
            List<Tip> tips = tipEngineService.generateTipsForChange(
                    "usepreferment", false, true, context);
            
            assertThat(tips).isNotEmpty();
            // Powinien wspomnieć o smaku
            boolean mentionsFlavor = tips.stream()
                    .anyMatch(t -> t.getContent().toLowerCase().contains("smak"));
            assertThat(mentionsFlavor).isTrue();
        }
    }
    
    // ========================================
    // TESTY KATEGORYZACJI
    // ========================================
    
    @Nested
    @DisplayName("Kategoryzacja tipów")
    class CategorizationTests {
        
        @ParameterizedTest
        @CsvSource({
                "NEAPOLITAN, 65, 24",
                "NEW_YORK, 62, 48",
                "ROMAN, 80, 72",
                "DETROIT, 70, 24"
        })
        @DisplayName("Powinien wygenerować tipy dla różnych konfiguracji")
        void shouldGenerateTipsForVariousConfigurations(
                String styleName, double hydration, int fermentation) {
            
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.valueOf(styleName))
                    .hydration(hydration)
                    .fermentationHours(fermentation)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            assertThat(tips.getTips()).isNotEmpty();
        }
        
        @Test
        @DisplayName("Powinien wygenerować rekomendacje optymalizacyjne")
        void shouldGenerateOptimizationRecommendations() {
            // Wysoka hydratacja + krótka fermentacja = suboptymalne
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.ROMAN)
                    .hydration(75)
                    .fermentationHours(12) // Za krótko dla takiej hydratacji
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            assertThat(tips.getRecommendations()).isNotEmpty();
        }
    }
    
    // ========================================
    // TESTY TIPÓW NAUKOWYCH
    // ========================================
    
    @Nested
    @DisplayName("Tipy naukowe")
    class ScienceTipsTests {
        
        @Test
        @DisplayName("Powinien wygenerować tipy naukowe o hydratacji")
        void shouldGenerateScienceTipsAboutHydration() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(24)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasScienceTip = tips.getTips().stream()
                    .anyMatch(t -> t.getType() == TipType.SCIENCE);
            assertThat(hasScienceTip).isTrue();
        }
        
        @Test
        @DisplayName("Powinien wygenerować tipy naukowe o fermentacji")
        void shouldGenerateScienceTipsAboutFermentation() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(48)
                    .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasFermentationScienceTip = tips.getTips().stream()
                    .anyMatch(t -> t.getType() == TipType.SCIENCE && 
                                   t.getCategory() == TipCategory.FERMENTATION);
            assertThat(hasFermentationScienceTip).isTrue();
        }
    }
    
    // ========================================
    // TESTY TIPÓW POGODOWYCH
    // ========================================
    
    @Nested
    @DisplayName("Tipy pogodowe")
    class WeatherTipsTests {
        
        @Test
        @DisplayName("Powinien wygenerować tipy pogodowe gdy dane pogodowe są dostępne")
        void shouldGenerateWeatherTipsWhenWeatherDataAvailable() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(24)
                    .weatherTemperature(28.0)
                    .weatherHumidity(75.0)
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasWeatherTip = tips.getTips().stream()
                    .anyMatch(t -> t.getCategory() == TipCategory.WEATHER);
            assertThat(hasWeatherTip).isTrue();
        }
        
        @Test
        @DisplayName("Nie powinien generować tipów pogodowych gdy brak danych")
        void shouldNotGenerateWeatherTipsWhenNoWeatherData() {
            CalculationContext context = CalculationContext.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .hydration(65)
                    .fermentationHours(24)
                    // Brak weatherTemperature i weatherHumidity
                    .build();
            
            TipCollection tips = tipEngineService.generateAllTips(context);
            
            boolean hasWeatherTip = tips.getTips().stream()
                    .anyMatch(t -> t.getCategory() == TipCategory.WEATHER);
            assertThat(hasWeatherTip).isFalse();
        }
    }
}
