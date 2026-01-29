package com.pizzamaestro.service;

import com.pizzamaestro.dto.request.CalculationRequest;
import com.pizzamaestro.dto.response.AdvancedCalculationResponse;
import com.pizzamaestro.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Zaawansowany serwis obliczeń ciasta pizzy.
 * 
 * Implementuje profesjonalne algorytmy piekarskie:
 * - DDT (Desired Dough Temperature) - obliczanie temperatury wody
 * - Współczynnik tarcia miksera
 * - Korekty dla różnych typów mąk i ich siły W
 * - Zaawansowane obliczenia fermentacji z modelami kinetycznymi
 * - Wpływ twardości wody na fermentację
 * 
 * @author PizzaMaestro Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedDoughCalculationService {
    
    // ========================================
    // STAŁE PROFESJONALNE
    // ========================================
    
    // Docelowa temperatura ciasta dla różnych stylów (°C)
    private static final Map<PizzaStyle, Double> TARGET_DOUGH_TEMPS = Map.of(
            PizzaStyle.NEAPOLITAN, 24.0,
            PizzaStyle.ROMAN, 23.0,
            PizzaStyle.NEW_YORK, 24.0,
            PizzaStyle.DETROIT, 25.0,
            PizzaStyle.SICILIAN, 24.0,
            PizzaStyle.FOCACCIA, 25.0,
            PizzaStyle.GRANDMA, 24.0,
            PizzaStyle.PAN, 26.0
    );
    
    // Współczynniki tarcia dla różnych mikserów (°C wzrostu na minutę wyrabiania)
    private static final Map<MixerType, Double> MIXER_FRICTION_FACTORS = Map.of(
            MixerType.HAND_KNEADING, 0.3,        // Ręczne wyrabianie - niskie tarcie
            MixerType.STAND_MIXER_HOME, 0.5,    // Mikser planetarny domowy
            MixerType.STAND_MIXER_PRO, 0.7,     // Mikser planetarny profesjonalny
            MixerType.SPIRAL_MIXER, 0.9,        // Mikser spiralny - wysokie tarcie
            MixerType.FORK_MIXER, 0.4           // Mikser widełkowy
    );
    
    // Czasy wyrabiania dla różnych mikserów i hydratacji (minuty)
    private static final int BASE_MIXING_TIME = 8;
    
    // Optymalne zakresy fermentacji dla stylów (godziny)
    private static final Map<PizzaStyle, int[]> FERMENTATION_RANGES = Map.of(
            PizzaStyle.NEAPOLITAN, new int[]{8, 72},
            PizzaStyle.ROMAN, new int[]{24, 96},
            PizzaStyle.NEW_YORK, new int[]{24, 72},
            PizzaStyle.DETROIT, new int[]{4, 48},
            PizzaStyle.SICILIAN, new int[]{4, 24},
            PizzaStyle.FOCACCIA, new int[]{2, 24},
            PizzaStyle.GRANDMA, new int[]{4, 24},
            PizzaStyle.PAN, new int[]{2, 12}
    );
    
    // ========================================
    // ALGORYTM DDT (DESIRED DOUGH TEMPERATURE)
    // ========================================
    
    /**
     * Oblicza temperaturę wody potrzebną do osiągnięcia docelowej temperatury ciasta.
     * 
     * Wzór DDT (metoda 3-czynnikowa):
     * Water Temp = (DDT × 3) - Room Temp - Flour Temp - Friction Factor
     * 
     * Wzór DDT (metoda 4-czynnikowa z prefermentem):
     * Water Temp = (DDT × 4) - Room Temp - Flour Temp - Preferment Temp - Friction Factor
     * 
     * @param request parametry kalkulacji
     * @return szczegółowe wyniki obliczeń DDT
     */
    public AdvancedCalculationResponse.DDTCalculation calculateDDT(CalculationRequest request) {
        PizzaStyle style = request.getPizzaStyle();
        double targetDoughTemp = TARGET_DOUGH_TEMPS.getOrDefault(style, 24.0);
        
        // Pobierz temperatury
        double roomTemp = request.getRoomTemperature() != null ? request.getRoomTemperature() : 22.0;
        double flourTemp = request.getFlourTemperature() != null ? request.getFlourTemperature() : roomTemp;
        
        // Oblicz współczynnik tarcia
        MixerType mixerType = request.getMixerType() != null ? request.getMixerType() : MixerType.HAND_KNEADING;
        double frictionFactor = calculateFrictionHeat(mixerType, request.getHydration());
        
        double waterTemp;
        String formula;
        int multiplier;
        
        if (request.isUsePreferment() && request.getPrefermentType() != null) {
            // Metoda 4-czynnikowa z prefermentem
            double prefermentTemp = request.getPrefermentTemperature() != null ? 
                    request.getPrefermentTemperature() : roomTemp;
            multiplier = 4;
            waterTemp = (targetDoughTemp * multiplier) - roomTemp - flourTemp - prefermentTemp - frictionFactor;
            formula = String.format("(%d°C × 4) - %.1f°C - %.1f°C - %.1f°C - %.1f°C = %.1f°C",
                    (int) targetDoughTemp, roomTemp, flourTemp, prefermentTemp, frictionFactor, waterTemp);
        } else {
            // Metoda 3-czynnikowa standardowa
            multiplier = 3;
            waterTemp = (targetDoughTemp * multiplier) - roomTemp - flourTemp - frictionFactor;
            formula = String.format("(%d°C × 3) - %.1f°C - %.1f°C - %.1f°C = %.1f°C",
                    (int) targetDoughTemp, roomTemp, flourTemp, frictionFactor, waterTemp);
        }
        
        // Walidacja i korekty
        List<String> warnings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        if (waterTemp < 2) {
            warnings.add("Obliczona temperatura wody jest zbyt niska (< 2°C). " +
                    "Użyj wody z lodem lub schłodź mąkę w lodówce.");
            waterTemp = Math.max(2, waterTemp);
        }
        
        if (waterTemp > 40) {
            warnings.add("Obliczona temperatura wody jest zbyt wysoka (> 40°C). " +
                    "Może to zabić drożdże! Rozważ schłodzenie pomieszczenia.");
            waterTemp = Math.min(40, waterTemp);
        }
        
        if (waterTemp < 10) {
            recommendations.add("Zimna woda - dodaj drożdże bezpośrednio do mąki, nie do wody.");
        }
        
        if (roomTemp > 28) {
            recommendations.add("Wysoka temperatura otoczenia - rozważ krótszą fermentację lub więcej czasu w lodówce.");
        }
        
        return AdvancedCalculationResponse.DDTCalculation.builder()
                .targetDoughTemperature(targetDoughTemp)
                .roomTemperature(roomTemp)
                .flourTemperature(flourTemp)
                .frictionFactor(frictionFactor)
                .calculatedWaterTemperature(round(waterTemp, 1))
                .formula(formula)
                .mixerType(mixerType)
                .mixerTypeName(mixerType.getDisplayName())
                .warnings(warnings)
                .recommendations(recommendations)
                .build();
    }
    
    /**
     * Oblicza ciepło generowane przez tarcie podczas wyrabiania.
     */
    private double calculateFrictionHeat(MixerType mixerType, double hydration) {
        double baseFriction = MIXER_FRICTION_FACTORS.getOrDefault(mixerType, 0.5);
        
        // Niższa hydratacja = więcej tarcia (cięższe ciasto)
        double hydrationFactor = 1.0 + (65.0 - hydration) * 0.01;
        if (hydrationFactor < 0.8) hydrationFactor = 0.8;
        if (hydrationFactor > 1.3) hydrationFactor = 1.3;
        
        // Szacowany czas wyrabiania w minutach
        int mixingTime = calculateMixingTime(mixerType, hydration);
        
        return baseFriction * mixingTime * hydrationFactor;
    }
    
    /**
     * Oblicza szacowany czas wyrabiania.
     */
    private int calculateMixingTime(MixerType mixerType, double hydration) {
        int baseTime = BASE_MIXING_TIME;
        
        // Mikser spiralny jest szybszy
        if (mixerType == MixerType.SPIRAL_MIXER) {
            baseTime = (int) (baseTime * 0.7);
        }
        // Ręczne wyrabianie dłuższe
        else if (mixerType == MixerType.HAND_KNEADING) {
            baseTime = (int) (baseTime * 1.5);
        }
        
        // Wysoka hydratacja wymaga ostrożniejszego mieszania
        if (hydration > 70) {
            baseTime = (int) (baseTime * 0.8); // Mniej intensywne
        }
        
        return baseTime;
    }
    
    // ========================================
    // ZAAWANSOWANE OBLICZENIA DROŻDŻY
    // ========================================
    
    /**
     * Zaawansowany algorytm obliczania ilości drożdży.
     * 
     * Uwzględnia:
     * - Model kinetyczny fermentacji (równanie Arrheniusa)
     * - Typ drożdży i ich aktywność
     * - Temperaturę fermentacji (pokojowa + lodówka)
     * - Siłę mąki (W) - mocniejsza mąka = więcej drożdży
     * - Zawartość soli (hamuje fermentację)
     * - Twardość wody (wpływa na drożdże)
     * 
     * @param flourGrams ilość mąki w gramach
     * @param request parametry kalkulacji
     * @return obliczona ilość drożdży świeżych w gramach
     */
    public AdvancedCalculationResponse.YeastCalculation calculateAdvancedYeast(
            double flourGrams, CalculationRequest request) {
        
        // Parametry bazowe
        int totalHours = request.getTotalFermentationHours();
        double roomTemp = request.getRoomTemperature() != null ? request.getRoomTemperature() : 22.0;
        double fridgeTemp = request.getFridgeTemperature() != null ? request.getFridgeTemperature() : 4.0;
        Recipe.FermentationMethod method = request.getFermentationMethod();
        
        // Podział czasu fermentacji
        double roomHours, fridgeHours;
        switch (method) {
            case COLD_FERMENTATION -> {
                roomHours = Math.min(2, totalHours * 0.1);
                fridgeHours = totalHours - roomHours;
            }
            case MIXED -> {
                roomHours = totalHours * 0.3;
                fridgeHours = totalHours * 0.7;
            }
            case ROOM_TEMPERATURE -> {
                roomHours = totalHours;
                fridgeHours = 0;
            }
            default -> { // SAME_DAY
                roomHours = totalHours;
                fridgeHours = 0;
            }
        }
        
        // Oblicz efektywny czas fermentacji (w "godzinach równoważnych w 20°C")
        double effectiveHours = calculateEffectiveFermentationTime(roomHours, roomTemp, fridgeHours, fridgeTemp);
        
        // Bazowy procent drożdży dla 8h fermentacji w 20°C
        double baseYeastPercentage = 0.2; // 0.2% świeżych drożdży
        
        // Współczynnik czasowy (model logarytmiczny)
        double timeFactor = Math.log(8) / Math.log(effectiveHours);
        if (effectiveHours < 4) timeFactor = Math.min(timeFactor, 2.0);
        if (effectiveHours > 96) timeFactor = Math.max(timeFactor, 0.15);
        
        double yeastPercentage = baseYeastPercentage * timeFactor;
        
        // Korekta dla siły mąki (W)
        Double flourStrength = request.getFlourStrength();
        if (flourStrength != null) {
            if (flourStrength > 300) {
                // Mocna mąka wymaga więcej drożdży
                yeastPercentage *= (1 + (flourStrength - 300) * 0.001);
            } else if (flourStrength < 220) {
                // Słaba mąka - mniej drożdży
                yeastPercentage *= (1 - (220 - flourStrength) * 0.001);
            }
        }
        
        // Korekta dla soli (sól hamuje fermentację)
        double saltPercentage = request.getSaltPercentage();
        if (saltPercentage > 3.0) {
            yeastPercentage *= (1 + (saltPercentage - 3.0) * 0.05);
        }
        
        // Korekta dla cukru (cukier przyspiesza fermentację)
        if (request.getSugarPercentage() > 0) {
            yeastPercentage *= (1 - request.getSugarPercentage() * 0.02);
        }
        
        // Przelicz na gramy
        double freshYeastGrams = flourGrams * (yeastPercentage / 100);
        
        // Konwersja na wybrany typ drożdży
        Recipe.YeastType yeastType = request.getYeastType();
        double convertedYeastGrams = convertYeast(freshYeastGrams, Recipe.YeastType.FRESH, yeastType);
        
        // Przygotuj szczegóły obliczeń
        Map<String, Object> calculationDetails = new LinkedHashMap<>();
        calculationDetails.put("effectiveFermentationHours", round(effectiveHours, 1));
        calculationDetails.put("roomTempHours", round(roomHours, 1));
        calculationDetails.put("fridgeHours", round(fridgeHours, 1));
        calculationDetails.put("timeFactor", round(timeFactor, 3));
        calculationDetails.put("basePercentage", baseYeastPercentage);
        calculationDetails.put("adjustedPercentage", round(yeastPercentage, 4));
        
        List<String> adjustments = new ArrayList<>();
        if (flourStrength != null && flourStrength > 300) {
            adjustments.add(String.format("Mocna mąka (W=%d) - +%.1f%% drożdży", 
                    flourStrength.intValue(), (flourStrength - 300) * 0.1));
        }
        if (saltPercentage > 3.0) {
            adjustments.add(String.format("Wysoka sól (%.1f%%) - +%.1f%% drożdży", 
                    saltPercentage, (saltPercentage - 3.0) * 5));
        }
        
        return AdvancedCalculationResponse.YeastCalculation.builder()
                .yeastType(yeastType)
                .yeastTypeName(yeastType.getDisplayName())
                .freshYeastGrams(round(freshYeastGrams, 2))
                .convertedYeastGrams(round(convertedYeastGrams, 2))
                .yeastPercentage(round(yeastPercentage, 4))
                .effectiveFermentationHours(round(effectiveHours, 1))
                .calculationDetails(calculationDetails)
                .adjustments(adjustments)
                .build();
    }
    
    /**
     * Oblicza efektywny czas fermentacji w "godzinach równoważnych w 20°C".
     * Wykorzystuje uproszczone równanie Arrheniusa dla aktywności drożdży.
     * 
     * Zasada: Podwojenie aktywności drożdży na każde ~8°C wzrostu temperatury
     * (do pewnej granicy)
     */
    private double calculateEffectiveFermentationTime(
            double roomHours, double roomTemp, 
            double fridgeHours, double fridgeTemp) {
        
        // Q10 dla drożdży ≈ 2-3, używamy 2.5
        double q10 = 2.5;
        double referenceTemp = 20.0;
        
        // Współczynnik dla temperatury pokojowej
        double roomFactor = Math.pow(q10, (roomTemp - referenceTemp) / 10);
        
        // Współczynnik dla lodówki (4°C jest ~16°C poniżej referencji)
        double fridgeFactor = Math.pow(q10, (fridgeTemp - referenceTemp) / 10);
        
        // Efektywne godziny = suma (rzeczywiste godziny × współczynnik)
        double effectiveHours = (roomHours * roomFactor) + (fridgeHours * fridgeFactor);
        
        // Minimum 1 godzina efektywna
        return Math.max(1, effectiveHours);
    }
    
    /**
     * Konwertuje ilość drożdży między typami.
     */
    private double convertYeast(double grams, Recipe.YeastType fromType, Recipe.YeastType toType) {
        if (fromType == toType) return grams;
        
        // Najpierw do świeżych
        double freshGrams = grams / fromType.getConversionFactor();
        
        // Potem do docelowego typu
        return freshGrams * toType.getConversionFactor();
    }
    
    // ========================================
    // ANALIZA MĄKI I REKOMENDACJE
    // ========================================
    
    /**
     * Analizuje parametry mąki i generuje rekomendacje.
     */
    public AdvancedCalculationResponse.FlourAnalysis analyzeFlour(CalculationRequest request) {
        List<String> recommendations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        Double strength = request.getFlourStrength();
        Double protein = request.getFlourProtein();
        double hydration = request.getHydration();
        PizzaStyle style = request.getPizzaStyle();
        int fermentationHours = request.getTotalFermentationHours();
        
        // Analiza siły W
        if (strength != null) {
            if (strength < 200) {
                warnings.add("Mąka o niskiej sile (W<200) może nie wytrzymać długiej fermentacji.");
                if (fermentationHours > 12) {
                    recommendations.add("Rozważ skrócenie fermentacji do max 12h lub użycie mocniejszej mąki.");
                }
            } else if (strength > 350) {
                recommendations.add("Mocna mąka (W>350) - idealna do długiej fermentacji 48-72h.");
                if (fermentationHours < 24) {
                    recommendations.add("Możesz wydłużyć fermentację dla głębszego smaku.");
                }
            }
            
            // Sugestie hydratacji na podstawie W
            double suggestedMinHydration = 55 + (strength - 200) * 0.05;
            double suggestedMaxHydration = 65 + (strength - 200) * 0.08;
            
            if (hydration < suggestedMinHydration - 5) {
                recommendations.add(String.format(
                        "Dla mąki W=%d możesz zwiększyć hydratację do %.0f-%.0f%%.", 
                        strength.intValue(), suggestedMinHydration, suggestedMaxHydration));
            } else if (hydration > suggestedMaxHydration + 5) {
                warnings.add(String.format(
                        "Hydratacja %.0f%% może być za wysoka dla mąki W=%d. Ciasto może być zbyt lepkie.",
                        hydration, strength.intValue()));
            }
        }
        
        // Analiza białka
        if (protein != null) {
            if (protein < 11 && hydration > 65) {
                warnings.add(String.format(
                        "Mąka o niskiej zawartości białka (%.1f%%) może mieć problem z hydratacją %.0f%%.",
                        protein, hydration));
            }
            if (protein > 14 && style == PizzaStyle.NEAPOLITAN) {
                recommendations.add("Wysokobiałkowa mąka - rozważ dłuższą autolizę (30-60 min).");
            }
        }
        
        // Rekomendacje dla stylu
        switch (style) {
            case NEAPOLITAN -> {
                if (hydration < 58 || hydration > 70) {
                    recommendations.add("Pizza neapolitańska najlepsza przy hydratacji 60-65%.");
                }
                if (strength != null && (strength < 250 || strength > 320)) {
                    recommendations.add("Dla neapolitańskiej zalecana mąka W=260-300 (np. Caputo Pizzeria).");
                }
            }
            case ROMAN -> {
                if (hydration < 70) {
                    recommendations.add("Pizza rzymska wymaga wysokiej hydratacji (70-80%) dla chrupkości.");
                }
            }
            case NEW_YORK -> {
                if (protein != null && protein < 12) {
                    recommendations.add("Dla NY style użyj mąki high-gluten (12.5%+ białka).");
                }
            }
        }
        
        return AdvancedCalculationResponse.FlourAnalysis.builder()
                .flourStrength(strength)
                .proteinContent(protein)
                .hydration(hydration)
                .pizzaStyle(style)
                .recommendations(recommendations)
                .warnings(warnings)
                .build();
    }
    
    // ========================================
    // WPŁYW WODY NA FERMENTACJĘ
    // ========================================
    
    /**
     * Analizuje wpływ parametrów wody na ciasto.
     */
    public AdvancedCalculationResponse.WaterAnalysis analyzeWater(CalculationRequest request) {
        List<String> effects = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        Double hardness = request.getWaterHardness();
        Double ph = request.getWaterPh();
        
        double fermentationModifier = 1.0;
        double glutenModifier = 1.0;
        
        if (hardness != null) {
            if (hardness < 50) {
                // Bardzo miękka woda
                effects.add("Bardzo miękka woda - szybsza fermentacja, słabszy gluten.");
                fermentationModifier *= 1.1;
                glutenModifier *= 0.95;
                recommendations.add("Rozważ dodanie szczypty soli mineralnej lub użycie twardszej wody.");
            } else if (hardness > 200) {
                // Twarda woda
                effects.add("Twarda woda - wolniejsza fermentacja, mocniejszy gluten.");
                fermentationModifier *= 0.9;
                glutenModifier *= 1.05;
                if (hardness > 300) {
                    recommendations.add("Bardzo twarda woda może hamować drożdże. Rozważ filtrację.");
                }
            } else {
                effects.add("Umiarkowana twardość wody - optymalna dla pizzy.");
            }
        }
        
        if (ph != null) {
            if (ph < 6.5) {
                effects.add("Kwaśna woda (pH<6.5) - może przyspieszyć fermentację.");
                fermentationModifier *= 1.05;
            } else if (ph > 8.0) {
                effects.add("Zasadowa woda (pH>8.0) - może spowalniać fermentację.");
                fermentationModifier *= 0.95;
                recommendations.add("Rozważ użycie wody o niższym pH lub dodanie odrobiny soku z cytryny.");
            }
        }
        
        return AdvancedCalculationResponse.WaterAnalysis.builder()
                .hardness(hardness)
                .ph(ph)
                .fermentationModifier(round(fermentationModifier, 2))
                .glutenModifier(round(glutenModifier, 2))
                .effects(effects)
                .recommendations(recommendations)
                .build();
    }
    
    // ========================================
    // KALKULACJE HARMONOGRAMU
    // ========================================
    
    /**
     * Generuje optymalny harmonogram na podstawie wszystkich czynników.
     */
    public List<AdvancedCalculationResponse.DetailedScheduleStep> generateDetailedSchedule(
            CalculationRequest request, LocalDateTime plannedBakeTime) {
        
        List<AdvancedCalculationResponse.DetailedScheduleStep> steps = new ArrayList<>();
        
        LocalDateTime currentTime = plannedBakeTime;
        PizzaStyle style = request.getPizzaStyle();
        Recipe.FermentationMethod method = request.getFermentationMethod();
        double hydration = request.getHydration();
        
        // 1. PIECZENIE
        int bakingSeconds = style.getBakingTimeSeconds();
        steps.add(0, createDetailedStep(
                "PIECZENIE",
                "Piecz pizzę w rozgrzanym piecu",
                currentTime,
                bakingSeconds / 60,
                (double) style.getOvenTemperature(),
                "fire",
                List.of(
                        "Piec musi być w pełni rozgrzany",
                        "Obracaj pizzę w połowie pieczenia",
                        "Obserwuj brzegi - powinny być złociste"
                ),
                "critical"
        ));
        
        // 2. ROZCIĄGANIE
        currentTime = currentTime.minusMinutes(20);
        List<String> stretchingTips = new ArrayList<>();
        stretchingTips.add("Nie używaj wałka - zniszczy bąbelki w cieście");
        stretchingTips.add("Rozciągaj od środka na zewnątrz, zostawiając brzeg");
        if (hydration > 70) {
            stretchingTips.add("Wysoka hydratacja - posyp blat mąką lub semoliną");
        }
        
        steps.add(0, createDetailedStep(
                "ROZCIĄGANIE",
                "Rozciągnij kulkę ciasta na placek",
                currentTime,
                15,
                null,
                "expand-arrows-alt",
                stretchingTips,
                "important"
        ));
        
        // 3. TEMPEROWANIE (wyjęcie z lodówki)
        if (method == Recipe.FermentationMethod.COLD_FERMENTATION || 
            method == Recipe.FermentationMethod.MIXED) {
            
            int temperingMinutes = hydration > 70 ? 150 : 120; // Więcej dla wysokiej hydratacji
            currentTime = currentTime.minusMinutes(temperingMinutes);
            
            steps.add(0, createDetailedStep(
                    "TEMPEROWANIE",
                    String.format("Wyjmij kulki z lodówki i pozostaw w temp. pokojowej przez %d min", temperingMinutes),
                    currentTime,
                    temperingMinutes,
                    request.getRoomTemperature(),
                    "temperature-high",
                    List.of(
                            "Nie rozpakowuj kulek - niech ocieplą się pod przykryciem",
                            "Zimne ciasto będzie trudne do rozciągnięcia",
                            "Ciasto powinno być miękkie i elastyczne przed formowaniem"
                    ),
                    "important"
            ));
            
            // 4. FERMENTACJA W LODÓWCE
            int coldHours = calculateColdHours(request);
            currentTime = currentTime.minusHours(coldHours);
            
            steps.add(0, createDetailedStep(
                    "FERMENTACJA CHŁODNICZA",
                    String.format("Fermentacja w lodówce przez %d godzin", coldHours),
                    currentTime,
                    coldHours * 60,
                    request.getFridgeTemperature(),
                    "snowflake",
                    List.of(
                            "Przykryj kulki szczelnie folią lub w zamkniętym pojemniku",
                            "Temperatura lodówki powinna być 3-5°C",
                            "Długa fermentacja rozwija smak i poprawia strawność"
                    ),
                    "normal"
            ));
        }
        
        // 5. FORMOWANIE KULEK
        currentTime = currentTime.minusMinutes(20);
        steps.add(0, createDetailedStep(
                "FORMOWANIE KULEK",
                String.format("Podziel ciasto na %d kulek po %dg", 
                        request.getNumberOfPizzas(), request.getBallWeight()),
                currentTime,
                20,
                null,
                "circle",
                List.of(
                        "Używaj wagi kuchennej dla precyzji",
                        "Formuj kulki energicznym ruchem dłoni",
                        "Powierzchnia powinna być gładka, napięta"
                ),
                "important"
        ));
        
        // 6. FERMENTACJA ZBIORCZA
        int bulkHours = calculateBulkHours(request);
        if (bulkHours > 0) {
            currentTime = currentTime.minusHours(bulkHours);
            
            List<String> bulkTips = new ArrayList<>();
            bulkTips.add("Przykryj ciasto wilgotną ściereczką lub folią");
            bulkTips.add("Ciasto powinno zwiększyć objętość o 50-100%");
            
            // Dodaj składania dla wysokiej hydratacji
            if (hydration >= 68) {
                int folds = Math.min(4, bulkHours);
                bulkTips.add(String.format("Wykonaj %d składań (coil fold) co %d min", 
                        folds, bulkHours * 60 / (folds + 1)));
            }
            
            steps.add(0, createDetailedStep(
                    "FERMENTACJA ZBIORCZA",
                    String.format("Fermentacja w temp. pokojowej przez %d godzin", bulkHours),
                    currentTime,
                    bulkHours * 60,
                    request.getRoomTemperature(),
                    "clock",
                    bulkTips,
                    "normal"
            ));
        }
        
        // 7. WYRABIANIE
        MixerType mixerType = request.getMixerType() != null ? request.getMixerType() : MixerType.HAND_KNEADING;
        int mixingTime = calculateMixingTime(mixerType, hydration);
        currentTime = currentTime.minusMinutes(mixingTime + 5);
        
        List<String> kneadingTips = getKneadingTips(mixerType, hydration);
        
        steps.add(0, createDetailedStep(
                "WYRABIANIE",
                String.format("Wyrabiaj ciasto przez %d minut (%s)", mixingTime, mixerType.getDisplayName()),
                currentTime,
                mixingTime,
                null,
                "hand-paper",
                kneadingTips,
                "important"
        ));
        
        // 8. MIESZANIE (autoliza opcjonalnie)
        currentTime = currentTime.minusMinutes(10);
        
        List<String> mixingTips = new ArrayList<>();
        mixingTips.add("Rozpuść drożdże w letniej wodzie (jeśli świeże/aktywne)");
        mixingTips.add("Dodaj mąkę do wody (nie odwrotnie!)");
        mixingTips.add("Mieszaj do połączenia składników");
        mixingTips.add("Sól dodaj na końcu mieszania lub po autolizie");
        
        if (request.getFlourStrength() != null && request.getFlourStrength() > 300) {
            // Autoliza dla mocnej mąki
            mixingTips.add("AUTOLIZA: Po wymieszaniu mąki z wodą, odczekaj 30 min przed dodaniem soli i drożdży");
        }
        
        steps.add(0, createDetailedStep(
                "MIESZANIE",
                "Wymieszaj składniki na jednolitą masę",
                currentTime,
                10,
                null,
                "utensils",
                mixingTips,
                "normal"
        ));
        
        // 9. PREFERMENT (jeśli używany)
        if (request.isUsePreferment() && request.getPrefermentType() != null) {
            int prefermentHours = request.getPrefermentFermentationHours() != null ?
                    request.getPrefermentFermentationHours() : 12;
            currentTime = currentTime.minusHours(prefermentHours);
            
            steps.add(0, createDetailedStep(
                    "PREFERMENT (" + request.getPrefermentType().getDisplayName().toUpperCase() + ")",
                    String.format("Przygotuj %s i fermentuj przez %d godzin", 
                            request.getPrefermentType().getDisplayName(), prefermentHours),
                    currentTime,
                    prefermentHours * 60,
                    request.getRoomTemperature(),
                    "flask",
                    getPrefermentTips(request.getPrefermentType()),
                    "important"
            ));
        }
        
        // Ponumeruj kroki
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setStepNumber(i + 1);
        }
        
        return steps;
    }
    
    private AdvancedCalculationResponse.DetailedScheduleStep createDetailedStep(
            String title, String description, LocalDateTime time, int durationMinutes,
            Double temperature, String icon, List<String> tips, String importance) {
        
        return AdvancedCalculationResponse.DetailedScheduleStep.builder()
                .title(title)
                .description(description)
                .scheduledTime(time)
                .durationMinutes(durationMinutes)
                .temperature(temperature)
                .icon(icon)
                .tips(tips)
                .importance(importance)
                .build();
    }
    
    private int calculateColdHours(CalculationRequest request) {
        int total = request.getTotalFermentationHours();
        return switch (request.getFermentationMethod()) {
            case COLD_FERMENTATION -> Math.max(6, total - 4);
            case MIXED -> (int) (total * 0.7);
            default -> 0;
        };
    }
    
    private int calculateBulkHours(CalculationRequest request) {
        int total = request.getTotalFermentationHours();
        return switch (request.getFermentationMethod()) {
            case ROOM_TEMPERATURE -> Math.max(2, total - 2);
            case COLD_FERMENTATION -> 2;
            case MIXED -> (int) (total * 0.3);
            case SAME_DAY -> Math.max(1, total - 1);
        };
    }
    
    private List<String> getKneadingTips(MixerType mixerType, double hydration) {
        List<String> tips = new ArrayList<>();
        
        switch (mixerType) {
            case HAND_KNEADING -> {
                tips.add("Technika: składaj ciasto na siebie i dociskaj dłonią");
                tips.add("Ciasto gotowe gdy jest gładkie i odchodzi od blatu");
                if (hydration > 70) {
                    tips.add("Używaj mokrych rąk zamiast posypywać mąką");
                    tips.add("Technika slap & fold działa lepiej dla mokrego ciasta");
                }
            }
            case STAND_MIXER_HOME -> {
                tips.add("Zacznij od niskiej prędkości (2) przez 3 min");
                tips.add("Zwiększ do średniej (4-5) na kolejne 5-7 min");
                if (hydration > 72) {
                    tips.add("UWAGA: Domowy mikser może nie poradzić sobie z hydratacją >72%");
                }
            }
            case SPIRAL_MIXER -> {
                tips.add("Spiral: 3 min na wolnych + 5-6 min na szybkich");
                tips.add("Monitoruj temperaturę ciasta - nie przekraczaj 26°C");
            }
        }
        
        tips.add("Test: ciasto powinno przechodzić test 'window pane' (rozciągać się bez rwania)");
        
        return tips;
    }
    
    private List<String> getPrefermentTips(Recipe.PrefermentType type) {
        return switch (type) {
            case POOLISH -> List.of(
                    "Poolish: 100% hydratacji (równe ilości mąki i wody)",
                    "Dodaj minimalną ilość drożdży (0.1% świeżych)",
                    "Gotowy gdy pełen bąbelków i zaczyna opadać",
                    "Nie przedłużaj - zbyt dojrzały poolish da kwaśny smak"
            );
            case BIGA -> List.of(
                    "Biga: niska hydratacja (50-60%)",
                    "Ciasto powinno być sztywne, nie lepkie",
                    "Fermentuj w chłodniejszym miejscu (18-20°C)",
                    "Gotowa gdy podwoiła objętość"
            );
            case LIEVITO_MADRE -> List.of(
                    "Użyj aktywnego, odświeżonego zakwasu",
                    "Odśwież zakwas 8-12h przed użyciem",
                    "Proporcje odświeżenia: 1:1:0.5 (zakwas:mąka:woda)",
                    "Zakwas gotowy gdy potroiła objętość i ma kopulasty wierzch"
            );
        };
    }
    
    private double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }
}
