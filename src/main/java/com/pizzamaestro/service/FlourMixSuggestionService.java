package com.pizzamaestro.service;

import com.pizzamaestro.dto.request.CalculationRequest;
import com.pizzamaestro.model.Ingredient;
import com.pizzamaestro.model.PizzaStyle;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serwis sugestii miksów mąk.
 * 
 * Inteligentnie dobiera miksy mąk na podstawie:
 * - Stylu pizzy (wymagana siła W, zawartość białka)
 * - Dostępnych mąk użytkownika
 * - Optymalizacji dla docelowych parametrów
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlourMixSuggestionService {
    
    private final IngredientService ingredientService;
    
    /**
     * Sugeruje miks mąk dla danego stylu pizzy.
     * 
     * @param style styl pizzy
     * @param availableFlourIds lista ID dostępnych mąk (jeśli null - używa wszystkich)
     * @return sugerowany miks z wyjaśnieniem
     */
    public FlourMixSuggestion suggestForStyle(PizzaStyle style, List<String> availableFlourIds) {
        log.info("🌾 Sugestia miksu dla stylu: {}", style.getDisplayName());
        
        // Pobierz zalecane parametry dla stylu
        StyleRequirements requirements = getStyleRequirements(style);
        
        // Pobierz dostępne mąki
        List<Ingredient> availableFlours = getAvailableFlours(availableFlourIds);
        
        if (availableFlours.isEmpty()) {
            log.warn("⚠️ Brak dostępnych mąk do sugestii");
            return FlourMixSuggestion.builder()
                    .success(false)
                    .message("Brak dostępnych mąk. Dodaj mąki do profilu lub użyj domyślnych.")
                    .build();
        }
        
        // Znajdź optymalny miks
        return findOptimalMix(requirements, availableFlours, style);
    }
    
    /**
     * Sugeruje miks dla docelowych parametrów.
     */
    public FlourMixSuggestion suggestForTargetParameters(
            Double targetProtein, Double targetStrength, List<String> availableFlourIds) {
        
        log.info("🎯 Sugestia miksu dla parametrów: białko={}%, W={}", targetProtein, targetStrength);
        
        List<Ingredient> availableFlours = getAvailableFlours(availableFlourIds);
        
        if (availableFlours.isEmpty()) {
            return FlourMixSuggestion.builder()
                    .success(false)
                    .message("Brak dostępnych mąk.")
                    .build();
        }
        
        StyleRequirements requirements = StyleRequirements.builder()
                .targetProtein(targetProtein != null ? targetProtein : 12.0)
                .targetStrength(targetStrength != null ? targetStrength : 280.0)
                .proteinTolerance(1.0)
                .strengthTolerance(30.0)
                .build();
        
        return findOptimalMix(requirements, availableFlours, null);
    }
    
    /**
     * Optymalizuje istniejący miks - sugeruje proporcje dla listy mąk.
     */
    public FlourMixSuggestion optimizeMix(List<String> flourIds, PizzaStyle style) {
        if (flourIds == null || flourIds.size() < 2) {
            return FlourMixSuggestion.builder()
                    .success(false)
                    .message("Potrzeba co najmniej 2 mąk do optymalizacji miksu.")
                    .build();
        }
        
        // Optymalizacja N+1 - pobierz wszystkie mąki jednym zapytaniem
        List<Ingredient> flours = ingredientService.findAllByIds(flourIds).stream()
                .filter(f -> f.getFlourParameters() != null)
                .collect(Collectors.toList());
        
        if (flours.size() < 2) {
            return FlourMixSuggestion.builder()
                    .success(false)
                    .message("Nie znaleziono wystarczającej liczby mąk z parametrami.")
                    .build();
        }
        
        StyleRequirements requirements = style != null ? 
                getStyleRequirements(style) : 
                StyleRequirements.builder()
                        .targetProtein(12.5)
                        .targetStrength(280.0)
                        .proteinTolerance(1.0)
                        .strengthTolerance(30.0)
                        .build();
        
        return optimizeProportions(flours, requirements, style);
    }
    
    // ========================================
    // PRYWATNE METODY
    // ========================================
    
    private StyleRequirements getStyleRequirements(PizzaStyle style) {
        return switch (style) {
            case NEAPOLITAN -> StyleRequirements.builder()
                    .targetProtein(12.5)
                    .targetStrength(280.0)
                    .proteinTolerance(1.0)
                    .strengthTolerance(40.0)
                    .preferType00(true)
                    .description("Pizza neapolitańska wymaga mąki typu 00 o średniej sile W (260-300)")
                    .build();
                    
            case NEW_YORK -> StyleRequirements.builder()
                    .targetProtein(13.5)
                    .targetStrength(320.0)
                    .proteinTolerance(1.0)
                    .strengthTolerance(40.0)
                    .preferHighGluten(true)
                    .description("Pizza nowojorska wymaga mocniejszej mąki (W 300-350) dla elastycznego ciasta")
                    .build();
                    
            case ROMAN -> StyleRequirements.builder()
                    .targetProtein(13.0)
                    .targetStrength(300.0)
                    .proteinTolerance(1.5)
                    .strengthTolerance(50.0)
                    .description("Pizza rzymska wymaga mąki średnio-mocnej dla chrupiącej tekstury")
                    .build();
                    
            case DETROIT -> StyleRequirements.builder()
                    .targetProtein(14.0)
                    .targetStrength(340.0)
                    .proteinTolerance(1.0)
                    .strengthTolerance(40.0)
                    .preferHighGluten(true)
                    .description("Pizza Detroit wymaga mocnej mąki dla puszystej struktury")
                    .build();
                    
            case CHICAGO_DEEP_DISH -> StyleRequirements.builder()
                    .targetProtein(11.5)
                    .targetStrength(240.0)
                    .proteinTolerance(1.5)
                    .strengthTolerance(40.0)
                    .description("Deep dish wymaga słabszej mąki dla kruchego ciasta")
                    .build();
                    
            case SICILIAN -> StyleRequirements.builder()
                    .targetProtein(13.5)
                    .targetStrength(320.0)
                    .proteinTolerance(1.0)
                    .strengthTolerance(40.0)
                    .description("Pizza sycylijska wymaga mocnej mąki dla grubego, puszystego ciasta")
                    .build();
                    
            default -> StyleRequirements.builder()
                    .targetProtein(12.5)
                    .targetStrength(280.0)
                    .proteinTolerance(1.5)
                    .strengthTolerance(50.0)
                    .description("Standardowe parametry dla uniwersalnej pizzy")
                    .build();
        };
    }
    
    private List<Ingredient> getAvailableFlours(List<String> availableFlourIds) {
        List<Ingredient> allFlours = ingredientService.getAllFlours();
        
        if (availableFlourIds == null || availableFlourIds.isEmpty()) {
            // Zwróć wszystkie dostępne mąki
            return allFlours.stream()
                    .filter(f -> f.getFlourParameters() != null)
                    .collect(Collectors.toList());
        }
        
        // Filtruj do dostępnych
        Set<String> availableSet = new HashSet<>(availableFlourIds);
        return allFlours.stream()
                .filter(f -> availableSet.contains(f.getId()))
                .filter(f -> f.getFlourParameters() != null)
                .collect(Collectors.toList());
    }
    
    private FlourMixSuggestion findOptimalMix(
            StyleRequirements requirements, List<Ingredient> availableFlours, PizzaStyle style) {
        
        // 1. Sprawdź czy jest idealna pojedyncza mąka
        Ingredient perfectSingle = findPerfectSingleFlour(requirements, availableFlours);
        if (perfectSingle != null) {
            return createSingleFlourSuggestion(perfectSingle, requirements, style);
        }
        
        // 2. Znajdź najlepszą kombinację 2-3 mąk
        return findBestCombination(requirements, availableFlours, style);
    }
    
    private Ingredient findPerfectSingleFlour(StyleRequirements req, List<Ingredient> flours) {
        for (Ingredient flour : flours) {
            Ingredient.FlourParameters params = flour.getFlourParameters();
            
            boolean proteinOk = Math.abs(params.getProteinContent() - req.targetProtein) <= req.proteinTolerance;
            boolean strengthOk = params.getStrength() == null || 
                    Math.abs(params.getStrength() - req.targetStrength) <= req.strengthTolerance;
            
            if (proteinOk && strengthOk) {
                // Sprawdź dodatkowe preferencje
                if (req.preferType00 && params.getFlourType() != Ingredient.FlourType.TYPE_00) continue;
                if (req.preferHighGluten && params.getProteinContent() < 13.0) continue;
                
                return flour;
            }
        }
        return null;
    }
    
    private FlourMixSuggestion createSingleFlourSuggestion(
            Ingredient flour, StyleRequirements req, PizzaStyle style) {
        
        Ingredient.FlourParameters params = flour.getFlourParameters();
        
        List<CalculationRequest.FlourMixEntry> mix = List.of(
                CalculationRequest.FlourMixEntry.builder()
                        .flourId(flour.getId())
                        .percentage(100.0)
                        .build()
        );
        
        List<FlourMixSuggestion.FlourDetail> details = List.of(
                FlourMixSuggestion.FlourDetail.builder()
                        .flourId(flour.getId())
                        .flourName(flour.getName())
                        .brand(flour.getBrand())
                        .percentage(100.0)
                        .proteinContent(params.getProteinContent())
                        .strength(params.getStrength())
                        .build()
        );
        
        return FlourMixSuggestion.builder()
                .success(true)
                .isMix(false)
                .flourMix(mix)
                .flourDetails(details)
                .resultProtein(params.getProteinContent())
                .resultStrength(params.getStrength())
                .message(String.format("✅ %s (%s) idealnie pasuje do %s",
                        flour.getName(), flour.getBrand(),
                        style != null ? style.getDisplayName() : "Twoich wymagań"))
                .explanation(String.format("Ta mąka ma białko %.1f%% i siłę W %.0f, " +
                        "co jest optymalne dla tego stylu pizzy.",
                        params.getProteinContent(), 
                        params.getStrength() != null ? params.getStrength() : 0))
                .build();
    }
    
    private FlourMixSuggestion findBestCombination(
            StyleRequirements req, List<Ingredient> flours, PizzaStyle style) {
        
        // Sortuj mąki według odległości od celu
        flours.sort((a, b) -> {
            double distA = calculateDistance(a.getFlourParameters(), req);
            double distB = calculateDistance(b.getFlourParameters(), req);
            return Double.compare(distA, distB);
        });
        
        // Próbuj kombinacje 2 mąk
        if (flours.size() >= 2) {
            for (int i = 0; i < Math.min(flours.size(), 5); i++) {
                for (int j = i + 1; j < Math.min(flours.size(), 6); j++) {
                    FlourMixSuggestion suggestion = tryTwoFlourMix(
                            flours.get(i), flours.get(j), req, style);
                    if (suggestion != null && suggestion.isSuccess()) {
                        return suggestion;
                    }
                }
            }
        }
        
        // Fallback - użyj najlepszej pojedynczej mąki
        Ingredient best = flours.get(0);
        return createSingleFlourSuggestion(best, req, style);
    }
    
    private FlourMixSuggestion tryTwoFlourMix(
            Ingredient flour1, Ingredient flour2, StyleRequirements req, PizzaStyle style) {
        
        Ingredient.FlourParameters p1 = flour1.getFlourParameters();
        Ingredient.FlourParameters p2 = flour2.getFlourParameters();
        
        // Oblicz optymalną proporcję dla białka
        double protein1 = p1.getProteinContent();
        double protein2 = p2.getProteinContent();
        
        if (Math.abs(protein1 - protein2) < 0.5) {
            // Zbyt podobne mąki - nie ma sensu miksować
            return null;
        }
        
        // Oblicz proporcję żeby osiągnąć docelowe białko
        // targetProtein = protein1 * p + protein2 * (1-p)
        // p = (targetProtein - protein2) / (protein1 - protein2)
        double proportion = (req.targetProtein - protein2) / (protein1 - protein2);
        
        // Ogranicz do rozsądnych proporcji
        if (proportion < 0.2 || proportion > 0.8) {
            return null;
        }
        
        double pct1 = Math.round(proportion * 100 / 5) * 5; // Zaokrąglij do 5%
        double pct2 = 100 - pct1;
        
        // Oblicz wynikowe parametry
        double resultProtein = protein1 * (pct1/100) + protein2 * (pct2/100);
        Double resultStrength = null;
        if (p1.getStrength() != null && p2.getStrength() != null) {
            resultStrength = p1.getStrength() * (pct1/100) + p2.getStrength() * (pct2/100);
        }
        
        List<CalculationRequest.FlourMixEntry> mix = List.of(
                CalculationRequest.FlourMixEntry.builder()
                        .flourId(flour1.getId())
                        .percentage(pct1)
                        .build(),
                CalculationRequest.FlourMixEntry.builder()
                        .flourId(flour2.getId())
                        .percentage(pct2)
                        .build()
        );
        
        List<FlourMixSuggestion.FlourDetail> details = List.of(
                FlourMixSuggestion.FlourDetail.builder()
                        .flourId(flour1.getId())
                        .flourName(flour1.getName())
                        .brand(flour1.getBrand())
                        .percentage(pct1)
                        .proteinContent(p1.getProteinContent())
                        .strength(p1.getStrength())
                        .build(),
                FlourMixSuggestion.FlourDetail.builder()
                        .flourId(flour2.getId())
                        .flourName(flour2.getName())
                        .brand(flour2.getBrand())
                        .percentage(pct2)
                        .proteinContent(p2.getProteinContent())
                        .strength(p2.getStrength())
                        .build()
        );
        
        String styleName = style != null ? style.getDisplayName() : "Twoich wymagań";
        
        return FlourMixSuggestion.builder()
                .success(true)
                .isMix(true)
                .flourMix(mix)
                .flourDetails(details)
                .resultProtein(Math.round(resultProtein * 10) / 10.0)
                .resultStrength(resultStrength != null ? (double) Math.round(resultStrength) : null)
                .message(String.format("🎯 Miks %.0f%% %s + %.0f%% %s dla %s",
                        pct1, flour1.getName(), pct2, flour2.getName(), styleName))
                .explanation(String.format(
                        "Ten miks da białko %.1f%% i siłę W ~%.0f. " +
                        "%s wzmocni strukturę, a %s zapewni elastyczność.",
                        resultProtein, 
                        resultStrength != null ? resultStrength : 280,
                        protein1 > protein2 ? flour1.getName() : flour2.getName(),
                        protein1 > protein2 ? flour2.getName() : flour1.getName()))
                .build();
    }
    
    private FlourMixSuggestion optimizeProportions(
            List<Ingredient> flours, StyleRequirements req, PizzaStyle style) {
        
        // Dla 2 mąk - oblicz optymalną proporcję
        if (flours.size() == 2) {
            return tryTwoFlourMix(flours.get(0), flours.get(1), req, style);
        }
        
        // Dla więcej mąk - użyj uproszczonego algorytmu z pierwszymi dwoma mąkami
        // Pełna optymalizacja dla 3+ mąk wymaga algorytmu programowania liniowego
        return tryTwoFlourMix(flours.get(0), flours.get(1), req, style);
    }
    
    private double calculateDistance(Ingredient.FlourParameters params, StyleRequirements req) {
        double proteinDiff = Math.abs(params.getProteinContent() - req.targetProtein);
        double strengthDiff = params.getStrength() != null ? 
                Math.abs(params.getStrength() - req.targetStrength) / 10.0 : 0;
        return proteinDiff + strengthDiff;
    }
    
    // ========================================
    // DTO
    // ========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class StyleRequirements {
        private double targetProtein;
        private double targetStrength;
        private double proteinTolerance;
        private double strengthTolerance;
        private boolean preferType00;
        private boolean preferHighGluten;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlourMixSuggestion {
        private boolean success;
        private boolean isMix;
        private List<CalculationRequest.FlourMixEntry> flourMix;
        private List<FlourDetail> flourDetails;
        private double resultProtein;
        private Double resultStrength;
        private String message;
        private String explanation;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FlourDetail {
            private String flourId;
            private String flourName;
            private String brand;
            private double percentage;
            private double proteinContent;
            private Double strength;
        }
    }
}
