package com.pizzamaestro.controller;

import com.pizzamaestro.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler pogody do integracji z kalkulatorem.
 * Wykorzystuje darmowe API Open-Meteo.
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Pogoda", description = "Dane pogodowe wpływające na fermentację")
public class WeatherController {
    
    private final WeatherService weatherService;
    
    /**
     * Pobiera pogodę na podstawie współrzędnych GPS.
     */
    @GetMapping("/coordinates")
    @Operation(summary = "Pobierz pogodę po współrzędnych")
    public ResponseEntity<WeatherService.WeatherData> getWeatherByCoordinates(
            @RequestParam 
            @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być >= -90")
            @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być <= 90")
            @Parameter(description = "Szerokość geograficzna (-90 do 90)")
            double latitude,
            
            @RequestParam 
            @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być >= -180")
            @DecimalMax(value = "180.0", message = "Długość geograficzna musi być <= 180")
            @Parameter(description = "Długość geograficzna (-180 do 180)")
            double longitude) {
        
        log.info("📍 Żądanie pogody dla współrzędnych: lat={}, lon={}", latitude, longitude);
        log.debug("   Walidacja: latitude w zakresie [-90, 90], longitude w zakresie [-180, 180]");
        
        WeatherService.WeatherData weather = weatherService.getWeatherByCoordinates(latitude, longitude);
        
        log.debug("   Pobrano pogodę: temp={}°C, humidity={}%", 
                weather.getTemperature(), weather.getHumidity());
        
        return ResponseEntity.ok(weather);
    }
    
    /**
     * Pobiera pogodę na podstawie nazwy miasta.
     */
    @GetMapping("/city")
    @Operation(summary = "Pobierz pogodę dla miasta")
    public ResponseEntity<WeatherService.WeatherData> getWeatherByCity(
            @RequestParam 
            @NotBlank(message = "Nazwa miasta nie może być pusta")
            @Size(min = 2, max = 100, message = "Nazwa miasta musi mieć od 2 do 100 znaków")
            @Parameter(description = "Nazwa miasta (np. Warszawa, Kraków)")
            String name) {
        
        log.info("🏙️ Żądanie pogody dla miasta: '{}'", name);
        
        WeatherService.WeatherData weather = weatherService.getWeatherByCity(name);
        
        log.debug("   Znaleziono: {} - temp={}°C", 
                weather.getCityName() != null ? weather.getCityName() : name, 
                weather.getTemperature());
        
        return ResponseEntity.ok(weather);
    }
    
    /**
     * Oblicza wpływ pogody na fermentację.
     */
    @GetMapping("/fermentation-adjustment")
    @Operation(summary = "Oblicz wpływ pogody na fermentację")
    public ResponseEntity<WeatherService.FermentationAdjustment> getFermentationAdjustment(
            @RequestParam 
            @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być >= -90")
            @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być <= 90")
            double latitude,
            
            @RequestParam 
            @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być >= -180")
            @DecimalMax(value = "180.0", message = "Długość geograficzna musi być <= 180")
            double longitude) {
        
        log.info("🧮 Obliczanie wpływu pogody na fermentację dla: lat={}, lon={}", latitude, longitude);
        
        WeatherService.WeatherData weather = weatherService.getWeatherByCoordinates(latitude, longitude);
        WeatherService.FermentationAdjustment adjustment = weatherService.calculateFermentationAdjustment(weather);
        
        log.debug("   Wpływ na fermentację: totalFactor={}, yeastAdjustment={}%", 
                String.format("%.3f", adjustment.getTotalFactor()),
                String.format("%.1f", adjustment.getYeastAdjustmentPercent()));
        
        return ResponseEntity.ok(adjustment);
    }
    
    /**
     * Pobiera pogodę i wpływ na fermentację razem.
     */
    @GetMapping("/full-analysis")
    @Operation(summary = "Pełna analiza pogodowa z wpływem na fermentację")
    public ResponseEntity<FullWeatherAnalysis> getFullAnalysis(
            @RequestParam 
            @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być >= -90")
            @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być <= 90")
            double latitude,
            
            @RequestParam 
            @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być >= -180")
            @DecimalMax(value = "180.0", message = "Długość geograficzna musi być <= 180")
            double longitude) {
        
        log.info("📊 Pełna analiza pogodowa dla: lat={}, lon={}", latitude, longitude);
        
        WeatherService.WeatherData weather = weatherService.getWeatherByCoordinates(latitude, longitude);
        WeatherService.FermentationAdjustment adjustment = weatherService.calculateFermentationAdjustment(weather);
        
        log.info("   ✅ Analiza zakończona: temp={}°C, humidity={}%, wpływ={}%", 
                weather.getTemperature(), 
                weather.getHumidity(),
                String.format("%.1f", (adjustment.getTotalFactor() - 1) * 100));
        
        return ResponseEntity.ok(FullWeatherAnalysis.builder()
                .weather(weather)
                .fermentationAdjustment(adjustment)
                .build());
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FullWeatherAnalysis {
        private WeatherService.WeatherData weather;
        private WeatherService.FermentationAdjustment fermentationAdjustment;
    }
}
