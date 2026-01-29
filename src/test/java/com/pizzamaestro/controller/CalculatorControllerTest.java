package com.pizzamaestro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzamaestro.dto.request.CalculationRequest;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy integracyjne dla CalculatorController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CalculatorController Integration Tests")
class CalculatorControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // ========================================
    // TESTY GET ENDPOINTS
    // ========================================
    
    @Nested
    @DisplayName("GET /api/calculator/public/styles")
    class GetStylesTests {
        
        @Test
        @DisplayName("Powinien zwrócić listę stylów pizzy")
        void shouldReturnPizzaStyles() throws Exception {
            mockMvc.perform(get("/api/calculator/public/styles"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[0].displayName").exists());
        }
    }
    
    @Nested
    @DisplayName("GET /api/calculator/public/ovens")
    class GetOvensTests {
        
        @Test
        @DisplayName("Powinien zwrócić listę typów pieców")
        void shouldReturnOvenTypes() throws Exception {
            mockMvc.perform(get("/api/calculator/public/ovens"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(greaterThan(0))));
        }
    }
    
    @Nested
    @DisplayName("GET /api/calculator/public/yeast-types")
    class GetYeastTypesTests {
        
        @Test
        @DisplayName("Powinien zwrócić listę typów drożdży")
        void shouldReturnYeastTypes() throws Exception {
            mockMvc.perform(get("/api/calculator/public/yeast-types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(greaterThan(0))));
        }
    }
    
    @Nested
    @DisplayName("GET /api/calculator/public/fermentation-methods")
    class GetFermentationMethodsTests {
        
        @Test
        @DisplayName("Powinien zwrócić listę metod fermentacji")
        void shouldReturnFermentationMethods() throws Exception {
            mockMvc.perform(get("/api/calculator/public/fermentation-methods"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(greaterThan(0))));
        }
    }
    
    // ========================================
    // TESTY POST /calculate
    // ========================================
    
    @Nested
    @DisplayName("POST /api/calculator/public/calculate")
    class CalculateTests {
        
        @Test
        @DisplayName("Powinien wykonać kalkulację dla prawidłowego requestu")
        void shouldCalculateForValidRequest() throws Exception {
            CalculationRequest request = CalculationRequest.builder()
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
                    .build();
            
            mockMvc.perform(post("/api/calculator/public/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.pizzaStyle").value("NEAPOLITAN"))
                    .andExpect(jsonPath("$.numberOfPizzas").value(4))
                    .andExpect(jsonPath("$.ingredients").exists())
                    .andExpect(jsonPath("$.ingredients.flourGrams").isNumber())
                    .andExpect(jsonPath("$.ingredients.waterGrams").isNumber())
                    .andExpect(jsonPath("$.ingredients.saltGrams").isNumber())
                    .andExpect(jsonPath("$.ingredients.yeastGrams").isNumber());
        }
        
        @Test
        @DisplayName("Powinien zwrócić 400 dla nieprawidłowego requestu - brak stylu")
        void shouldReturn400ForMissingStyle() throws Exception {
            CalculationRequest request = CalculationRequest.builder()
                    .pizzaStyle(null) // Brak stylu
                    .numberOfPizzas(4)
                    .ballWeight(250)
                    .hydration(65.0)
                    .saltPercentage(2.5)
                    .yeastType(Recipe.YeastType.FRESH)
                    .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                    .totalFermentationHours(24)
                    .build();
            
            mockMvc.perform(post("/api/calculator/public/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Powinien zwrócić 400 dla nieprawidłowej liczby pizz")
        void shouldReturn400ForInvalidNumberOfPizzas() throws Exception {
            CalculationRequest request = CalculationRequest.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .numberOfPizzas(0) // Nieprawidłowa liczba
                    .ballWeight(250)
                    .hydration(65.0)
                    .saltPercentage(2.5)
                    .yeastType(Recipe.YeastType.FRESH)
                    .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                    .totalFermentationHours(24)
                    .build();
            
            mockMvc.perform(post("/api/calculator/public/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Powinien zwrócić 400 dla zbyt wysokiej hydratacji")
        void shouldReturn400ForTooHighHydration() throws Exception {
            CalculationRequest request = CalculationRequest.builder()
                    .pizzaStyle(PizzaStyle.NEAPOLITAN)
                    .numberOfPizzas(4)
                    .ballWeight(250)
                    .hydration(100.0) // Za wysoka
                    .saltPercentage(2.5)
                    .yeastType(Recipe.YeastType.FRESH)
                    .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                    .totalFermentationHours(24)
                    .build();
            
            mockMvc.perform(post("/api/calculator/public/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Powinien obliczyć z prefermentem")
        void shouldCalculateWithPreferment() throws Exception {
            CalculationRequest request = CalculationRequest.builder()
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
                    .usePreferment(true)
                    .prefermentType(Recipe.PrefermentType.POOLISH)
                    .prefermentPercentage(30.0)
                    .prefermentFermentationHours(12)
                    .build();
            
            mockMvc.perform(post("/api/calculator/public/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.preferment").exists())
                    .andExpect(jsonPath("$.preferment.type").value("POOLISH"))
                    .andExpect(jsonPath("$.mainDough").exists());
        }
    }
    
    // ========================================
    // TESTY STYLÓW PIZZY
    // ========================================
    
    @Nested
    @DisplayName("GET /api/calculator/public/style/{style}")
    class GetStyleDefaultsTests {
        
        @Test
        @DisplayName("Powinien zwrócić domyślne parametry dla neapolitańskiej")
        void shouldReturnNeapolitanDefaults() throws Exception {
            mockMvc.perform(get("/api/calculator/public/style/NEAPOLITAN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hydration").exists())
                    .andExpect(jsonPath("$.fermentationHours").exists())
                    .andExpect(jsonPath("$.saltPercentage").exists());
        }
        
        @Test
        @DisplayName("Powinien zwrócić domyślne parametry dla NY style")
        void shouldReturnNewYorkDefaults() throws Exception {
            mockMvc.perform(get("/api/calculator/public/style/NEW_YORK"))
                    .andExpect(status().isOk());
        }
        
        @Test
        @DisplayName("Powinien zwrócić 400 dla nieprawidłowego stylu")
        void shouldReturn400ForInvalidStyle() throws Exception {
            mockMvc.perform(get("/api/calculator/public/style/INVALID_STYLE"))
                    .andExpect(status().isBadRequest());
        }
    }
}
