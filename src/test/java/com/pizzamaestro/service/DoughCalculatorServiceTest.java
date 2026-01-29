package com.pizzamaestro.service;

import com.pizzamaestro.dto.request.CalculationRequest;
import com.pizzamaestro.dto.response.CalculationResponse;
import com.pizzamaestro.model.OvenType;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Testy jednostkowe dla DoughCalculatorService.
 */
@SpringBootTest
@DisplayName("DoughCalculatorService Tests")
class DoughCalculatorServiceTest {
    
    @Autowired
    private DoughCalculatorService calculatorService;
    
    private CalculationRequest.CalculationRequestBuilder defaultRequestBuilder;
    
    @BeforeEach
    void setUp() {
        defaultRequestBuilder = CalculationRequest.builder()
                .pizzaStyle(PizzaStyle.NEAPOLITAN)
                .numberOfPizzas(4)
                .ballWeight(250)
                .hydration(65.0)
                .saltPercentage(2.5)
                .oilPercentage(0.0)
                .sugarPercentage(0.0)
                .yeastType(Recipe.YeastType.FRESH)
                .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                .totalFermentationHours(24)
                .roomTemperature(22.0)
                .fridgeTemperature(4.0);
    }
    
    // ========================================
    // TESTY WALIDACJI
    // ========================================
    
    @Nested
    @DisplayName("Walidacja wejścia")
    class ValidationTests {
        
        @Test
        @DisplayName("Powinien rzucić wyjątek gdy request jest null")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> calculatorService.calculate(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
        
        @Test
        @DisplayName("Powinien rzucić wyjątek gdy pizzaStyle jest null")
        void shouldThrowWhenPizzaStyleIsNull() {
            CalculationRequest request = defaultRequestBuilder.pizzaStyle(null).build();
            
            assertThatThrownBy(() -> calculatorService.calculate(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Styl pizzy");
        }
        
        @Test
        @DisplayName("Powinien rzucić wyjątek gdy numberOfPizzas jest 0")
        void shouldThrowWhenNumberOfPizzasIsZero() {
            CalculationRequest request = defaultRequestBuilder.numberOfPizzas(0).build();
            
            assertThatThrownBy(() -> calculatorService.calculate(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Liczba pizz");
        }
        
        @Test
        @DisplayName("Powinien rzucić wyjątek gdy numberOfPizzas jest ujemna")
        void shouldThrowWhenNumberOfPizzasIsNegative() {
            CalculationRequest request = defaultRequestBuilder.numberOfPizzas(-1).build();
            
            assertThatThrownBy(() -> calculatorService.calculate(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Liczba pizz");
        }
    }
    
    // ========================================
    // TESTY KALKULACJI SKŁADNIKÓW
    // ========================================
    
    @Nested
    @DisplayName("Kalkulacja składników")
    class IngredientCalculationTests {
        
        @Test
        @DisplayName("Powinien obliczyć całkowitą wagę ciasta")
        void shouldCalculateTotalDoughWeight() {
            CalculationRequest request = defaultRequestBuilder.build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            // 4 pizze x 250g = 1000g
            assertThat(response.getIngredients().getTotalDoughWeight()).isEqualTo(1000.0);
        }
        
        @Test
        @DisplayName("Powinien obliczyć ilość mąki zgodnie z Baker's Math")
        void shouldCalculateFlourAmount() {
            CalculationRequest request = defaultRequestBuilder.build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            // Mąka = 100% bazy (około 594g dla 1000g ciasta przy 65% hydratacji)
            assertThat(response.getIngredients().getFlourGrams())
                    .isBetween(590.0, 600.0);
        }
        
        @Test
        @DisplayName("Powinien obliczyć ilość wody na podstawie hydratacji")
        void shouldCalculateWaterBasedOnHydration() {
            CalculationRequest request = defaultRequestBuilder.hydration(65.0).build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            double flour = response.getIngredients().getFlourGrams();
            double water = response.getIngredients().getWaterGrams();
            double actualHydration = (water / flour) * 100;
            
            assertThat(actualHydration).isCloseTo(65.0, within(0.1));
        }
        
        @Test
        @DisplayName("Powinien obliczyć ilość soli na podstawie procentu")
        void shouldCalculateSaltBasedOnPercentage() {
            CalculationRequest request = defaultRequestBuilder.saltPercentage(2.5).build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            double flour = response.getIngredients().getFlourGrams();
            double salt = response.getIngredients().getSaltGrams();
            double actualPercentage = (salt / flour) * 100;
            
            assertThat(actualPercentage).isCloseTo(2.5, within(0.1));
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {55.0, 60.0, 65.0, 70.0, 75.0, 80.0})
        @DisplayName("Powinien poprawnie obliczyć hydratację dla różnych wartości")
        void shouldCalculateCorrectHydrationForDifferentValues(double hydration) {
            CalculationRequest request = defaultRequestBuilder.hydration(hydration).build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getBakerPercentages().getWater()).isEqualTo(hydration);
        }
    }
    
    // ========================================
    // TESTY KALKULACJI DROŻDŻY
    // ========================================
    
    @Nested
    @DisplayName("Kalkulacja drożdży")
    class YeastCalculationTests {
        
        @Test
        @DisplayName("Powinien obliczyć mniej drożdży dla dłuższej fermentacji")
        void shouldCalculateLessYeastForLongerFermentation() {
            CalculationRequest shortFermentation = defaultRequestBuilder
                    .totalFermentationHours(8).build();
            CalculationRequest longFermentation = defaultRequestBuilder
                    .totalFermentationHours(72).build();
            
            CalculationResponse shortResponse = calculatorService.calculate(shortFermentation);
            CalculationResponse longResponse = calculatorService.calculate(longFermentation);
            
            assertThat(longResponse.getIngredients().getYeastGrams())
                    .isLessThan(shortResponse.getIngredients().getYeastGrams());
        }
        
        @Test
        @DisplayName("Powinien użyć podanego procentu drożdży jeśli podany")
        void shouldUseProvidedYeastPercentage() {
            CalculationRequest request = defaultRequestBuilder
                    .yeastPercentage(1.0).build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            double flour = response.getIngredients().getFlourGrams();
            double yeast = response.getIngredients().getYeastGrams();
            double actualPercentage = (yeast / flour) * 100;
            
            // Konwersja z fresh na fresh = 1:1
            assertThat(actualPercentage).isCloseTo(1.0, within(0.1));
        }
        
        @Test
        @DisplayName("Powinien konwertować drożdże suche na świeże")
        void shouldConvertDryYeastToFresh() {
            CalculationRequest freshRequest = defaultRequestBuilder
                    .yeastType(Recipe.YeastType.FRESH)
                    .yeastPercentage(1.0).build();
            CalculationRequest dryRequest = defaultRequestBuilder
                    .yeastType(Recipe.YeastType.INSTANT_DRY)
                    .yeastPercentage(1.0).build();
            
            CalculationResponse freshResponse = calculatorService.calculate(freshRequest);
            CalculationResponse dryResponse = calculatorService.calculate(dryRequest);
            
            // Suche instant = ~1/3 świeżych
            assertThat(dryResponse.getIngredients().getYeastGrams())
                    .isLessThan(freshResponse.getIngredients().getYeastGrams());
        }
    }
    
    // ========================================
    // TESTY STYLÓW PIZZY
    // ========================================
    
    @Nested
    @DisplayName("Style pizzy")
    class PizzaStyleTests {
        
        @ParameterizedTest
        @EnumSource(PizzaStyle.class)
        @DisplayName("Powinien obsłużyć wszystkie style pizzy")
        void shouldHandleAllPizzaStyles(PizzaStyle style) {
            CalculationRequest request = defaultRequestBuilder
                    .pizzaStyle(style).build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getPizzaStyle()).isEqualTo(style);
            assertThat(response.getPizzaStyleName()).isEqualTo(style.getDisplayName());
        }
        
        @Test
        @DisplayName("Powinien zwrócić informacje o piecu dla stylu")
        void shouldReturnOvenInfoForStyle() {
            CalculationRequest request = defaultRequestBuilder
                    .pizzaStyle(PizzaStyle.NEAPOLITAN).build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getOvenInfo()).isNotNull();
            assertThat(response.getOvenInfo().getTemperature()).isGreaterThan(400);
        }
    }
    
    // ========================================
    // TESTY PREFERMENTU
    // ========================================
    
    @Nested
    @DisplayName("Preferment")
    class PrefermentTests {
        
        @Test
        @DisplayName("Powinien obliczyć poolish")
        void shouldCalculatePoolish() {
            CalculationRequest request = defaultRequestBuilder
                    .usePreferment(true)
                    .prefermentType(Recipe.PrefermentType.POOLISH)
                    .prefermentPercentage(30.0)
                    .prefermentFermentationHours(12)
                    .build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getPreferment()).isNotNull();
            assertThat(response.getPreferment().getType()).isEqualTo(Recipe.PrefermentType.POOLISH);
            // Poolish ma 100% hydratacji
            assertThat(response.getPreferment().getWaterGrams())
                    .isEqualTo(response.getPreferment().getFlourGrams());
        }
        
        @Test
        @DisplayName("Powinien obliczyć bigę")
        void shouldCalculateBiga() {
            CalculationRequest request = defaultRequestBuilder
                    .usePreferment(true)
                    .prefermentType(Recipe.PrefermentType.BIGA)
                    .prefermentPercentage(30.0)
                    .prefermentFermentationHours(16)
                    .build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getPreferment()).isNotNull();
            assertThat(response.getPreferment().getType()).isEqualTo(Recipe.PrefermentType.BIGA);
            // Biga ma niższą hydratację (~50-60%)
            assertThat(response.getPreferment().getWaterGrams())
                    .isLessThan(response.getPreferment().getFlourGrams());
        }
        
        @Test
        @DisplayName("Powinien odjąć preferment od głównego ciasta")
        void shouldSubtractPrefermentFromMainDough() {
            CalculationRequest request = defaultRequestBuilder
                    .usePreferment(true)
                    .prefermentType(Recipe.PrefermentType.POOLISH)
                    .prefermentPercentage(30.0)
                    .build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getMainDough()).isNotNull();
            assertThat(response.getMainDough().getFlourGrams())
                    .isLessThan(response.getIngredients().getFlourGrams());
        }
    }
    
    // ========================================
    // TESTY HARMONOGRAMU
    // ========================================
    
    @Nested
    @DisplayName("Harmonogram")
    class ScheduleTests {
        
        @Test
        @DisplayName("Powinien wygenerować harmonogram gdy generateSchedule=true")
        void shouldGenerateScheduleWhenEnabled() {
            CalculationRequest request = defaultRequestBuilder
                    .generateSchedule(true)
                    .plannedBakeTime(LocalDateTime.now().plusHours(24))
                    .build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getSchedule()).isNotNull();
            assertThat(response.getSchedule()).isNotEmpty();
        }
        
        @Test
        @DisplayName("Nie powinien generować harmonogramu gdy generateSchedule=false")
        void shouldNotGenerateScheduleWhenDisabled() {
            CalculationRequest request = defaultRequestBuilder
                    .generateSchedule(false)
                    .build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getSchedule()).isNull();
        }
        
        @Test
        @DisplayName("Harmonogram powinien zawierać kroki dla fermentacji zimnej")
        void shouldContainColdFermentationSteps() {
            CalculationRequest request = defaultRequestBuilder
                    .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                    .generateSchedule(true)
                    .plannedBakeTime(LocalDateTime.now().plusHours(48))
                    .build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            boolean hasColdProof = response.getSchedule().stream()
                    .anyMatch(step -> step.getStepType() == Recipe.StepType.COLD_PROOF);
            
            assertThat(hasColdProof).isTrue();
        }
        
        @Test
        @DisplayName("Harmonogram powinien zawierać składania dla wysokiej hydratacji")
        void shouldContainFoldsForHighHydration() {
            CalculationRequest request = defaultRequestBuilder
                    .hydration(75.0)
                    .fermentationMethod(Recipe.FermentationMethod.ROOM_TEMPERATURE)
                    .totalFermentationHours(8)
                    .generateSchedule(true)
                    .plannedBakeTime(LocalDateTime.now().plusHours(8))
                    .build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            boolean hasFolds = response.getSchedule().stream()
                    .anyMatch(step -> step.getStepType() == Recipe.StepType.FOLD);
            
            assertThat(hasFolds).isTrue();
        }
    }
    
    // ========================================
    // TESTY WSKAZÓWEK
    // ========================================
    
    @Nested
    @DisplayName("Wskazówki")
    class TipsTests {
        
        @Test
        @DisplayName("Powinien wygenerować wskazówki")
        void shouldGenerateTips() {
            CalculationRequest request = defaultRequestBuilder.build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            assertThat(response.getTips()).isNotNull();
            assertThat(response.getTips()).isNotEmpty();
        }
        
        @Test
        @DisplayName("Powinien dodać wskazówki o składaniu dla wysokiej hydratacji")
        void shouldAddFoldingTipsForHighHydration() {
            CalculationRequest request = defaultRequestBuilder
                    .hydration(75.0).build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            boolean hasFoldingTip = response.getTips().stream()
                    .anyMatch(tip -> tip.toLowerCase().contains("składan") || 
                                     tip.toLowerCase().contains("fold"));
            
            assertThat(hasFoldingTip).isTrue();
        }
        
        @Test
        @DisplayName("Powinien dodać wskazówki o lodówce dla fermentacji zimnej")
        void shouldAddFridgeTipsForColdFermentation() {
            CalculationRequest request = defaultRequestBuilder
                    .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                    .build();
            
            CalculationResponse response = calculatorService.calculate(request);
            
            boolean hasFridgeTip = response.getTips().stream()
                    .anyMatch(tip -> tip.toLowerCase().contains("lodówk"));
            
            assertThat(hasFridgeTip).isTrue();
        }
    }
}
