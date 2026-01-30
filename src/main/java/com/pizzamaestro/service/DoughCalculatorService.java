package com.pizzamaestro.service;

import com.pizzamaestro.dto.request.CalculationRequest;
import com.pizzamaestro.dto.response.CalculationResponse;
import com.pizzamaestro.model.*;
import com.pizzamaestro.service.strategy.FermentationStrategy;
import com.pizzamaestro.service.strategy.FermentationStrategyFactory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Główny serwis kalkulacji receptury ciasta na pizzę.
 * 
 * Implementuje zaawansowane algorytmy obliczania:
 * - Ilości składników na podstawie procentów piekarskich
 * - Ilości drożdży na podstawie czasu i temperatury fermentacji
 * - Harmonogramu przygotowania ciasta
 * 
 * @author PizzaMaestro Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoughCalculatorService {
    
    private final FermentationStrategyFactory fermentationStrategyFactory;
    private final IngredientService ingredientService;
    private final EnvironmentalCorrectionService environmentalCorrectionService;
    
    /**
     * Wykonuje kalkulację receptury ciasta na pizzę.
     *
     * @param request parametry kalkulacji
     * @return obliczona receptura z harmonogramem
     * @throws IllegalArgumentException gdy request jest null lub zawiera nieprawidłowe dane
     */
    public CalculationResponse calculate(CalculationRequest request) {
        // Walidacja wejścia
        if (request == null) {
            log.error("❌ calculate: request jest null");
            throw new IllegalArgumentException("Request nie może być null");
        }
        if (request.getPizzaStyle() == null) {
            log.error("❌ calculate: pizzaStyle jest null");
            throw new IllegalArgumentException("Styl pizzy jest wymagany");
        }
        if (request.getNumberOfPizzas() <= 0) {
            log.error("❌ calculate: nieprawidłowa liczba pizz: {}", request.getNumberOfPizzas());
            throw new IllegalArgumentException("Liczba pizz musi być większa od 0");
        }
        
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║ 🍕 KALKULACJA CIASTA NA PIZZĘ                            ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📊 Parametry: {} pizz × {}g, styl: {}, hydratacja: {}%", 
                request.getNumberOfPizzas(), request.getBallWeight(),
                request.getPizzaStyle(), request.getHydration());
        
        // 1. Oblicz całkowitą wagę ciasta
        double totalDoughWeight = request.getNumberOfPizzas() * request.getBallWeight();
        
        // 2. Oblicz ilość mąki (100% bazy)
        double flourGrams = calculateFlourAmount(totalDoughWeight, request);
        
        // 3. Oblicz pozostałe składniki na podstawie procentów piekarskich
        double waterGrams = flourGrams * (request.getHydration() / 100.0);
        double saltGrams = flourGrams * (request.getSaltPercentage() / 100.0);
        double oilGrams = flourGrams * (request.getOilPercentage() / 100.0);
        double sugarGrams = flourGrams * (request.getSugarPercentage() / 100.0);
        
        // 4. Oblicz ilość drożdży
        double yeastGrams = calculateYeastAmount(flourGrams, request);
        
        // 5. Buduj odpowiedź
        CalculationResponse.CalculationResponseBuilder responseBuilder = CalculationResponse.builder()
                .pizzaStyle(request.getPizzaStyle())
                .pizzaStyleName(request.getPizzaStyle().getDisplayName())
                .numberOfPizzas(request.getNumberOfPizzas())
                .ballWeight(request.getBallWeight());
        
        // 6. Składniki
        List<CalculationResponse.AdditionalIngredientResult> additionalResults = new ArrayList<>();
        if (request.getAdditionalIngredients() != null) {
            for (CalculationRequest.AdditionalIngredientRequest additional : request.getAdditionalIngredients()) {
                double grams = flourGrams * (additional.getPercentage() / 100.0);
                additionalResults.add(CalculationResponse.AdditionalIngredientResult.builder()
                        .name(additional.getName())
                        .grams(round(grams))
                        .percentage(additional.getPercentage())
                        .build());
            }
        }
        
        responseBuilder.ingredients(CalculationResponse.IngredientsResult.builder()
                .totalDoughWeight(round(totalDoughWeight))
                .flourGrams(round(flourGrams))
                .waterGrams(round(waterGrams))
                .saltGrams(round(saltGrams))
                .yeastGrams(round(yeastGrams))
                .yeastType(request.getYeastType().getDisplayName())
                .oilGrams(round(oilGrams))
                .sugarGrams(round(sugarGrams))
                .additionalIngredients(additionalResults)
                .build());
        
        // 7. Procenty piekarskie
        responseBuilder.bakerPercentages(CalculationResponse.BakerPercentagesResult.builder()
                .flour(100.0)
                .water(request.getHydration())
                .salt(request.getSaltPercentage())
                .yeast(round((yeastGrams / flourGrams) * 100, 3))
                .oil(request.getOilPercentage())
                .sugar(request.getSugarPercentage())
                .build());
        
        // 8. Preferment (jeśli używany)
        if (request.isUsePreferment() && request.getPrefermentType() != null) {
            CalculationResponse.PrefermentResult preferment = calculatePreferment(
                    flourGrams, request.getPrefermentType(), 
                    request.getPrefermentPercentage(), request.getPrefermentFermentationHours());
            responseBuilder.preferment(preferment);
            
            // Ciasto główne po odjęciu prefermentu
            responseBuilder.mainDough(CalculationResponse.MainDoughResult.builder()
                    .flourGrams(round(flourGrams - preferment.getFlourGrams()))
                    .waterGrams(round(waterGrams - preferment.getWaterGrams()))
                    .saltGrams(round(saltGrams))
                    .yeastGrams(round(yeastGrams - preferment.getYeastGrams()))
                    .oilGrams(round(oilGrams))
                    .sugarGrams(round(sugarGrams))
                    .build());
        }
        
        // 9. Harmonogram fermentacji
        if (request.isGenerateSchedule() && request.getPlannedBakeTime() != null) {
            List<CalculationResponse.ScheduleStep> schedule = generateSchedule(request);
            responseBuilder.schedule(schedule);
        }
        
        // 10. Wskazówki
        List<String> tips = generateTips(request);
        responseBuilder.tips(tips);
        
        // 11. Informacje o piecu
        OvenType ovenType = request.getOvenType() != null ? 
                request.getOvenType() : request.getPizzaStyle().getRecommendedOven();
        int ovenTemp = request.getOvenTemperature() != null ? 
                request.getOvenTemperature() : request.getPizzaStyle().getOvenTemperature();
        
        responseBuilder.ovenInfo(CalculationResponse.OvenInfo.builder()
                .ovenType(ovenType)
                .ovenName(ovenType.getDisplayName())
                .temperature(ovenTemp)
                .bakingTimeSeconds(request.getPizzaStyle().getBakingTimeSeconds())
                .tips(ovenType.getTips())
                .build());
        
        return responseBuilder.build();
    }
    
    /**
     * Oblicza ilość mąki na podstawie całkowitej wagi ciasta.
     * 
     * Wzór: mąka = całkowita_waga / (1 + hydratacja + sól + oliwa + cukier + drożdże)
     */
    private double calculateFlourAmount(double totalDoughWeight, CalculationRequest request) {
        // Suma wszystkich procentów (mąka = 100%)
        double totalPercentage = 100.0 
                + request.getHydration() 
                + request.getSaltPercentage() 
                + request.getOilPercentage() 
                + request.getSugarPercentage();
        
        // Szacunkowy procent drożdży (początkowo zakładamy ~0.5%)
        totalPercentage += 0.5;
        
        // Dodatkowe składniki
        if (request.getAdditionalIngredients() != null) {
            for (CalculationRequest.AdditionalIngredientRequest additional : request.getAdditionalIngredients()) {
                totalPercentage += additional.getPercentage();
            }
        }
        
        return (totalDoughWeight / totalPercentage) * 100.0;
    }
    
    /**
     * Oblicza ilość drożdży na podstawie:
     * - Typu drożdży
     * - Metody fermentacji
     * - Czasu fermentacji
     * - Temperatury
     * 
     * Wykorzystuje wzorzec Strategy do wyboru odpowiedniego algorytmu.
     */
    private double calculateYeastAmount(double flourGrams, CalculationRequest request) {
        if (flourGrams <= 0) {
            log.error("❌ calculateYeastAmount: nieprawidłowa ilość mąki: {}", flourGrams);
            throw new IllegalArgumentException("Ilość mąki musi być większa od 0");
        }
        
        log.debug("🍞 Obliczanie drożdży dla {}g mąki, fermentacja: {}h, metoda: {}", 
                flourGrams, request.getTotalFermentationHours(), request.getFermentationMethod());
        
        // Jeśli użytkownik podał konkretny procent drożdży
        if (request.getYeastPercentage() != null) {
            log.debug("   Użyto ręcznie podanego procentu drożdży: {}%", request.getYeastPercentage());
            double baseYeast = flourGrams * (request.getYeastPercentage() / 100.0);
            return convertYeastType(baseYeast, Recipe.YeastType.FRESH, request.getYeastType());
        }
        
        // Automatyczne obliczenie na podstawie czasu i temperatury
        FermentationStrategy strategy = fermentationStrategyFactory.getStrategy(request.getFermentationMethod());
        
        if (strategy == null) {
            log.error("❌ Nie znaleziono strategii fermentacji dla metody: {}", request.getFermentationMethod());
            throw new IllegalStateException("Nie znaleziono strategii fermentacji");
        }
        
        double roomTemp = request.getRoomTemperature() != null ? request.getRoomTemperature() : 22.0;
        double fridgeTemp = request.getFridgeTemperature() != null ? request.getFridgeTemperature() : 4.0;
        
        double yeastPercentage = strategy.calculateYeastPercentage(
                request.getTotalFermentationHours(),
                roomTemp,
                fridgeTemp,
                request.getFermentationMethod()
        );
        
        double freshYeastGrams = flourGrams * (yeastPercentage / 100.0);
        
        // Konwersja na wybrany typ drożdży
        return convertYeastType(freshYeastGrams, Recipe.YeastType.FRESH, request.getYeastType());
    }
    
    /**
     * Konwertuje ilość drożdży między typami.
     * Przelicznik bazuje na drożdżach świeżych:
     * - 1g suszonych instant = 3g świeżych
     * - 1g suszonych aktywnych = 2.5g świeżych
     */
    private double convertYeastType(double grams, Recipe.YeastType fromType, Recipe.YeastType toType) {
        if (fromType == toType) return grams;
        
        // Najpierw konwertuj na świeże
        double freshGrams = grams / fromType.getConversionFactor();
        
        // Potem konwertuj na docelowy typ
        return freshGrams * toType.getConversionFactor();
    }
    
    /**
     * Oblicza recepturę prefermentu (poolish, biga, lievito madre).
     */
    private CalculationResponse.PrefermentResult calculatePreferment(
            double totalFlour, Recipe.PrefermentType type, 
            Double percentage, Integer hours) {
        
        double prefermentPercentage = percentage != null ? percentage : 30.0;
        int fermentationHours = hours != null ? hours : 12;
        
        double prefermentFlour = totalFlour * (prefermentPercentage / 100.0);
        double prefermentWater = prefermentFlour * (type.getHydration() / 100.0);
        
        // Drożdże w preferment - zazwyczaj bardzo mało
        double prefermentYeast = switch (type) {
            case POOLISH -> prefermentFlour * 0.001; // 0.1% świeżych
            case BIGA -> prefermentFlour * 0.002;    // 0.2% świeżych  
            case LIEVITO_MADRE -> 0; // zakwas nie potrzebuje drożdży
        };
        
        String instructions = switch (type) {
            case POOLISH -> String.format(
                    "Wymieszaj %dg mąki z %dg wody i %.1fg drożdży świeżych. " +
                    "Przykryj i zostaw w temperaturze pokojowej na %d godzin do podwojenia objętości.",
                    (int) prefermentFlour, (int) prefermentWater, prefermentYeast, fermentationHours);
            case BIGA -> String.format(
                    "Wymieszaj %dg mąki z %dg wody i %.1fg drożdży. " +
                    "Zagnieć krótko na sztywną masę. Fermentuj %d godzin.",
                    (int) prefermentFlour, (int) prefermentWater, prefermentYeast, fermentationHours);
            case LIEVITO_MADRE -> String.format(
                    "Użyj %dg odświeżonego lievito madre z %dg mąki i %dg wody. " +
                    "Odśwież zakwas 8-12 godzin przed użyciem.",
                    (int) prefermentFlour, (int) prefermentFlour, (int) prefermentWater);
        };
        
        return CalculationResponse.PrefermentResult.builder()
                .type(type)
                .typeName(type.getDisplayName())
                .flourGrams(round(prefermentFlour))
                .waterGrams(round(prefermentWater))
                .yeastGrams(round(prefermentYeast))
                .fermentationHours(fermentationHours)
                .instructions(instructions)
                .build();
    }
    
    /**
     * Generuje harmonogram przygotowania ciasta.
     */
    private List<CalculationResponse.ScheduleStep> generateSchedule(CalculationRequest request) {
        List<CalculationResponse.ScheduleStep> steps = new ArrayList<>();
        LocalDateTime bakeTime = request.getPlannedBakeTime();
        int stepNumber = 1;
        
        // Wartości domyślne dla temperatur
        double roomTemp = request.getRoomTemperature() != null ? request.getRoomTemperature() : 22.0;
        double fridgeTemp = request.getFridgeTemperature() != null ? request.getFridgeTemperature() : 4.0;
        
        // Pracujemy wstecz od czasu pieczenia
        LocalDateTime currentTime = bakeTime;
        
        // 1. Pieczenie
        steps.add(0, createStep(stepNumber++, Recipe.StepType.BAKE, 
                "Pieczenie pizzy", 
                "Rozgrzej piec i piecz pizzę",
                currentTime, 
                request.getPizzaStyle().getBakingTimeSeconds() / 60,
                (double) request.getPizzaStyle().getOvenTemperature()));
        
        // 2. Formowanie placka (15 min przed pieczeniem)
        currentTime = currentTime.minusMinutes(15);
        steps.add(0, createStep(stepNumber++, Recipe.StepType.SHAPE,
                "Rozciąganie ciasta",
                "Delikatnie rozciągnij kulkę ciasta na placek, zostawiając grubszy brzeg",
                currentTime, 15, null));
        
        // 3. Końcowy odpoczynek w temp. pokojowej
        int finalRestMinutes = switch (request.getFermentationMethod()) {
            case COLD_FERMENTATION -> 120; // 2h po wyjęciu z lodówki
            case MIXED -> 90;
            default -> 30;
        };
        
        currentTime = currentTime.minusMinutes(finalRestMinutes);
        steps.add(0, createStep(stepNumber++, Recipe.StepType.FINAL_PROOF,
                "Końcowy odpoczynek",
                "Kulki ciasta odpoczywają w temperaturze pokojowej",
                currentTime, finalRestMinutes, roomTemp));
        
        // 4. Wyjęcie z lodówki (jeśli fermentacja chłodnicza)
        if (request.getFermentationMethod() == Recipe.FermentationMethod.COLD_FERMENTATION ||
            request.getFermentationMethod() == Recipe.FermentationMethod.MIXED) {
            
            steps.add(0, createStep(stepNumber++, Recipe.StepType.REMOVE_FROM_FRIDGE,
                    "Wyjęcie z lodówki",
                    "Wyjmij kulki ciasta z lodówki",
                    currentTime, 5, null));
            
            // Fermentacja w lodówce
            int coldHours = calculateColdFermentationHours(request);
            currentTime = currentTime.minusHours(coldHours);
            steps.add(0, createStep(stepNumber++, Recipe.StepType.COLD_PROOF,
                    "Fermentacja w lodówce",
                    String.format("Fermentacja chłodnicza przez %d godzin", coldHours),
                    currentTime, coldHours * 60, fridgeTemp));
        }
        
        // 5. Formowanie kulek
        currentTime = currentTime.minusMinutes(15);
        steps.add(0, createStep(stepNumber++, Recipe.StepType.BALL_FORMING,
                "Formowanie kulek",
                String.format("Podziel ciasto na %d kulek po %dg każda", 
                        request.getNumberOfPizzas(), request.getBallWeight()),
                currentTime, 15, null));
        
        // 6. Fermentacja zbiorcza w temp. pokojowej
        int bulkHours = calculateBulkFermentationHours(request);
        if (bulkHours > 0) {
            currentTime = currentTime.minusHours(bulkHours);
            
            // Dodaj składania jeśli wysoka hydratacja
            if (request.getHydration() >= 70) {
                int folds = Math.min(4, bulkHours);
                int foldInterval = bulkHours * 60 / (folds + 1);
                
                for (int i = folds; i > 0; i--) {
                    LocalDateTime foldTime = currentTime.plusMinutes((long) foldInterval * i);
                    steps.add(findInsertPosition(steps, foldTime), 
                            createStep(stepNumber++, Recipe.StepType.FOLD,
                                    "Składanie ciasta",
                                    "Wykonaj delikatne składanie (coil fold lub letter fold)",
                                    foldTime, 5, null));
                }
            }
            
            steps.add(0, createStep(stepNumber++, Recipe.StepType.BULK_FERMENTATION,
                    "Fermentacja zbiorcza",
                    String.format("Fermentacja w temperaturze %d°C przez %d godzin", 
                            (int) roomTemp, bulkHours),
                    currentTime, bulkHours * 60, roomTemp));
        }
        
        // 7. Wyrabianie
        currentTime = currentTime.minusMinutes(15);
        steps.add(0, createStep(stepNumber++, Recipe.StepType.KNEAD,
                "Wyrabianie ciasta",
                "Wyrabiaj ciasto przez 10-15 minut do uzyskania gładkiej, elastycznej masy",
                currentTime, 15, null));
        
        // 8. Mieszanie składników
        currentTime = currentTime.minusMinutes(10);
        steps.add(0, createStep(stepNumber++, Recipe.StepType.MIX_DOUGH,
                "Mieszanie składników",
                "Rozpuść drożdże w wodzie, dodaj mąkę i wymieszaj. Na koniec dodaj sól.",
                currentTime, 10, null));
        
        // 9. Preferment (jeśli używany)
        if (request.isUsePreferment() && request.getPrefermentType() != null) {
            int prefermentHours = request.getPrefermentFermentationHours() != null ? 
                    request.getPrefermentFermentationHours() : 12;
            currentTime = currentTime.minusHours(prefermentHours);
            steps.add(0, createStep(stepNumber++, Recipe.StepType.MIX_PREFERMENT,
                    "Przygotowanie prefermentu",
                    String.format("Przygotuj %s i zostaw na %d godzin", 
                            request.getPrefermentType().getDisplayName(), prefermentHours),
                    currentTime, prefermentHours * 60, roomTemp));
        }
        
        // Przenumeruj kroki od początku
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setStepNumber(i + 1);
        }
        
        // Dodaj względne czasy
        LocalDateTime now = LocalDateTime.now();
        for (CalculationResponse.ScheduleStep step : steps) {
            step.setRelativeTime(formatRelativeTime(now, step.getScheduledTime()));
        }
        
        return steps;
    }
    
    private CalculationResponse.ScheduleStep createStep(
            int stepNumber, Recipe.StepType type, String title, String description,
            LocalDateTime time, int durationMinutes, Double temperature) {
        
        return CalculationResponse.ScheduleStep.builder()
                .stepNumber(stepNumber)
                .stepType(type)
                .title(title)
                .description(description)
                .scheduledTime(time)
                .durationMinutes(durationMinutes)
                .temperature(temperature)
                .icon(getStepIcon(type))
                .build();
    }
    
    private String getStepIcon(Recipe.StepType type) {
        return switch (type) {
            case MIX_PREFERMENT, MIX_DOUGH -> "bowl-mixing";
            case KNEAD -> "hand-paper";
            case BULK_FERMENTATION, ROOM_TEMP_PROOF -> "clock";
            case FOLD -> "arrows-alt-v";
            case DIVIDE, BALL_FORMING -> "cut";
            case COLD_PROOF -> "snowflake";
            case REMOVE_FROM_FRIDGE -> "temperature-high";
            case FINAL_PROOF -> "hourglass-half";
            case SHAPE -> "circle-notch";
            case BAKE -> "fire";
            default -> "check";
        };
    }
    
    private int calculateColdFermentationHours(CalculationRequest request) {
        return switch (request.getFermentationMethod()) {
            case COLD_FERMENTATION -> request.getTotalFermentationHours() - 4; // 4h na temp. pokojową
            case MIXED -> (int) (request.getTotalFermentationHours() * 0.7); // 70% w lodówce
            default -> 0;
        };
    }
    
    private int calculateBulkFermentationHours(CalculationRequest request) {
        return switch (request.getFermentationMethod()) {
            case ROOM_TEMPERATURE -> Math.max(2, request.getTotalFermentationHours() - 2);
            case COLD_FERMENTATION -> 2; // krótka fermentacja przed lodówką
            case MIXED -> (int) (request.getTotalFermentationHours() * 0.3);
            case SAME_DAY -> Math.max(1, request.getTotalFermentationHours() - 1);
        };
    }
    
    private int findInsertPosition(List<CalculationResponse.ScheduleStep> steps, LocalDateTime time) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getScheduledTime().isAfter(time)) {
                return i;
            }
        }
        return steps.size();
    }
    
    private String formatRelativeTime(LocalDateTime from, LocalDateTime to) {
        long minutes = ChronoUnit.MINUTES.between(from, to);
        
        if (minutes < 0) {
            minutes = Math.abs(minutes);
            if (minutes < 60) return String.format("%d min temu", minutes);
            if (minutes < 1440) return String.format("%dh %dmin temu", minutes / 60, minutes % 60);
            return String.format("%d dni temu", minutes / 1440);
        } else {
            if (minutes < 60) return String.format("Za %d min", minutes);
            if (minutes < 1440) return String.format("Za %dh %dmin", minutes / 60, minutes % 60);
            return String.format("Za %d dni", minutes / 1440);
        }
    }
    
    /**
     * Generuje wskazówki na podstawie parametrów.
     */
    private List<String> generateTips(CalculationRequest request) {
        List<String> tips = new ArrayList<>();
        
        // Wskazówki dotyczące hydratacji
        if (request.getHydration() >= 70) {
            tips.add("Przy wysokiej hydratacji (70%+) stosuj technikę składania ciasta (coil fold) " +
                    "zamiast intensywnego wyrabiania.");
            tips.add("Użyj wilgotnych rąk podczas formowania kulek - zapobiegnie to przywieraniu.");
        }
        
        if (request.getHydration() < 55) {
            tips.add("Niska hydratacja da sztywniejsze ciasto - idealne do pizzy w stylu nowojorskim " +
                    "lub włoskiego chleba.");
        }
        
        // Wskazówki dotyczące fermentacji
        if (request.getFermentationMethod() == Recipe.FermentationMethod.COLD_FERMENTATION) {
            tips.add("Długa fermentacja w lodówce rozwija głębszy smak i lepszą strawność ciasta.");
            tips.add("Wyjmij ciasto z lodówki minimum 2 godziny przed pieczeniem.");
        }
        
        // Wskazówki dotyczące typu pieca
        if (request.getOvenType() == OvenType.HOME_OVEN) {
            tips.add("W piekarniku domowym najlepiej użyć kamienia lub stali do pizzy " +
                    "na najniższej półce.");
            tips.add("Rozgrzewaj piekarnik na maksymalną temperaturę przez minimum 45 minut.");
        }
        
        // Wskazówki dotyczące stylu
        if (request.getPizzaStyle() == PizzaStyle.NEAPOLITAN) {
            tips.add("Autentyczna pizza neapolitańska powinna mieć \"leopardowanie\" - " +
                    "charakterystyczne ciemne plamki na spodzie.");
            tips.add("Cornicione (brzeg) powinien być puszysty i lekko zwęglony.");
        }
        
        // Temperatura pokojowa
        double roomTemp = request.getRoomTemperature() != null ? request.getRoomTemperature() : 22.0;
        if (roomTemp > 26) {
            tips.add("Przy wysokiej temperaturze pokojowej (>26°C) fermentacja będzie szybsza. " +
                    "Rozważ skrócenie czasu lub użycie mniejszej ilości drożdży.");
        }
        
        return tips;
    }
    
    /**
     * Zaokrągla do 1 miejsca po przecinku.
     */
    private double round(double value) {
        return round(value, 1);
    }
    
    /**
     * Zaokrągla do określonej liczby miejsc po przecinku.
     */
    private double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }
    
    // ========================================
    // OBSŁUGA MIKSÓW MĄK
    // ========================================
    
    /**
     * Oblicza średnią ważoną parametrów dla miksu mąk.
     * 
     * @param flourMix lista mąk z procentami
     * @return parametry miksu (średnia ważona białka, W, P/L, absorpcja)
     */
    public FlourMixParameters calculateFlourMixParameters(List<CalculationRequest.FlourMixEntry> flourMix) {
        if (flourMix == null || flourMix.isEmpty()) {
            return null;
        }
        
        double totalPercentage = flourMix.stream()
                .mapToDouble(CalculationRequest.FlourMixEntry::getPercentage)
                .sum();
        
        if (Math.abs(totalPercentage - 100.0) > 0.1) {
            log.warn("⚠️ Suma procentów mąk ({}) nie równa się 100%", totalPercentage);
        }
        
        double weightedProtein = 0.0;
        double weightedStrength = 0.0;
        double weightedExtensibility = 0.0;
        double weightedHydrationMin = 0.0;
        double weightedHydrationMax = 0.0;
        
        int strengthCount = 0;
        int extensibilityCount = 0;
        
        List<FlourMixParameters.FlourPortion> portions = new ArrayList<>();
        
        // Optymalizacja N+1 - pobierz wszystkie mąki jednym zapytaniem
        List<String> flourIds = flourMix.stream()
                .map(CalculationRequest.FlourMixEntry::getFlourId)
                .collect(java.util.stream.Collectors.toList());
        java.util.Map<String, Ingredient> flourMap = ingredientService.findAllByIdsAsMap(flourIds);
        
        for (CalculationRequest.FlourMixEntry entry : flourMix) {
            Ingredient flour = flourMap.get(entry.getFlourId());
            if (flour == null || flour.getFlourParameters() == null) {
                log.warn("⚠️ Nie znaleziono mąki o ID: {}", entry.getFlourId());
                continue;
            }
            
            double weight = entry.getPercentage() / 100.0;
            Ingredient.FlourParameters params = flour.getFlourParameters();
            
            weightedProtein += params.getProteinContent() * weight;
            weightedHydrationMin += params.getRecommendedHydrationMin() * weight;
            weightedHydrationMax += params.getRecommendedHydrationMax() * weight;
            
            if (params.getStrength() != null) {
                weightedStrength += params.getStrength() * weight;
                strengthCount++;
            }
            
            if (params.getExtensibility() != null) {
                weightedExtensibility += params.getExtensibility() * weight;
                extensibilityCount++;
            }
            
            portions.add(FlourMixParameters.FlourPortion.builder()
                    .flourId(flour.getId())
                    .flourName(flour.getName())
                    .brand(flour.getBrand())
                    .percentage(entry.getPercentage())
                    .proteinContent(params.getProteinContent())
                    .strength(params.getStrength())
                    .build());
        }
        
        return FlourMixParameters.builder()
                .portions(portions)
                .averageProtein(round(weightedProtein, 1))
                .averageStrength(strengthCount > 0 ? round(weightedStrength, 0) : null)
                .averageExtensibility(extensibilityCount > 0 ? round(weightedExtensibility, 2) : null)
                .recommendedHydrationMin(round(weightedHydrationMin, 0))
                .recommendedHydrationMax(round(weightedHydrationMax, 0))
                .build();
    }
    
    /**
     * Oblicza porcje mąk w gramach na podstawie miksu.
     * Uses batch query to avoid N+1 problem.
     */
    public List<Recipe.FlourPortion> calculateFlourPortions(
            List<CalculationRequest.FlourMixEntry> flourMix, double totalFlourGrams) {
        
        if (flourMix == null || flourMix.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Batch fetch all flours to avoid N+1 query problem
        List<String> flourIds = flourMix.stream()
                .map(CalculationRequest.FlourMixEntry::getFlourId)
                .toList();
        Map<String, Ingredient> flourMap = ingredientService.findAllByIdsAsMap(flourIds);
        
        List<Recipe.FlourPortion> portions = new ArrayList<>();
        
        for (CalculationRequest.FlourMixEntry entry : flourMix) {
            Ingredient flour = flourMap.get(entry.getFlourId());
            String flourName = flour != null ? 
                    flour.getName() + (flour.getBrand() != null ? " (" + flour.getBrand() + ")" : "") :
                    "Nieznana mąka";
            
            double grams = totalFlourGrams * (entry.getPercentage() / 100.0);
            
            portions.add(Recipe.FlourPortion.builder()
                    .flourId(entry.getFlourId())
                    .flourName(flourName)
                    .percentage(entry.getPercentage())
                    .grams(round(grams))
                    .build());
        }
        
        return portions;
    }
    
    /**
     * Parametry obliczonego miksu mąk.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlourMixParameters {
        private List<FlourPortion> portions;
        private double averageProtein;
        private Double averageStrength;
        private Double averageExtensibility;
        private double recommendedHydrationMin;
        private double recommendedHydrationMax;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FlourPortion {
            private String flourId;
            private String flourName;
            private String brand;
            private double percentage;
            private double proteinContent;
            private Double strength;
        }
    }
}
