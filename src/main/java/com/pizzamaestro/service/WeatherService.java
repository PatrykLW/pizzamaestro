package com.pizzamaestro.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serwis pogodowy wykorzystujący darmowe API Open-Meteo.
 * 
 * Open-Meteo nie wymaga klucza API i jest całkowicie darmowy.
 * Dokumentacja: https://open-meteo.com/
 * 
 * Pogoda wpływa na fermentację:
 * - Wysoka temperatura = szybsza fermentacja = mniej drożdży
 * - Wysoka wilgotność = wolniejsze schnięcie ciasta
 * - Niskie ciśnienie = szybsza fermentacja (mniej tlenu)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Open-Meteo API - darmowe, bez klucza
    private static final String OPEN_METEO_URL = 
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}" +
            "&current=temperature_2m,relative_humidity_2m,surface_pressure,weather_code" +
            "&hourly=temperature_2m,relative_humidity_2m" +
            "&forecast_days=2&timezone=auto";
    
    // Geocoding API - do konwersji nazwy miasta na współrzędne
    private static final String GEOCODING_URL = 
            "https://geocoding-api.open-meteo.com/v1/search?name={city}&count=1&language=pl&format=json";
    
    /**
     * Pobiera aktualną pogodę dla podanych współrzędnych.
     * 
     * @param latitude szerokość geograficzna (-90 do 90)
     * @param longitude długość geograficzna (-180 do 180)
     * @return dane pogodowe
     * @throws IllegalArgumentException gdy współrzędne są poza zakresem
     */
    @Cacheable(value = "weather", key = "#latitude + '_' + #longitude")
    public WeatherData getWeatherByCoordinates(double latitude, double longitude) {
        // Walidacja współrzędnych
        validateCoordinates(latitude, longitude);
        
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║ 🌤️  POBIERANIE DANYCH POGODOWYCH                         ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📍 Współrzędne: lat={}, lon={}", latitude, longitude);
        
        try {
            String url = OPEN_METEO_URL
                    .replace("{lat}", String.valueOf(latitude))
                    .replace("{lon}", String.valueOf(longitude));
            
            OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);
            
            if (response != null && response.getCurrent() != null) {
                WeatherData weather = mapToWeatherData(response, latitude, longitude);
                
                log.info("✅ Pogoda pobrana pomyślnie:");
                log.info("   🌡️  Temperatura: {}°C", weather.getTemperature());
                log.info("   💧 Wilgotność: {}%", weather.getHumidity());
                log.info("   📊 Ciśnienie: {} hPa", weather.getPressure());
                log.info("   ☁️  Opis: {}", weather.getDescription());
                log.info("   📈 Wpływ na fermentację: {}%", 
                        String.format("%.1f", (weather.getFermentationFactor() - 1) * 100));
                
                return weather;
            }
            
            log.warn("⚠️ Brak danych pogodowych w odpowiedzi");
            return getDefaultWeather();
            
        } catch (Exception e) {
            log.error("❌ Błąd pobierania pogody: {}", e.getMessage());
            return getDefaultWeather();
        }
    }
    
    /**
     * Pobiera pogodę dla nazwy miasta.
     * 
     * @param cityName nazwa miasta (min. 2 znaki)
     * @return dane pogodowe
     * @throws IllegalArgumentException gdy nazwa miasta jest nieprawidłowa
     */
    public WeatherData getWeatherByCity(String cityName) {
        // Walidacja nazwy miasta
        if (cityName == null || cityName.trim().isEmpty()) {
            log.error("❌ Nazwa miasta nie może być pusta");
            throw new IllegalArgumentException("Nazwa miasta nie może być pusta");
        }
        
        String trimmedCity = cityName.trim();
        if (trimmedCity.length() < 2) {
            log.error("❌ Nazwa miasta zbyt krótka: '{}'", trimmedCity);
            throw new IllegalArgumentException("Nazwa miasta musi mieć co najmniej 2 znaki");
        }
        
        if (trimmedCity.length() > 100) {
            log.error("❌ Nazwa miasta zbyt długa: {} znaków", trimmedCity.length());
            throw new IllegalArgumentException("Nazwa miasta nie może przekraczać 100 znaków");
        }
        
        log.info("🔍 Szukam współrzędnych dla miasta: '{}'", trimmedCity);
        
        try {
            String url = GEOCODING_URL.replace("{city}", cityName);
            GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class);
            
            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                GeocodingResult city = response.getResults().get(0);
                log.info("✅ Znaleziono: {} ({}, {})", 
                        city.getName(), city.getLatitude(), city.getLongitude());
                
                WeatherData weather = getWeatherByCoordinates(city.getLatitude(), city.getLongitude());
                weather.setCityName(city.getName());
                weather.setCountry(city.getCountry());
                return weather;
            }
            
            log.warn("⚠️ Nie znaleziono miasta: {}", cityName);
            return getDefaultWeather();
            
        } catch (Exception e) {
            log.error("❌ Błąd wyszukiwania miasta: {}", e.getMessage());
            return getDefaultWeather();
        }
    }
    
    /**
     * Oblicza wpływ pogody na fermentację.
     * 
     * WAŻNE: Używa temperatury WEWNĘTRZNEJ (w pomieszczeniu), nie zewnętrznej!
     * 
     * Czynniki wpływające:
     * 1. Temperatura wewnętrzna - główny czynnik (Q10 ≈ 2 dla drożdży)
     * 2. Wilgotność wewnętrzna - wpływa na schnięcie powierzchni ciasta
     * 3. Ciśnienie atmosferyczne - wpływa na aktywność drożdży
     */
    public FermentationAdjustment calculateFermentationAdjustment(WeatherData weather) {
        log.info("🧮 Obliczam wpływ pogody na fermentację...");
        log.info("🏠 Używam temperatury WEWNĘTRZNEJ: {}°C (zewn: {}°C)", 
                String.format("%.1f", weather.getIndoorTemperature()),
                String.format("%.1f", weather.getTemperature()));
        
        // Używamy temperatury WEWNĘTRZNEJ do obliczeń!
        double indoorTemp = weather.getIndoorTemperature();
        double indoorHumidity = weather.getIndoorHumidity();
        
        double tempFactor = calculateTemperatureFactor(indoorTemp);
        double humidityFactor = calculateHumidityFactor(indoorHumidity);
        double pressureFactor = calculatePressureFactor(weather.getPressure());
        
        // Łączny współczynnik
        double totalFactor = tempFactor * humidityFactor * pressureFactor;
        
        // Zalecenia
        List<String> recommendations = generateWeatherRecommendations(weather);
        
        FermentationAdjustment adjustment = FermentationAdjustment.builder()
                .temperatureFactor(tempFactor)
                .humidityFactor(humidityFactor)
                .pressureFactor(pressureFactor)
                .totalFactor(totalFactor)
                .yeastAdjustmentPercent((1 - totalFactor) * 100)
                .fermentationTimeAdjustmentPercent((totalFactor - 1) * 100)
                .recommendations(recommendations)
                .indoorTemperature(indoorTemp)
                .indoorHumidity(indoorHumidity)
                .build();
        
        log.info("📊 Wyniki analizy pogodowej (dla temp. wewnętrznej {}°C):", String.format("%.1f", indoorTemp));
        log.info("   🌡️  Współczynnik temperatury: {}", String.format("%.3f", tempFactor));
        log.info("   💧 Współczynnik wilgotności: {}", String.format("%.3f", humidityFactor));
        log.info("   📊 Współczynnik ciśnienia: {}", String.format("%.3f", pressureFactor));
        log.info("   📈 Łączny współczynnik: {}", String.format("%.3f", totalFactor));
        log.info("   🦠 Korekta drożdży: {}%", String.format("%.1f", adjustment.getYeastAdjustmentPercent()));
        
        return adjustment;
    }
    
    /**
     * Współczynnik temperatury - Q10 model.
     * Referencyjna temperatura: 20°C
     */
    private double calculateTemperatureFactor(double temperature) {
        double referenceTemp = 20.0;
        double q10 = 2.0; // Drożdże podwajają aktywność na każde 10°C
        
        return Math.pow(q10, (temperature - referenceTemp) / 10.0);
    }
    
    /**
     * Współczynnik wilgotności.
     * Wysoka wilgotność = wolniejsze schnięcie = lepsza fermentacja
     */
    private double calculateHumidityFactor(double humidity) {
        // Optymalna wilgotność: 65-75%
        if (humidity >= 65 && humidity <= 75) {
            return 1.0;
        } else if (humidity < 65) {
            // Sucho - ciasto może schnąć
            return 1.0 - (65 - humidity) * 0.003;
        } else {
            // Wilgotno - minimalna korekta
            return 1.0 + (humidity - 75) * 0.001;
        }
    }
    
    /**
     * Współczynnik ciśnienia atmosferycznego.
     * Niskie ciśnienie = mniej tlenu = szybsza fermentacja
     */
    private double calculatePressureFactor(double pressure) {
        double referencePressure = 1013.25; // hPa na poziomie morza
        
        // Bardzo subtelny wpływ
        return 1.0 + (referencePressure - pressure) * 0.0002;
    }
    
    /**
     * Generuje zalecenia na podstawie pogody (używa temperatury WEWNĘTRZNEJ).
     */
    private List<String> generateWeatherRecommendations(WeatherData weather) {
        java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        double indoorTemp = weather.getIndoorTemperature();
        double indoorHumidity = weather.getIndoorHumidity();
        double outdoorTemp = weather.getTemperature();
        
        // Temperatura wewnętrzna
        if (indoorTemp > 26) {
            recommendations.add("🔥 Ciepło w pomieszczeniu (" + String.format("%.0f", indoorTemp) + "°C). Fermentacja będzie szybsza.");
            recommendations.add("💧 Użyj zimniejszej wody (15-18°C) do ciasta.");
            recommendations.add("🦠 Algorytm automatycznie zmniejszył ilość drożdży.");
        } else if (indoorTemp > 24) {
            recommendations.add("☀️ Temperatura pokojowa " + String.format("%.0f", indoorTemp) + "°C - idealna dla szybszej fermentacji.");
        } else if (indoorTemp < 19) {
            recommendations.add("❄️ Chłodno w pomieszczeniu (" + String.format("%.0f", indoorTemp) + "°C) - ciasto będzie rosnąć wolniej.");
            recommendations.add("🦠 Algorytm automatycznie zwiększył ilość drożdży lub wydłużył fermentację.");
            recommendations.add("🌡️ Użyj cieplejszej wody (30-35°C) do rozpuszczenia drożdży.");
        }
        
        // Wilgotność wewnętrzna
        if (indoorHumidity < 40) {
            recommendations.add("🏜️ Suche powietrze w pomieszczeniu (" + String.format("%.0f", indoorHumidity) + "%). Przykryj ciasto szczelnie folią.");
            recommendations.add("📉 Rozważ zmniejszenie hydratacji o 1-2%.");
        } else if (indoorHumidity > 70) {
            recommendations.add("💦 Wysoka wilgotność (" + String.format("%.0f", indoorHumidity) + "%) - mąka może być bardziej wilgotna.");
            recommendations.add("📉 Rozważ zmniejszenie hydratacji o 1-2%.");
        }
        
        // Ciśnienie (burza)
        if (weather.getPressure() < 1000) {
            recommendations.add("🌧️ Niskie ciśnienie (możliwa burza) - ciasto może rosnąć szybciej.");
        }
        
        // Komentarz o różnicy temperatur zewn./wewn.
        if (outdoorTemp < 10) {
            recommendations.add("🏠 Na zewnątrz " + String.format("%.0f", outdoorTemp) + "°C, ale w ogrzewanym pomieszczeniu ~" 
                    + String.format("%.0f", indoorTemp) + "°C. Algorytm uwzględnia temperaturę wewnętrzną.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ Idealne warunki do robienia pizzy! Temperatura " 
                    + String.format("%.0f", indoorTemp) + "°C, wilgotność " + String.format("%.0f", indoorHumidity) + "%.");
        }
        
        return recommendations;
    }
    
    /**
     * Mapuje odpowiedź API na WeatherData.
     */
    private WeatherData mapToWeatherData(OpenMeteoResponse response, double lat, double lon) {
        OpenMeteoResponse.CurrentWeather current = response.getCurrent();
        
        double outdoorTemp = current.getTemperature();
        double outdoorHumidity = current.getHumidity();
        
        // Oblicz temperaturę i wilgotność wewnętrzną
        double indoorTemp = calculateIndoorTemperature(outdoorTemp);
        double indoorHumidity = calculateIndoorHumidity(outdoorHumidity, outdoorTemp, indoorTemp);
        
        log.debug("🏠 Przeliczenie na warunki wewnętrzne:");
        log.debug("   Zewnątrz: {}°C, {}%", outdoorTemp, outdoorHumidity);
        log.debug("   Wewnątrz: {}°C, {}%", String.format("%.1f", indoorTemp), 
                  String.format("%.1f", indoorHumidity));
        
        return WeatherData.builder()
                .latitude(lat)
                .longitude(lon)
                .temperature(outdoorTemp)
                .indoorTemperature(indoorTemp)
                .humidity(outdoorHumidity)
                .indoorHumidity(indoorHumidity)
                .pressure(current.getPressure())
                .weatherCode(current.getWeatherCode())
                .description(getWeatherDescription(current.getWeatherCode()))
                // Współczynnik fermentacji liczymy dla TEMPERATURY WEWNĘTRZNEJ!
                .fermentationFactor(calculateTemperatureFactor(indoorTemp))
                .fetchedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Oblicza szacowaną temperaturę wewnętrzną na podstawie temperatury zewnętrznej.
     * 
     * Założenia:
     * - W zimie (temp < 15°C) pomieszczenia są ogrzewane do ~20-22°C
     * - W lecie (temp > 25°C) pomieszczenia mogą być chłodzone lub cieplejsze
     * - Zakładamy typowe mieszkanie/dom bez klimatyzacji
     */
    private double calculateIndoorTemperature(double outdoorTemp) {
        // Typowa temperatura pokojowa w ogrzewanym mieszkaniu: 20-22°C
        double typicalIndoorTemp = 21.0;
        
        if (outdoorTemp < 10) {
            // Zimno na zewnątrz - mieszkanie ogrzewane do ~20-22°C
            // Im zimniej, tym bardziej zbliżamy się do typowej temp. pokojowej
            return typicalIndoorTemp;
        } else if (outdoorTemp < 15) {
            // Chłodno - lekkie ogrzewanie
            return Math.max(outdoorTemp + 5, typicalIndoorTemp - 1);
        } else if (outdoorTemp <= 25) {
            // Komfortowo - temperatura wewnętrzna zbliżona do zewnętrznej
            // z lekkim buforem (w domu trochę cieplej/chłodniej)
            return (outdoorTemp + typicalIndoorTemp) / 2;
        } else if (outdoorTemp <= 30) {
            // Ciepło - bez klimatyzacji w domu może być nawet cieplej
            // ale zazwyczaj trochę chłodniej dzięki ścianom
            return outdoorTemp - 2;
        } else {
            // Upał - bez klimatyzacji w domu jest gorąco, ale mniej niż na zewnątrz
            return outdoorTemp - 3;
        }
    }
    
    /**
     * Oblicza szacowaną wilgotność wewnętrzną.
     * 
     * Założenia:
     * - W ogrzewanych pomieszczeniach wilgotność spada (suche powietrze)
     * - W lecie wilgotność wewnętrzna zbliżona do zewnętrznej
     */
    private double calculateIndoorHumidity(double outdoorHumidity, double outdoorTemp, double indoorTemp) {
        if (outdoorTemp < 10) {
            // Ogrzewanie znacząco obniża wilgotność
            // Typowa wilgotność w ogrzewanym mieszkaniu: 30-50%
            double heatingDrop = (10 - outdoorTemp) * 2; // Im zimniej, tym bardziej sucho
            return Math.max(30, Math.min(50, outdoorHumidity - heatingDrop));
        } else if (outdoorTemp < 20) {
            // Lekkie ogrzewanie - umiarkowany spadek wilgotności
            return Math.max(35, outdoorHumidity - 10);
        } else {
            // Bez ogrzewania - wilgotność podobna do zewnętrznej
            // z lekką korektą wynikającą z zamkniętego pomieszczenia
            return outdoorHumidity * 0.95;
        }
    }
    
    /**
     * Domyślna pogoda gdy API niedostępne.
     */
    private WeatherData getDefaultWeather() {
        log.info("📌 Używam domyślnych wartości pogodowych (22°C wewnątrz, 50% wilgotności)");
        return WeatherData.builder()
                .temperature(18.0)           // Domyślna temp. zewnętrzna
                .indoorTemperature(22.0)     // Typowa temp. pokojowa
                .humidity(55.0)              // Domyślna wilgotność zewnętrzna
                .indoorHumidity(50.0)        // Typowa wilgotność w domu
                .pressure(1013.0)
                .description("Brak danych - używam typowych wartości dla pomieszczenia")
                .fermentationFactor(1.0)
                .fetchedAt(LocalDateTime.now())
                .isDefault(true)
                .build();
    }
    
    /**
     * Opis pogody na podstawie kodu WMO.
     */
    private String getWeatherDescription(int code) {
        return switch (code) {
            case 0 -> "Bezchmurnie ☀️";
            case 1, 2, 3 -> "Częściowe zachmurzenie ⛅";
            case 45, 48 -> "Mgła 🌫️";
            case 51, 53, 55 -> "Mżawka 🌧️";
            case 61, 63, 65 -> "Deszcz 🌧️";
            case 71, 73, 75 -> "Śnieg ❄️";
            case 80, 81, 82 -> "Przelotne opady 🌦️";
            case 95, 96, 99 -> "Burza ⛈️";
            default -> "Nieznane warunki";
        };
    }
    
    /**
     * Waliduje współrzędne geograficzne.
     * 
     * @param latitude szerokość geograficzna
     * @param longitude długość geograficzna
     * @throws IllegalArgumentException gdy współrzędne są poza zakresem
     */
    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            log.error("❌ Nieprawidłowa szerokość geograficzna: {} (musi być -90 do 90)", latitude);
            throw new IllegalArgumentException(
                    String.format("Szerokość geograficzna musi być w zakresie -90 do 90, podano: %.6f", latitude));
        }
        
        if (longitude < -180.0 || longitude > 180.0) {
            log.error("❌ Nieprawidłowa długość geograficzna: {} (musi być -180 do 180)", longitude);
            throw new IllegalArgumentException(
                    String.format("Długość geograficzna musi być w zakresie -180 do 180, podano: %.6f", longitude));
        }
        
        log.debug("✅ Współrzędne zwalidowane: lat={}, lon={}", latitude, longitude);
    }
    
    // ==================== DTOs ====================
    
    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WeatherData {
        private double latitude;
        private double longitude;
        private String cityName;
        private String country;
        private double temperature;        // Temperatura zewnętrzna
        private double indoorTemperature;  // Szacowana temperatura wewnętrzna
        private double indoorHumidity;     // Szacowana wilgotność wewnętrzna
        private double humidity;
        private double pressure;
        private int weatherCode;
        private String description;
        private double fermentationFactor;
        private LocalDateTime fetchedAt;
        private boolean isDefault;
    }
    
    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FermentationAdjustment {
        private double temperatureFactor;
        private double humidityFactor;
        private double pressureFactor;
        private double totalFactor;
        private double yeastAdjustmentPercent;
        private double fermentationTimeAdjustmentPercent;
        private double indoorTemperature;      // Szacowana temp. wewnętrzna
        private double indoorHumidity;         // Szacowana wilgotność wewnętrzna
        private List<String> recommendations;
    }
    
    // ==================== API Response DTOs ====================
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenMeteoResponse {
        private double latitude;
        private double longitude;
        private String timezone;
        
        @JsonProperty("current")
        private CurrentWeather current;
        
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class CurrentWeather {
            @JsonProperty("temperature_2m")
            private double temperature;
            
            @JsonProperty("relative_humidity_2m")
            private double humidity;
            
            @JsonProperty("surface_pressure")
            private double pressure;
            
            @JsonProperty("weather_code")
            private int weatherCode;
        }
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocodingResponse {
        private List<GeocodingResult> results;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocodingResult {
        private String name;
        private double latitude;
        private double longitude;
        private String country;
        
        @JsonProperty("country_code")
        private String countryCode;
    }
}
