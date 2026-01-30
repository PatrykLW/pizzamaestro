package com.pizzamaestro.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serwis korekt środowiskowych dla obliczeń ciasta.
 * 
 * Uwzględnia wpływ:
 * - Wilgotności powietrza na absorpcję mąki
 * - Wysokości npm na fermentację (ciśnienie atmosferyczne)
 * - Temperatury na aktywność drożdży
 */
@Service
@Slf4j
public class EnvironmentalCorrectionService {
    
    // Stałe bazowe
    private static final double BASE_HUMIDITY = 50.0; // % wilgotności powietrza
    private static final int BASE_ALTITUDE = 0; // m npm
    private static final double BASE_PRESSURE = 1013.25; // hPa na poziomie morza
    
    /**
     * Oblicza korekty środowiskowe dla receptury.
     */
    public EnvironmentalCorrections calculateCorrections(
            Integer ambientHumidity, 
            Integer altitudeMeters,
            Double roomTemperature) {
        
        EnvironmentalCorrections.EnvironmentalCorrectionsBuilder builder = 
                EnvironmentalCorrections.builder();
        
        double humidity = ambientHumidity != null ? ambientHumidity : BASE_HUMIDITY;
        int altitude = altitudeMeters != null ? altitudeMeters : BASE_ALTITUDE;
        double roomTemp = roomTemperature != null ? roomTemperature : 22.0;
        
        // Korekta hydratacji dla wilgotności powietrza
        double hydrationCorrection = calculateHydrationCorrection(humidity);
        builder.hydrationCorrectionPercent(hydrationCorrection);
        
        // Korekta drożdży dla wysokości (niższe ciśnienie = szybsza fermentacja)
        double yeastCorrection = calculateYeastCorrectionForAltitude(altitude);
        builder.yeastCorrectionPercent(yeastCorrection);
        
        // Korekta czasu fermentacji dla wysokości
        double fermentationTimeCorrection = calculateFermentationTimeCorrection(altitude, roomTemp);
        builder.fermentationTimeCorrectionPercent(fermentationTimeCorrection);
        
        // Oblicz ciśnienie atmosferyczne dla wysokości
        double pressure = calculatePressureAtAltitude(altitude);
        builder.estimatedPressureHPa(pressure);
        
        // Generuj rekomendacje
        builder.recommendations(generateRecommendations(humidity, altitude, roomTemp));
        
        log.info("🌍 Korekty środowiskowe: wilgotność={}%, wysokość={}m npm, temp={}°C", 
                humidity, altitude, roomTemp);
        log.info("   📊 Korekty: hydratacja {:+.1f}%, drożdże {:+.1f}%, czas fermentacji {:+.1f}%",
                hydrationCorrection, yeastCorrection, fermentationTimeCorrection);
        
        return builder.build();
    }
    
    /**
     * Korekta hydratacji dla wilgotności powietrza.
     * 
     * Wysoka wilgotność = mąka wchłonęła wilgoć z powietrza = mniej wody potrzeba
     * Niska wilgotność = mąka jest bardziej sucha = więcej wody potrzeba
     */
    private double calculateHydrationCorrection(double humidity) {
        // Korekta: +/- 0.5% hydratacji na każde 10% różnicy od bazowej wilgotności
        double humidityDiff = humidity - BASE_HUMIDITY;
        double correction = -humidityDiff * 0.05; // Odwrotna zależność
        
        // Ogranicz do rozsądnego zakresu
        return Math.max(-3.0, Math.min(3.0, correction));
    }
    
    /**
     * Korekta ilości drożdży dla wysokości npm.
     * 
     * Na większych wysokościach ciśnienie jest niższe,
     * co przyspiesza fermentację - potrzeba mniej drożdży.
     */
    private double calculateYeastCorrectionForAltitude(int altitude) {
        if (altitude <= 500) {
            return 0.0; // Bez korekty dla niskich wysokości
        }
        
        // Korekta: -5% drożdży na każde 1000m powyżej 500m
        double altitudeAbove500 = altitude - 500;
        double correction = -(altitudeAbove500 / 1000.0) * 5.0;
        
        // Ogranicz do max -20%
        return Math.max(-20.0, correction);
    }
    
    /**
     * Korekta czasu fermentacji dla wysokości i temperatury.
     */
    private double calculateFermentationTimeCorrection(int altitude, double roomTemp) {
        double correction = 0.0;
        
        // Korekta dla wysokości (szybsza fermentacja = krótszy czas)
        if (altitude > 500) {
            double altitudeAbove500 = altitude - 500;
            correction -= (altitudeAbove500 / 1000.0) * 8.0; // -8% na 1000m
        }
        
        // Korekta dla temperatury
        // Baza: 22°C
        double tempDiff = roomTemp - 22.0;
        // +1°C = -5% czasu fermentacji (szybciej)
        // -1°C = +5% czasu fermentacji (wolniej)
        correction -= tempDiff * 5.0;
        
        // Ogranicz do rozsądnego zakresu
        return Math.max(-30.0, Math.min(50.0, correction));
    }
    
    /**
     * Oblicza ciśnienie atmosferyczne dla danej wysokości.
     * Wzór barometryczny.
     */
    private double calculatePressureAtAltitude(int altitude) {
        // Uproszczony wzór: P = P0 * exp(-altitude/8500)
        return BASE_PRESSURE * Math.exp(-altitude / 8500.0);
    }
    
    /**
     * Generuje tekstowe rekomendacje.
     */
    private java.util.List<String> generateRecommendations(
            double humidity, int altitude, double roomTemp) {
        
        java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        // Rekomendacje dla wilgotności
        if (humidity > 70) {
            recommendations.add("🌧️ Wysoka wilgotność powietrza - mąka może być wilgotna. " +
                    "Rozważ delikatne zmniejszenie ilości wody lub dłuższe wyrabianie.");
        } else if (humidity < 30) {
            recommendations.add("☀️ Niska wilgotność powietrza - mąka jest sucha. " +
                    "Możesz potrzebować nieco więcej wody dla odpowiedniej konsystencji.");
        }
        
        // Rekomendacje dla wysokości
        if (altitude > 1000) {
            recommendations.add("🏔️ Wysoka wysokość npm (" + altitude + "m) - " +
                    "fermentacja przebiega szybciej. Zmniejszono ilość drożdży i czas fermentacji.");
        } else if (altitude > 500) {
            recommendations.add("⛰️ Umiarkowana wysokość npm (" + altitude + "m) - " +
                    "niewielka korekta drożdży i czasu fermentacji.");
        }
        
        // Rekomendacje dla temperatury
        if (roomTemp > 28) {
            recommendations.add("🌡️ Wysoka temperatura pokojowa (" + roomTemp + "°C) - " +
                    "fermentacja będzie szybka. Rozważ użycie lodówki lub mniej drożdży.");
        } else if (roomTemp < 18) {
            recommendations.add("❄️ Niska temperatura pokojowa (" + roomTemp + "°C) - " +
                    "fermentacja będzie wolniejsza. Rozważ dłuższy czas lub cieplejsze miejsce.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ Warunki środowiskowe są optymalne dla fermentacji.");
        }
        
        return recommendations;
    }
    
    /**
     * DTO z korektami środowiskowymi.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnvironmentalCorrections {
        /** Korekta hydratacji w % (np. -2% oznacza zmniejszenie hydratacji o 2%) */
        private double hydrationCorrectionPercent;
        
        /** Korekta ilości drożdży w % (np. -10% oznacza zmniejszenie drożdży o 10%) */
        private double yeastCorrectionPercent;
        
        /** Korekta czasu fermentacji w % (np. -15% oznacza skrócenie czasu o 15%) */
        private double fermentationTimeCorrectionPercent;
        
        /** Szacunkowe ciśnienie atmosferyczne w hPa */
        private double estimatedPressureHPa;
        
        /** Rekomendacje tekstowe */
        private java.util.List<String> recommendations;
    }
}
