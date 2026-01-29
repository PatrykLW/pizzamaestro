package com.pizzamaestro.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla WeatherService.
 */
@SpringBootTest
@ActiveProfiles("test")
class WeatherServiceTest {

    @Autowired
    private WeatherService weatherService;

    // ==================== TESTY WALIDACJI WSPÓŁRZĘDNYCH ====================

    @Nested
    @DisplayName("Walidacja współrzędnych")
    class CoordinateValidationTests {

        @Test
        @DisplayName("Powinien zaakceptować poprawne współrzędne")
        void shouldAcceptValidCoordinates() {
            // Warszawa
            assertDoesNotThrow(() -> 
                    weatherService.getWeatherByCoordinates(52.2297, 21.0122));
        }

        @Test
        @DisplayName("Powinien zaakceptować współrzędne na krawędzi zakresu")
        void shouldAcceptEdgeCoordinates() {
            // Biegun północny
            assertDoesNotThrow(() -> 
                    weatherService.getWeatherByCoordinates(90.0, 0.0));

            // Biegun południowy
            assertDoesNotThrow(() -> 
                    weatherService.getWeatherByCoordinates(-90.0, 0.0));

            // Linia zmiany daty
            assertDoesNotThrow(() -> 
                    weatherService.getWeatherByCoordinates(0.0, 180.0));
            assertDoesNotThrow(() -> 
                    weatherService.getWeatherByCoordinates(0.0, -180.0));
        }

        @Test
        @DisplayName("Powinien odrzucić szerokość geograficzną > 90")
        void shouldRejectLatitudeAbove90() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCoordinates(91.0, 0.0));

            assertTrue(ex.getMessage().contains("Szerokość geograficzna"));
        }

        @Test
        @DisplayName("Powinien odrzucić szerokość geograficzną < -90")
        void shouldRejectLatitudeBelow90() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCoordinates(-91.0, 0.0));

            assertTrue(ex.getMessage().contains("Szerokość geograficzna"));
        }

        @Test
        @DisplayName("Powinien odrzucić długość geograficzną > 180")
        void shouldRejectLongitudeAbove180() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCoordinates(0.0, 181.0));

            assertTrue(ex.getMessage().contains("Długość geograficzna"));
        }

        @Test
        @DisplayName("Powinien odrzucić długość geograficzną < -180")
        void shouldRejectLongitudeBelow180() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCoordinates(0.0, -181.0));

            assertTrue(ex.getMessage().contains("Długość geograficzna"));
        }
    }

    // ==================== TESTY WALIDACJI MIASTA ====================

    @Nested
    @DisplayName("Walidacja nazwy miasta")
    class CityValidationTests {

        @Test
        @DisplayName("Powinien zaakceptować poprawną nazwę miasta")
        void shouldAcceptValidCityName() {
            assertDoesNotThrow(() -> 
                    weatherService.getWeatherByCity("Warszawa"));
        }

        @Test
        @DisplayName("Powinien odrzucić null jako nazwę miasta")
        void shouldRejectNullCityName() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCity(null));

            assertTrue(ex.getMessage().contains("pusta"));
        }

        @Test
        @DisplayName("Powinien odrzucić pustą nazwę miasta")
        void shouldRejectEmptyCityName() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCity(""));

            assertTrue(ex.getMessage().contains("pusta"));
        }

        @Test
        @DisplayName("Powinien odrzucić nazwę miasta z samymi spacjami")
        void shouldRejectWhitespaceCityName() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCity("   "));

            assertTrue(ex.getMessage().contains("pusta"));
        }

        @Test
        @DisplayName("Powinien odrzucić zbyt krótką nazwę miasta")
        void shouldRejectTooShortCityName() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCity("A"));

            assertTrue(ex.getMessage().contains("2 znaki"));
        }

        @Test
        @DisplayName("Powinien odrzucić zbyt długą nazwę miasta")
        void shouldRejectTooLongCityName() {
            String longName = "A".repeat(101);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                    weatherService.getWeatherByCity(longName));

            assertTrue(ex.getMessage().contains("100 znaków"));
        }
    }

    // ==================== TESTY POBIERANIA POGODY ====================

    @Nested
    @DisplayName("Pobieranie danych pogodowych")
    class WeatherDataTests {

        @Test
        @DisplayName("Powinien zwrócić dane pogodowe dla Warszawy")
        void shouldReturnWeatherDataForWarsaw() {
            WeatherService.WeatherData weather = weatherService.getWeatherByCoordinates(52.2297, 21.0122);

            assertNotNull(weather);
            assertNotNull(weather.getFetchedAt());
            // Temperatura powinna być w rozsądnym zakresie
            assertTrue(weather.getTemperature() > -60 && weather.getTemperature() < 60,
                    "Temperatura powinna być w zakresie -60°C do 60°C");
            // Wilgotność powinna być 0-100%
            assertTrue(weather.getHumidity() >= 0 && weather.getHumidity() <= 100,
                    "Wilgotność powinna być w zakresie 0-100%");
            // Ciśnienie powinno być w rozsądnym zakresie
            assertTrue(weather.getPressure() > 800 && weather.getPressure() < 1100,
                    "Ciśnienie powinno być w zakresie 800-1100 hPa");
        }

        @Test
        @DisplayName("Powinien zwrócić dane pogodowe dla miasta")
        void shouldReturnWeatherDataForCity() {
            WeatherService.WeatherData weather = weatherService.getWeatherByCity("Kraków");

            assertNotNull(weather);
            // Może ustawić nazwę miasta z API
            assertNotNull(weather.getFetchedAt());
        }

        @Test
        @DisplayName("Powinien zwrócić domyślne dane dla nieistniejącego miasta")
        void shouldReturnDefaultDataForNonExistentCity() {
            WeatherService.WeatherData weather = weatherService.getWeatherByCity("NieistniejaceMiasto12345xyz");

            assertNotNull(weather);
            assertTrue(weather.isDefault());
        }
    }

    // ==================== TESTY OBLICZANIA WPŁYWU NA FERMENTACJĘ ====================

    @Nested
    @DisplayName("Wpływ pogody na fermentację")
    class FermentationAdjustmentTests {

        @Test
        @DisplayName("Powinien obliczyć wpływ wysokiej temperatury")
        void shouldCalculateHighTemperatureImpact() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(30.0)
                    .humidity(60.0)
                    .pressure(1013.0)
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            assertNotNull(adjustment);
            // Wysoka temperatura = szybsza fermentacja = temperatureFactor > 1
            assertTrue(adjustment.getTemperatureFactor() > 1.0,
                    "Przy 30°C temperatura powinna przyspieszać fermentację");
            // Powinien zalecać zmniejszenie drożdży
            assertTrue(adjustment.getYeastAdjustmentPercent() < 0,
                    "Przy wysokiej temperaturze powinno zalecać mniej drożdży");
        }

        @Test
        @DisplayName("Powinien obliczyć wpływ niskiej temperatury")
        void shouldCalculateLowTemperatureImpact() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(10.0)
                    .humidity(60.0)
                    .pressure(1013.0)
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            assertNotNull(adjustment);
            // Niska temperatura = wolniejsza fermentacja = temperatureFactor < 1
            assertTrue(adjustment.getTemperatureFactor() < 1.0,
                    "Przy 10°C temperatura powinna spowalniać fermentację");
            // Powinien zalecać zwiększenie drożdży
            assertTrue(adjustment.getYeastAdjustmentPercent() > 0,
                    "Przy niskiej temperaturze powinno zalecać więcej drożdży");
        }

        @Test
        @DisplayName("Powinien obliczyć wpływ optymalnej temperatury")
        void shouldCalculateOptimalTemperatureImpact() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(20.0) // temperatura referencyjna
                    .humidity(70.0)    // optymalna wilgotność
                    .pressure(1013.25) // ciśnienie referencyjne
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            assertNotNull(adjustment);
            // Przy optymalnych warunkach factor powinien być blisko 1
            assertEquals(1.0, adjustment.getTemperatureFactor(), 0.01);
            assertEquals(1.0, adjustment.getHumidityFactor(), 0.01);
            assertEquals(1.0, adjustment.getPressureFactor(), 0.01);
        }

        @Test
        @DisplayName("Powinien wygenerować rekomendacje dla gorącej pogody")
        void shouldGenerateRecommendationsForHotWeather() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(32.0)
                    .humidity(45.0)
                    .pressure(1013.0)
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            assertNotNull(adjustment.getRecommendations());
            assertFalse(adjustment.getRecommendations().isEmpty());
            // Powinny być rekomendacje dotyczące temperatury
            boolean hasTemperatureRecommendation = adjustment.getRecommendations().stream()
                    .anyMatch(r -> r.contains("Wysoka temperatura") || r.contains("Ciepło"));
            assertTrue(hasTemperatureRecommendation);
        }

        @Test
        @DisplayName("Powinien wygenerować rekomendacje dla suchej pogody")
        void shouldGenerateRecommendationsForDryWeather() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(22.0)
                    .humidity(30.0) // bardzo sucho
                    .pressure(1013.0)
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            assertNotNull(adjustment.getRecommendations());
            // Powinny być rekomendacje dotyczące wilgotności
            boolean hasHumidityRecommendation = adjustment.getRecommendations().stream()
                    .anyMatch(r -> r.contains("sucho") || r.contains("Bardzo sucho"));
            assertTrue(hasHumidityRecommendation);
        }

        @Test
        @DisplayName("Powinien wygenerować pozytywną rekomendację dla idealnych warunków")
        void shouldGeneratePositiveRecommendationForIdealConditions() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(22.0)
                    .humidity(68.0)
                    .pressure(1010.0)
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            assertNotNull(adjustment.getRecommendations());
            // Przy idealnych warunkach powinien być pozytywny komunikat
            boolean hasPositiveMessage = adjustment.getRecommendations().stream()
                    .anyMatch(r -> r.contains("Idealne") || r.contains("✅"));
            assertTrue(hasPositiveMessage);
        }

        @Test
        @DisplayName("Powinien obliczyć wpływ niskiego ciśnienia")
        void shouldCalculateLowPressureImpact() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(22.0)
                    .humidity(60.0)
                    .pressure(995.0) // niskie ciśnienie (burza)
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            assertNotNull(adjustment);
            // Niskie ciśnienie = szybsza fermentacja
            assertTrue(adjustment.getPressureFactor() > 1.0);
            // Powinny być rekomendacje dotyczące ciśnienia/burzy
            boolean hasPressureRecommendation = adjustment.getRecommendations().stream()
                    .anyMatch(r -> r.contains("ciśnienie") || r.contains("burza"));
            assertTrue(hasPressureRecommendation);
        }
    }

    // ==================== TESTY ŁĄCZNEGO WPŁYWU ====================

    @Nested
    @DisplayName("Łączny wpływ czynników")
    class TotalFactorTests {

        @Test
        @DisplayName("Łączny współczynnik powinien być iloczynem czynników")
        void totalFactorShouldBeProductOfFactors() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(25.0)
                    .humidity(50.0)
                    .pressure(1000.0)
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            double expectedTotal = adjustment.getTemperatureFactor() * 
                                   adjustment.getHumidityFactor() * 
                                   adjustment.getPressureFactor();

            assertEquals(expectedTotal, adjustment.getTotalFactor(), 0.001);
        }

        @Test
        @DisplayName("Korekta drożdży powinna odpowiadać łącznemu współczynnikowi")
        void yeastAdjustmentShouldMatchTotalFactor() {
            WeatherService.WeatherData weather = WeatherService.WeatherData.builder()
                    .temperature(28.0)
                    .humidity(60.0)
                    .pressure(1013.0)
                    .build();

            WeatherService.FermentationAdjustment adjustment = 
                    weatherService.calculateFermentationAdjustment(weather);

            // yeastAdjustmentPercent = (1 - totalFactor) * 100
            double expectedYeastAdjustment = (1 - adjustment.getTotalFactor()) * 100;

            assertEquals(expectedYeastAdjustment, adjustment.getYeastAdjustmentPercent(), 0.01);
        }
    }
}
