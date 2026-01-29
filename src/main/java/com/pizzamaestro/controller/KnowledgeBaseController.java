package com.pizzamaestro.controller;

import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.TechniqueGuide;
import com.pizzamaestro.service.TechniqueGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kontroler bazy wiedzy - przewodniki, techniki, informacje.
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Baza wiedzy", description = "Przewodniki po technikach pizzy")
public class KnowledgeBaseController {
    
    private final TechniqueGuideService guideService;
    
    // ========================================
    // PUBLICZNE ENDPOINTY
    // ========================================
    
    /**
     * Lista wszystkich kategorii.
     */
    @GetMapping("/categories")
    @Operation(summary = "Lista kategorii przewodników")
    public ResponseEntity<List<CategoryInfo>> getCategories() {
        List<CategoryInfo> categories = Arrays.stream(TechniqueGuide.TechniqueCategory.values())
                .map(cat -> CategoryInfo.builder()
                        .id(cat.name())
                        .name(cat.getDisplayName())
                        .description(cat.getDescription())
                        .guideCount(guideService.getByCategory(cat).size())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Wszystkie przewodniki (darmowe dla FREE).
     */
    @GetMapping("/guides")
    @Operation(summary = "Lista wszystkich przewodników")
    public ResponseEntity<List<TechniqueGuide>> getAllGuides(
            @RequestParam(required = false, defaultValue = "false") boolean premiumOnly) {
        
        log.info("📚 Pobieranie przewodników (premiumOnly={})", premiumOnly);
        
        List<TechniqueGuide> guides = premiumOnly ? 
                guideService.getAllGuides() : 
                guideService.getFreeGuides();
        
        return ResponseEntity.ok(guides);
    }
    
    /**
     * Przewodniki po kategorii.
     */
    @GetMapping("/guides/category/{category}")
    @Operation(summary = "Przewodniki w kategorii")
    public ResponseEntity<List<TechniqueGuide>> getByCategory(
            @PathVariable TechniqueGuide.TechniqueCategory category) {
        
        log.info("📚 Pobieranie przewodników dla kategorii: {}", category);
        return ResponseEntity.ok(guideService.getByCategory(category));
    }
    
    /**
     * Pojedynczy przewodnik po slug.
     */
    @GetMapping("/guides/{slug}")
    @Operation(summary = "Szczegóły przewodnika")
    public ResponseEntity<TechniqueGuide> getGuide(@PathVariable String slug) {
        log.info("📖 Pobieranie przewodnika: {}", slug);
        return ResponseEntity.ok(guideService.getBySlug(slug));
    }
    
    /**
     * Przewodniki dla stylu pizzy.
     */
    @GetMapping("/guides/style/{style}")
    @Operation(summary = "Przewodniki dla stylu pizzy")
    public ResponseEntity<List<TechniqueGuide>> getForStyle(@PathVariable PizzaStyle style) {
        log.info("📚 Przewodniki dla stylu: {}", style);
        return ResponseEntity.ok(guideService.getForStyle(style));
    }
    
    /**
     * Przewodniki po poziomie trudności.
     */
    @GetMapping("/guides/difficulty/{level}")
    @Operation(summary = "Przewodniki po poziomie trudności")
    public ResponseEntity<List<TechniqueGuide>> getByDifficulty(
            @PathVariable TechniqueGuide.DifficultyLevel level) {
        
        log.info("📚 Przewodniki dla poziomu: {}", level);
        return ResponseEntity.ok(guideService.getByDifficulty(level));
    }
    
    /**
     * Popularne przewodniki.
     */
    @GetMapping("/guides/popular")
    @Operation(summary = "Popularne przewodniki")
    public ResponseEntity<List<TechniqueGuide>> getPopular() {
        log.info("📚 Popularne przewodniki");
        return ResponseEntity.ok(guideService.getPopular());
    }
    
    /**
     * Wyszukiwanie przewodników.
     */
    @GetMapping("/guides/search")
    @Operation(summary = "Wyszukiwanie przewodników")
    public ResponseEntity<List<TechniqueGuide>> search(@RequestParam String q) {
        log.info("🔍 Wyszukiwanie: {}", q);
        return ResponseEntity.ok(guideService.search(q));
    }
    
    /**
     * Powiązane przewodniki.
     */
    @GetMapping("/guides/{slug}/related")
    @Operation(summary = "Powiązane przewodniki")
    public ResponseEntity<List<TechniqueGuide>> getRelated(@PathVariable String slug) {
        log.info("🔗 Powiązane z: {}", slug);
        return ResponseEntity.ok(guideService.getRelated(slug));
    }
    
    // ========================================
    // SZYBKIE INFORMACJE
    // ========================================
    
    /**
     * Informacje o sile mąki (W).
     */
    @GetMapping("/flour-strength")
    @Operation(summary = "Przewodnik po sile mąki W")
    public ResponseEntity<FlourStrengthGuide> getFlourStrengthGuide() {
        return ResponseEntity.ok(FlourStrengthGuide.builder()
                .title("Siła mąki - parametr W")
                .description("W (od włoskiego 'forza') mierzy siłę glutenu i zdolność mąki do wchłaniania wody.")
                .ranges(List.of(
                        FlourStrengthRange.builder()
                                .range("W < 180")
                                .category("Bardzo słaba")
                                .description("Mąka do ciast, tortów. NIE dla pizzy.")
                                .maxHydration(55)
                                .maxFermentation(4)
                                .build(),
                        FlourStrengthRange.builder()
                                .range("W 180-220")
                                .category("Słaba")
                                .description("Pizza same-day, krótka fermentacja.")
                                .maxHydration(60)
                                .maxFermentation(12)
                                .recommendedStyles(List.of("NEAPOLITAN (same-day)"))
                                .build(),
                        FlourStrengthRange.builder()
                                .range("W 220-260")
                                .category("Średnia")
                                .description("Standardowa pizza, fermentacja do 24h.")
                                .maxHydration(65)
                                .maxFermentation(24)
                                .recommendedStyles(List.of("NEAPOLITAN", "NEW_YORK"))
                                .build(),
                        FlourStrengthRange.builder()
                                .range("W 260-300")
                                .category("Mocna")
                                .description("Długa fermentacja, wyższa hydratacja.")
                                .maxHydration(72)
                                .maxFermentation(48)
                                .recommendedStyles(List.of("NEAPOLITAN", "NEW_YORK", "ROMAN"))
                                .build(),
                        FlourStrengthRange.builder()
                                .range("W 300-350")
                                .category("Bardzo mocna")
                                .description("Ekstremalna fermentacja, wysoka hydratacja.")
                                .maxHydration(80)
                                .maxFermentation(72)
                                .recommendedStyles(List.of("ROMAN", "FOCACCIA"))
                                .build(),
                        FlourStrengthRange.builder()
                                .range("W > 350")
                                .category("Manitoba")
                                .description("Dla ekstremalnych zastosowań, mieszana z słabszymi mąkami.")
                                .maxHydration(90)
                                .maxFermentation(96)
                                .recommendedStyles(List.of("ROMAN"))
                                .build()
                ))
                .tips(List.of(
                        "W to nie wszystko - ważne też P/L (elastyczność/rozciągliwość)",
                        "Mąki włoskie często mają niższe W niż polskie/niemieckie",
                        "Caputo Pizzeria ma W260-270, Caputo Nuvola W300-320",
                        "Przy długiej fermentacji mąka 'pracuje' - potrzebujesz wyższego W"
                ))
                .build());
    }
    
    /**
     * Tabela przeliczników drożdży.
     */
    @GetMapping("/yeast-conversion")
    @Operation(summary = "Przeliczniki drożdży")
    public ResponseEntity<YeastConversionGuide> getYeastConversion() {
        return ResponseEntity.ok(YeastConversionGuide.builder()
                .title("Przeliczniki drożdży")
                .baseAmount(10.0)
                .baseType("Świeże (drożdże piekarskie)")
                .conversions(Map.of(
                        "FRESH", 10.0,
                        "INSTANT_DRY", 4.0,
                        "ACTIVE_DRY", 5.0,
                        "SOURDOUGH", 30.0
                ))
                .tips(List.of(
                        "Suche instant - dodawaj bezpośrednio do mąki",
                        "Suche aktywne - aktywuj w ciepłej wodzie (35°C) przez 5-10 min",
                        "Świeże - można rozpuścić w wodzie lub kruszyć na mąkę",
                        "Zakwas - wymaga karmienia i więcej czasu"
                ))
                .storageInfo(Map.of(
                        "FRESH", "Lodówka, do 2 tygodni",
                        "INSTANT_DRY", "Suche miejsce, do 2 lat (otwarte: lodówka, 4 miesiące)",
                        "ACTIVE_DRY", "Suche miejsce, do 2 lat",
                        "SOURDOUGH", "Lodówka, karmienie co 1-2 tygodnie"
                ))
                .build());
    }
    
    /**
     * Tabela hydratacji.
     */
    @GetMapping("/hydration-guide")
    @Operation(summary = "Przewodnik po hydratacji")
    public ResponseEntity<HydrationGuide> getHydrationGuide() {
        return ResponseEntity.ok(HydrationGuide.builder()
                .title("Przewodnik po hydratacji")
                .description("Hydratacja to stosunek wody do mąki (Baker's Math)")
                .ranges(List.of(
                        HydrationRange.builder()
                                .range("< 55%")
                                .description("Bardzo sztywne ciasto")
                                .difficulty("Łatwe")
                                .texture("Twarde, chrupiące")
                                .bestFor(List.of("Crackery", "Bardzo cienka pizza"))
                                .build(),
                        HydrationRange.builder()
                                .range("55-60%")
                                .description("Sztywne ciasto")
                                .difficulty("Łatwe")
                                .texture("Chrupiące, mało puszystości")
                                .bestFor(List.of("Pizza dla początkujących", "Tavern style"))
                                .build(),
                        HydrationRange.builder()
                                .range("60-65%")
                                .description("Klasyczne ciasto")
                                .difficulty("Średnie")
                                .texture("Zbalansowane, lekko puszyste")
                                .bestFor(List.of("Neapolitańska", "NY style"))
                                .build(),
                        HydrationRange.builder()
                                .range("65-70%")
                                .description("Puszyste ciasto")
                                .difficulty("Średnie/Trudne")
                                .texture("Puszyste, duże bąble")
                                .bestFor(List.of("Neapolitańska współczesna", "Focaccia"))
                                .build(),
                        HydrationRange.builder()
                                .range("70-80%")
                                .description("Bardzo puszyste")
                                .difficulty("Trudne")
                                .texture("Bardzo lekkie, duże alweole")
                                .bestFor(List.of("Rzymska al taglio", "Focaccia"))
                                .build(),
                        HydrationRange.builder()
                                .range("> 80%")
                                .description("Ekstremalne")
                                .difficulty("Ekspert")
                                .texture("Niemal płynne, wymaga blaszki")
                                .bestFor(List.of("Pizza w blaszce", "Focaccia"))
                                .build()
                ))
                .tips(List.of(
                        "Zacznij od niższej hydratacji i zwiększaj z doświadczeniem",
                        "Wyższa hydratacja = więcej składań (stretch & fold) zamiast wyrabiania",
                        "Mąka z wyższym W pozwala na wyższą hydratację",
                        "Technika autolizy ułatwia pracę z wysoką hydratacją"
                ))
                .build());
    }
    
    // ========================================
    // DTOs
    // ========================================
    
    @lombok.Data
    @lombok.Builder
    public static class CategoryInfo {
        private String id;
        private String name;
        private String description;
        private int guideCount;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class FlourStrengthGuide {
        private String title;
        private String description;
        private List<FlourStrengthRange> ranges;
        private List<String> tips;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class FlourStrengthRange {
        private String range;
        private String category;
        private String description;
        private int maxHydration;
        private int maxFermentation;
        private List<String> recommendedStyles;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class YeastConversionGuide {
        private String title;
        private double baseAmount;
        private String baseType;
        private Map<String, Double> conversions;
        private List<String> tips;
        private Map<String, String> storageInfo;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class HydrationGuide {
        private String title;
        private String description;
        private List<HydrationRange> ranges;
        private List<String> tips;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class HydrationRange {
        private String range;
        private String description;
        private String difficulty;
        private String texture;
        private List<String> bestFor;
    }
}
