package com.pizzamaestro.service;

import com.pizzamaestro.model.Ingredient;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Silnik interaktywnych wskazówek.
 * 
 * Analizuje parametry wprowadzane przez użytkownika i generuje
 * kontekstowe tipy, ostrzeżenia i rekomendacje w czasie rzeczywistym.
 * 
 * Zasada działania:
 * 1. Użytkownik zmienia parametr (np. hydratację)
 * 2. TipEngine analizuje zmianę w kontekście innych parametrów
 * 3. Generuje odpowiednie tipy wyjaśniające wpływ zmiany
 * 4. Sugeruje optymalne wartości
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TipEngineService {
    
    // ========================================
    // GŁÓWNE METODY GENEROWANIA TIPÓW
    // ========================================
    
    /**
     * Generuje wszystkie tipy dla aktualnej konfiguracji.
     */
    public TipCollection generateAllTips(CalculationContext context) {
        log.info("💡 Generowanie tipów dla kontekstu: style={}, hydration={}, fermentation={}h",
                context.getPizzaStyle(), context.getHydration(), context.getFermentationHours());
        
        List<Tip> tips = new ArrayList<>();
        List<Tip> warnings = new ArrayList<>();
        List<Tip> recommendations = new ArrayList<>();
        
        // Tipy dla stylu pizzy
        tips.addAll(generateStyleTips(context));
        
        // Tipy dla hydratacji
        tips.addAll(generateHydrationTips(context));
        warnings.addAll(generateHydrationWarnings(context));
        
        // Tipy dla fermentacji
        tips.addAll(generateFermentationTips(context));
        warnings.addAll(generateFermentationWarnings(context));
        
        // Tipy dla mąki
        if (context.getFlourStrength() != null) {
            tips.addAll(generateFlourTips(context));
            warnings.addAll(generateFlourWarnings(context));
        }
        
        // Tipy dla temperatury
        tips.addAll(generateTemperatureTips(context));
        warnings.addAll(generateTemperatureWarnings(context));
        
        // Rekomendacje optymalizacyjne
        recommendations.addAll(generateOptimizationRecommendations(context));
        
        // Tipy dla pogody
        if (context.getWeatherTemperature() != null) {
            tips.addAll(generateWeatherTips(context));
        }
        
        log.info("✅ Wygenerowano {} tipów, {} ostrzeżeń, {} rekomendacji",
                tips.size(), warnings.size(), recommendations.size());
        
        return TipCollection.builder()
                .tips(tips)
                .warnings(warnings)
                .recommendations(recommendations)
                .contextSummary(generateContextSummary(context))
                .build();
    }
    
    /**
     * Generuje tipy dla konkretnej zmiany parametru.
     */
    public List<Tip> generateTipsForChange(String parameterName, Object oldValue, Object newValue, CalculationContext context) {
        log.info("🔄 Zmiana parametru: {} = {} → {}", parameterName, oldValue, newValue);
        
        List<Tip> tips = new ArrayList<>();
        
        switch (parameterName.toLowerCase()) {
            case "hydration" -> tips.addAll(explainHydrationChange((Number) oldValue, (Number) newValue, context));
            case "fermentationhours" -> tips.addAll(explainFermentationChange((Number) oldValue, (Number) newValue, context));
            case "pizzastyle" -> tips.addAll(explainStyleChange((String) oldValue, (String) newValue, context));
            case "flourstrength" -> tips.addAll(explainFlourStrengthChange((Number) oldValue, (Number) newValue, context));
            case "yeasttype" -> tips.addAll(explainYeastTypeChange((String) oldValue, (String) newValue, context));
            case "roomtemperature" -> tips.addAll(explainTemperatureChange((Number) oldValue, (Number) newValue, context));
            case "usepreferment" -> tips.addAll(explainPrefermentChange((Boolean) oldValue, (Boolean) newValue, context));
            default -> log.debug("Brak specjalnych tipów dla parametru: {}", parameterName);
        }
        
        return tips;
    }
    
    // ========================================
    // TIPY DLA STYLU PIZZY
    // ========================================
    
    private List<Tip> generateStyleTips(CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        PizzaStyle style = context.getPizzaStyle();
        
        if (style == null) return tips;
        
        tips.add(Tip.builder()
                .type(TipType.INFO)
                .category(TipCategory.STYLE)
                .title("Styl: " + style.getDisplayName())
                .content(getStyleDescription(style))
                .details(getStyleDetails(style))
                .icon("🍕")
                .priority(1)
                .build());
        
        // Zalecane parametry dla stylu
        tips.add(Tip.builder()
                .type(TipType.RECOMMENDATION)
                .category(TipCategory.STYLE)
                .title("Zalecane parametry")
                .content(getStyleRecommendedParams(style))
                .icon("📊")
                .priority(2)
                .build());
        
        return tips;
    }
    
    private String getStyleDescription(PizzaStyle style) {
        return switch (style) {
            case NEAPOLITAN -> "Tradycyjna neapolitańska wymaga mąki W280-320, krótkiego pieczenia w wysokiej temperaturze (450-500°C) i miękkich, elastycznych brzegów (cornicione).";
            case NEW_YORK -> "NY style to cienka, chrupiąca pizza z możliwością złożenia. Wymaga mąki wysokobłkowej (13-14%), twardej wody i dłuższej fermentacji.";
            case ROMAN -> "Rzymska al taglio - wysoka hydratacja (75-85%), długa fermentacja, lekkie i puszyste ciasto pieczone w blaszce.";
            case DETROIT -> "Detroit style - grube ciasto w prostokątnej blaszce, ser do samych brzegów, chrupiące krawędzie. Wymaga długiej fermentacji.";
            case SICILIAN -> "Sycylijska - grube, puszyste ciasto w blaszce, przypominające focaccię. Duża ilość oliwy dla chrupkości.";
            case FOCACCIA -> "Focaccia - włoski chlebek z oliwą, ziołami i dodatkami. Wysoka hydratacja, dużo oliwy.";
            case PAN -> "Pan pizza - głęboka patelnia, miękkie puszyste ciasto, gruby spód.";
            default -> style.getDescription();
        };
    }
    
    private String getStyleDetails(PizzaStyle style) {
        return switch (style) {
            case NEAPOLITAN -> """
                    🌡️ Temperatura pieca: 450-500°C
                    ⏱️ Czas pieczenia: 60-90 sekund
                    💧 Hydratacja: 58-65%
                    🌾 Mąka: Typu 00, W260-320
                    ⏰ Fermentacja: 8-24h
                    """;
            case NEW_YORK -> """
                    🌡️ Temperatura pieca: 290-320°C
                    ⏱️ Czas pieczenia: 6-8 minut
                    💧 Hydratacja: 60-65%
                    🌾 Mąka: High-gluten, 13-14% białka
                    ⏰ Fermentacja: 24-72h (zimna)
                    🫒 Oliwa: 2-3%
                    🍬 Cukier: 1-2%
                    """;
            case ROMAN -> """
                    🌡️ Temperatura pieca: 280-300°C
                    ⏱️ Czas pieczenia: 8-12 minut
                    💧 Hydratacja: 75-85%
                    🌾 Mąka: W300-350
                    ⏰ Fermentacja: 48-96h (zimna)
                    🫒 Oliwa: 3-5%
                    """;
            default -> "";
        };
    }
    
    private String getStyleRecommendedParams(PizzaStyle style) {
        return switch (style) {
            case NEAPOLITAN -> "Hydratacja 60-65%, fermentacja 8-24h, temperatura pokojowa 18-24°C, mąka W280-320";
            case NEW_YORK -> "Hydratacja 60-65%, fermentacja 24-72h w lodówce, mąka 13%+ białka, 2% oliwy, 1% cukru";
            case ROMAN -> "Hydratacja 75-85%, fermentacja 48-96h w lodówce, mąka W300-350, 3-5% oliwy";
            case DETROIT -> "Hydratacja 70-75%, fermentacja 24-48h, ser do brzegów, pieczenie w blaszce z oliwą";
            default -> style.getDescription();
        };
    }
    
    // ========================================
    // TIPY DLA HYDRATACJI
    // ========================================
    
    private List<Tip> generateHydrationTips(CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        double hydration = context.getHydration();
        
        String hydrationLevel;
        String explanation;
        String handling;
        
        if (hydration < 55) {
            hydrationLevel = "Bardzo niska";
            explanation = "Ciasto będzie sztywne i trudne do rozciągnięcia. Nadaje się do crackerów lub bardzo cienkiej pizzy.";
            handling = "Łatwe w obsłudze, nie przykleja się do rąk.";
        } else if (hydration < 60) {
            hydrationLevel = "Niska";
            explanation = "Ciasto sztywne, idealne dla początkujących. Łatwe w formowaniu, mniej puszystości.";
            handling = "Bardzo łatwe w obsłudze. Idealne na start.";
        } else if (hydration < 65) {
            hydrationLevel = "Standardowa";
            explanation = "Klasyczna hydratacja dla większości stylów. Dobry balans między obsługą a puszystością.";
            handling = "Wymaga podstawowej wprawy. Może lekko kleić.";
        } else if (hydration < 70) {
            hydrationLevel = "Średnio-wysoka";
            explanation = "Ciasto bardziej puszyste i elastyczne. Większe bąble, lżejsza tekstura.";
            handling = "Wymaga doświadczenia. Ciasto klei się - używaj mąki lub oliwy.";
        } else if (hydration < 80) {
            hydrationLevel = "Wysoka";
            explanation = "Ciasto bardzo puszyste, duże bąble, lekka struktura. Typowe dla rzymskiej al taglio.";
            handling = "Trudne w obsłudze - wymaga techniki wet hands lub dużo mąki.";
        } else {
            hydrationLevel = "Bardzo wysoka";
            explanation = "Ciasto niemal płynne. Ekstremalna puszystość, wymaga pieczenia w blaszce.";
            handling = "Bardzo trudne - tylko dla ekspertów. Użyj techniki coil fold.";
        }
        
        tips.add(Tip.builder()
                .type(TipType.INFO)
                .category(TipCategory.HYDRATION)
                .title(String.format("Hydratacja %d%% - %s", (int) hydration, hydrationLevel))
                .content(explanation)
                .details(handling)
                .icon("💧")
                .priority(1)
                .build());
        
        // Tip o wpływie na gluten
        tips.add(Tip.builder()
                .type(TipType.SCIENCE)
                .category(TipCategory.HYDRATION)
                .title("Wpływ na gluten")
                .content(getGlutenHydrationExplanation(hydration))
                .icon("🔬")
                .priority(3)
                .build());
        
        return tips;
    }
    
    private String getGlutenHydrationExplanation(double hydration) {
        if (hydration < 60) {
            return "Niska hydratacja = gęsta sieć glutenowa, ciasto sztywne ale mocne. Gluten szybko się rozwija podczas wyrabiania.";
        } else if (hydration < 70) {
            return "Umiarkowana hydratacja = zbalansowana sieć glutenowa. Ciasto elastyczne i wytrzymałe. Optymalny rozwój glutenu.";
        } else {
            return "Wysoka hydratacja = luźna sieć glutenowa, więcej miejsca na gaz. Gluten rozwija się wolniej, wymaga techniki składania (fold) zamiast intensywnego wyrabiania.";
        }
    }
    
    private List<Tip> generateHydrationWarnings(CalculationContext context) {
        List<Tip> warnings = new ArrayList<>();
        double hydration = context.getHydration();
        PizzaStyle style = context.getPizzaStyle();
        Integer flourStrength = context.getFlourStrength();
        
        // Sprawdź zgodność z stylem
        if (style == PizzaStyle.NEAPOLITAN && hydration > 70) {
            warnings.add(Tip.builder()
                    .type(TipType.WARNING)
                    .category(TipCategory.HYDRATION)
                    .title("Wysoka hydratacja dla neapolitańskiej")
                    .content(String.format("Hydratacja %d%% jest wysoka dla pizzy neapolitańskiej (zalecane 58-65%%). Ciasto może być trudne do rozciągnięcia i formowania klasycznego cornicione.", (int) hydration))
                    .suggestion("Rozważ zmniejszenie hydratacji do 60-65% lub zmień styl na rzymską.")
                    .icon("⚠️")
                    .priority(1)
                    .build());
        }
        
        if (style == PizzaStyle.NEW_YORK && hydration > 68) {
            warnings.add(Tip.builder()
                    .type(TipType.WARNING)
                    .category(TipCategory.HYDRATION)
                    .title("Wysoka hydratacja dla NY style")
                    .content("NY style tradycyjnie ma niższą hydratację (60-65%) dla uzyskania chrupiącego, składanego plastra.")
                    .suggestion("Zmniejsz hydratację lub przygotuj się na miększe ciasto.")
                    .icon("⚠️")
                    .priority(2)
                    .build());
        }
        
        // Sprawdź zgodność z siłą mąki
        if (flourStrength != null) {
            if (hydration > 75 && flourStrength < 280) {
                warnings.add(Tip.builder()
                        .type(TipType.WARNING)
                        .category(TipCategory.HYDRATION)
                        .title("Mąka może nie utrzymać tej hydratacji")
                        .content(String.format("Hydratacja %d%% wymaga silnej mąki (W280+). Twoja mąka (W%d) może nie wchłonąć tyle wody.", (int) hydration, flourStrength))
                        .suggestion("Zmniejsz hydratację do 65-70% lub użyj silniejszej mąki.")
                        .icon("⚠️")
                        .priority(1)
                        .build());
            }
        }
        
        return warnings;
    }
    
    private List<Tip> explainHydrationChange(Number oldValue, Number newValue, CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        double oldH = oldValue.doubleValue();
        double newH = newValue.doubleValue();
        double diff = newH - oldH;
        
        String direction = diff > 0 ? "Zwiększenie" : "Zmniejszenie";
        String impact;
        
        if (Math.abs(diff) >= 10) {
            impact = "znacząca zmiana";
        } else if (Math.abs(diff) >= 5) {
            impact = "zauważalna zmiana";
        } else {
            impact = "niewielka zmiana";
        }
        
        tips.add(Tip.builder()
                .type(TipType.CHANGE_EXPLANATION)
                .category(TipCategory.HYDRATION)
                .title(String.format("%s hydratacji o %d%%", direction, (int) Math.abs(diff)))
                .content(String.format("To %s która wpłynie na:\n• Puszystość ciasta: %s\n• Łatwość obsługi: %s\n• Czas wyrabiania: %s",
                        impact,
                        diff > 0 ? "większa" : "mniejsza",
                        diff > 0 ? "trudniejsza" : "łatwiejsza",
                        diff > 0 ? "dłuższy (więcej składania)" : "krótszy"))
                .icon("🔄")
                .priority(1)
                .build());
        
        // Tip o dostosowaniu techniki
        if (newH > 70) {
            tips.add(Tip.builder()
                    .type(TipType.RECOMMENDATION)
                    .category(TipCategory.HYDRATION)
                    .title("Zalecana technika")
                    .content("Przy hydratacji >70% używaj techniki stretch & fold lub coil fold zamiast tradycyjnego wyrabiania. Wykonuj 3-4 serie składań co 30 minut.")
                    .icon("👐")
                    .priority(2)
                    .build());
        }
        
        return tips;
    }
    
    // ========================================
    // TIPY DLA FERMENTACJI
    // ========================================
    
    private List<Tip> generateFermentationTips(CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        int hours = context.getFermentationHours();
        Recipe.FermentationMethod method = context.getFermentationMethod();
        
        String fermentationDescription;
        String flavorImpact;
        String yeastImpact;
        
        if (hours <= 6) {
            fermentationDescription = "Bardzo krótka fermentacja - same-day pizza";
            flavorImpact = "Minimalne rozwinięcie smaku, ciasto głównie o smaku mąki";
            yeastImpact = "Wymaga większej ilości drożdży (2-3% świeżych)";
        } else if (hours <= 12) {
            fermentationDescription = "Krótka fermentacja - pizza tego samego dnia";
            flavorImpact = "Lekko rozwinięty smak, dobra dla prostych receptur";
            yeastImpact = "Standardowa ilość drożdży (1-2% świeżych)";
        } else if (hours <= 24) {
            fermentationDescription = "Standardowa fermentacja - dobra równowaga";
            flavorImpact = "Dobrze rozwinięty smak, lekkość ciasta";
            yeastImpact = "Mniejsza ilość drożdży (0.5-1% świeżych)";
        } else if (hours <= 48) {
            fermentationDescription = "Długa fermentacja - rozwinięty smak";
            flavorImpact = "Bogaty, złożony smak, lepsze trawienie";
            yeastImpact = "Minimalna ilość drożdży (0.1-0.5% świeżych)";
        } else if (hours <= 72) {
            fermentationDescription = "Bardzo długa fermentacja - pełen rozwój smaku";
            flavorImpact = "Kompleksowy smak, doskonała strawność, nuty kwasowe";
            yeastImpact = "Bardzo mało drożdży (0.05-0.2% świeżych)";
        } else {
            fermentationDescription = "Ekstremalna fermentacja - dla koneserów";
            flavorImpact = "Intensywne aromaty, wyraźna kwasowość";
            yeastImpact = "Minimalne drożdże (0.02-0.1%) lub zakwas";
        }
        
        tips.add(Tip.builder()
                .type(TipType.INFO)
                .category(TipCategory.FERMENTATION)
                .title(String.format("Fermentacja %dh - %s", hours, fermentationDescription))
                .content(String.format("Smak: %s\n\nDrożdże: %s", flavorImpact, yeastImpact))
                .icon("⏰")
                .priority(1)
                .build());
        
        // Tip o metodzie fermentacji
        if (method != null) {
            tips.add(Tip.builder()
                    .type(TipType.INFO)
                    .category(TipCategory.FERMENTATION)
                    .title("Metoda: " + method.getDisplayName())
                    .content(getFermentationMethodDescription(method, hours))
                    .icon("🌡️")
                    .priority(2)
                    .build());
        }
        
        // Nauka o fermentacji
        tips.add(Tip.builder()
                .type(TipType.SCIENCE)
                .category(TipCategory.FERMENTATION)
                .title("Co dzieje się podczas fermentacji?")
                .content("""
                        🦠 Drożdże rozkładają cukry na CO₂ i alkohol (etanol)
                        🔬 Enzymy rozkładają białka i skrobię na prostsze związki
                        🍞 Kwasy organiczne tworzą charakterystyczny smak
                        💪 Sieć glutenowa staje się silniejsza i bardziej elastyczna
                        """)
                .icon("🔬")
                .priority(4)
                .build());
        
        return tips;
    }
    
    private String getFermentationMethodDescription(Recipe.FermentationMethod method, int hours) {
        return switch (method) {
            case ROOM_TEMPERATURE -> String.format("""
                    Fermentacja w temperaturze pokojowej (20-24°C):
                    • Szybsza aktywność drożdży
                    • Czas: %dh
                    • Idealne dla krótszych fermentacji
                    • Monitoruj ciasto - może przefermentować
                    """, hours);
            case COLD_FERMENTATION -> String.format("""
                    Fermentacja w lodówce (4-6°C):
                    • Wolna, kontrolowana fermentacja
                    • Czas: %dh
                    • Głębszy rozwój smaku
                    • Wyjmij ciasto 2h przed formowaniem
                    """, hours);
            case MIXED -> String.format("""
                    Fermentacja mieszana:
                    • Start w temp. pokojowej (2-4h)
                    • Następnie lodówka (%dh - 4h)
                    • Najlepsze z obu światów
                    • Wyjmij 2h przed pieczeniem
                    """, hours);
            case SAME_DAY -> String.format("""
                    Pizza tego samego dnia:
                    • Tylko temp. pokojowa
                    • Czas: %dh
                    • Więcej drożdży potrzebne
                    • Szybki wynik, mniej smaku
                    """, hours);
        };
    }
    
    private List<Tip> generateFermentationWarnings(CalculationContext context) {
        List<Tip> warnings = new ArrayList<>();
        int hours = context.getFermentationHours();
        Recipe.FermentationMethod method = context.getFermentationMethod();
        double roomTemp = context.getRoomTemperature() != null ? context.getRoomTemperature() : 22;
        
        // Krótka fermentacja w pokoju
        if (hours > 12 && method == Recipe.FermentationMethod.ROOM_TEMPERATURE && roomTemp > 24) {
            warnings.add(Tip.builder()
                    .type(TipType.WARNING)
                    .category(TipCategory.FERMENTATION)
                    .title("Ryzyko przefermentowania")
                    .content(String.format("Fermentacja %dh w temperaturze %.0f°C może prowadzić do przefermentowania. Ciasto może stać się zbyt kwaśne i stracić strukturę.", hours, roomTemp))
                    .suggestion("Użyj fermentacji mieszanej lub zimnej dla dłuższych czasów.")
                    .icon("⚠️")
                    .priority(1)
                    .build());
        }
        
        // Bardzo krótka zimna fermentacja
        if (hours < 12 && method == Recipe.FermentationMethod.COLD_FERMENTATION) {
            warnings.add(Tip.builder()
                    .type(TipType.WARNING)
                    .category(TipCategory.FERMENTATION)
                    .title("Za krótka zimna fermentacja")
                    .content(String.format("%dh w lodówce to za mało - drożdże nie zdążą się aktywować w niskiej temperaturze.", hours))
                    .suggestion("Zimna fermentacja wymaga minimum 24h lub użyj metody pokojowej.")
                    .icon("⚠️")
                    .priority(1)
                    .build());
        }
        
        return warnings;
    }
    
    private List<Tip> explainFermentationChange(Number oldValue, Number newValue, CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        int oldH = oldValue.intValue();
        int newH = newValue.intValue();
        
        if (newH > oldH) {
            tips.add(Tip.builder()
                    .type(TipType.CHANGE_EXPLANATION)
                    .category(TipCategory.FERMENTATION)
                    .title(String.format("Wydłużenie fermentacji: %dh → %dh", oldH, newH))
                    .content(String.format("""
                            ✅ Więcej smaku i aromatu
                            ✅ Lepsza strawność (więcej rozłożonego glutenu)
                            ✅ Lżejsze ciasto
                            ⚠️ Algorytm zmniejszy ilość drożdży o ~%.0f%%
                            """, calculateYeastReduction(oldH, newH)))
                    .icon("⏰")
                    .priority(1)
                    .build());
        } else {
            tips.add(Tip.builder()
                    .type(TipType.CHANGE_EXPLANATION)
                    .category(TipCategory.FERMENTATION)
                    .title(String.format("Skrócenie fermentacji: %dh → %dh", oldH, newH))
                    .content(String.format("""
                            ⚡ Szybszy wynik
                            ⚠️ Mniej rozwinięty smak
                            ⚠️ Algorytm zwiększy ilość drożdży o ~%.0f%%
                            💡 Rozważ użycie prefermentu dla lepszego smaku
                            """, calculateYeastIncrease(oldH, newH)))
                    .icon("⏰")
                    .priority(1)
                    .build());
        }
        
        return tips;
    }
    
    private double calculateYeastReduction(int oldHours, int newHours) {
        // Przybliżone obliczenie - podwojenie czasu = ~50% mniej drożdży
        return (1 - (double) oldHours / newHours) * 100;
    }
    
    private double calculateYeastIncrease(int oldHours, int newHours) {
        return ((double) oldHours / newHours - 1) * 100;
    }
    
    // ========================================
    // TIPY DLA MĄKI
    // ========================================
    
    private List<Tip> generateFlourTips(CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        Integer strength = context.getFlourStrength();
        Double protein = context.getFlourProtein();
        
        if (strength != null) {
            String strengthCategory;
            String bestFor;
            
            if (strength < 200) {
                strengthCategory = "Słaba mąka";
                bestFor = "Idealna dla ciast, ciasteczek. NIE dla pizzy.";
            } else if (strength < 260) {
                strengthCategory = "Średnia mąka";
                bestFor = "Dobra dla pizzy same-day, krótka fermentacja (do 12h).";
            } else if (strength < 300) {
                strengthCategory = "Mocna mąka";
                bestFor = "Idealna dla pizzy neapolitańskiej, 12-48h fermentacji.";
            } else if (strength < 350) {
                strengthCategory = "Bardzo mocna mąka";
                bestFor = "Doskonała dla długich fermentacji (48-72h), wysokich hydratacji.";
            } else {
                strengthCategory = "Manitoba / Super mocna";
                bestFor = "Dla ekstremalnych fermentacji, bardzo wysokich hydratacji (80%+).";
            }
            
            tips.add(Tip.builder()
                    .type(TipType.INFO)
                    .category(TipCategory.FLOUR)
                    .title(String.format("Siła mąki W%d - %s", strength, strengthCategory))
                    .content(bestFor)
                    .details(getFlourStrengthDetails(strength))
                    .icon("🌾")
                    .priority(1)
                    .build());
        }
        
        if (protein != null) {
            tips.add(Tip.builder()
                    .type(TipType.INFO)
                    .category(TipCategory.FLOUR)
                    .title(String.format("Białko %.1f%%", protein))
                    .content(getProteinExplanation(protein))
                    .icon("💪")
                    .priority(2)
                    .build());
        }
        
        // Nauka o sile mąki
        tips.add(Tip.builder()
                .type(TipType.SCIENCE)
                .category(TipCategory.FLOUR)
                .title("Co oznacza parametr W?")
                .content("""
                        W (siła mąki) mierzy zdolność glutenu do:
                        • Wchłaniania wody
                        • Zatrzymywania gazów fermentacji
                        • Tworzenia elastycznej sieci
                        
                        Wyższa W = więcej wody, dłuższa fermentacja, większa puszystość
                        """)
                .icon("🔬")
                .priority(4)
                .build());
        
        return tips;
    }
    
    private String getFlourStrengthDetails(int strength) {
        return String.format("""
                Zalecane parametry dla W%d:
                • Max hydratacja: ~%d%%
                • Max fermentacja: ~%dh
                • Optymalna temp. wody: %d°C
                """,
                strength,
                calculateMaxHydration(strength),
                calculateMaxFermentation(strength),
                calculateOptimalWaterTemp(strength));
    }
    
    private int calculateMaxHydration(int strength) {
        // Przybliżenie: W200 = 60%, W300 = 75%, W400 = 90%
        return Math.min(90, 50 + strength / 5);
    }
    
    private int calculateMaxFermentation(int strength) {
        // Przybliżenie: W200 = 12h, W300 = 72h, W350+ = 96h+
        if (strength < 220) return 12;
        if (strength < 260) return 24;
        if (strength < 300) return 48;
        if (strength < 350) return 72;
        return 96;
    }
    
    private int calculateOptimalWaterTemp(int strength) {
        // Silniejsza mąka = cieplejsza woda (dłużej się hydratuje)
        return Math.min(30, 20 + (strength - 200) / 20);
    }
    
    private String getProteinExplanation(double protein) {
        if (protein < 10) {
            return "Niska zawartość białka - mąka do ciast, nie nadaje się do pizzy.";
        } else if (protein < 12) {
            return "Średnia zawartość białka - dobra dla pizzy neapolitańskiej, delikatne ciasto.";
        } else if (protein < 14) {
            return "Wysoka zawartość białka - idealna dla NY style, mocna sieć glutenowa.";
        } else {
            return "Bardzo wysoka zawartość białka (high-gluten) - wymaga dłuższej fermentacji, bardzo silne ciasto.";
        }
    }
    
    private List<Tip> generateFlourWarnings(CalculationContext context) {
        List<Tip> warnings = new ArrayList<>();
        Integer strength = context.getFlourStrength();
        double hydration = context.getHydration();
        int fermentationHours = context.getFermentationHours();
        
        if (strength != null) {
            int maxHydration = calculateMaxHydration(strength);
            int maxFermentation = calculateMaxFermentation(strength);
            
            if (hydration > maxHydration) {
                warnings.add(Tip.builder()
                        .type(TipType.WARNING)
                        .category(TipCategory.FLOUR)
                        .title("Hydratacja przekracza możliwości mąki")
                        .content(String.format("Mąka W%d może nie utrzymać hydratacji %d%% (max ~%d%%).", strength, (int) hydration, maxHydration))
                        .suggestion(String.format("Zmniejsz hydratację do %d%% lub użyj silniejszej mąki.", maxHydration))
                        .icon("⚠️")
                        .priority(1)
                        .build());
            }
            
            if (fermentationHours > maxFermentation) {
                warnings.add(Tip.builder()
                        .type(TipType.WARNING)
                        .category(TipCategory.FLOUR)
                        .title("Fermentacja może być za długa")
                        .content(String.format("Mąka W%d może nie wytrzymać %dh fermentacji (zalecane max ~%dh).", strength, fermentationHours, maxFermentation))
                        .suggestion("Skróć fermentację lub użyj silniejszej mąki.")
                        .icon("⚠️")
                        .priority(1)
                        .build());
            }
        }
        
        return warnings;
    }
    
    private List<Tip> explainFlourStrengthChange(Number oldValue, Number newValue, CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        int oldW = oldValue.intValue();
        int newW = newValue.intValue();
        
        tips.add(Tip.builder()
                .type(TipType.CHANGE_EXPLANATION)
                .category(TipCategory.FLOUR)
                .title(String.format("Zmiana mąki: W%d → W%d", oldW, newW))
                .content(String.format("""
                        %s mąka pozwala na:
                        • Max hydratacja: %d%% → %d%%
                        • Max fermentacja: %dh → %dh
                        • %s
                        """,
                        newW > oldW ? "Silniejsza" : "Słabsza",
                        calculateMaxHydration(oldW), calculateMaxHydration(newW),
                        calculateMaxFermentation(oldW), calculateMaxFermentation(newW),
                        newW > oldW ? "Możesz zwiększyć hydratację i/lub fermentację" : "Rozważ zmniejszenie hydratacji i/lub fermentacji"))
                .icon("🌾")
                .priority(1)
                .build());
        
        return tips;
    }
    
    // ========================================
    // TIPY DLA TEMPERATURY
    // ========================================
    
    private List<Tip> generateTemperatureTips(CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        Double roomTemp = context.getRoomTemperature();
        
        if (roomTemp != null) {
            String tempDescription;
            String impact;
            
            if (roomTemp < 18) {
                tempDescription = "Niska temperatura";
                impact = "Fermentacja znacznie zwolniona. Może wymagać więcej drożdży lub dłuższego czasu.";
            } else if (roomTemp < 22) {
                tempDescription = "Chłodno";
                impact = "Fermentacja nieco wolniejsza. Dobre warunki dla kontrolowanego wzrostu.";
            } else if (roomTemp < 26) {
                tempDescription = "Optymalna temperatura";
                impact = "Idealne warunki dla aktywności drożdży. Standardowe parametry.";
            } else if (roomTemp < 30) {
                tempDescription = "Ciepło";
                impact = "Szybsza fermentacja. Monitoruj ciasto, może wymagać mniej drożdży.";
            } else {
                tempDescription = "Gorąco";
                impact = "Bardzo szybka fermentacja. Ryzyko przefermentowania. Rozważ lodówkę.";
            }
            
            tips.add(Tip.builder()
                    .type(TipType.INFO)
                    .category(TipCategory.TEMPERATURE)
                    .title(String.format("%.0f°C - %s", roomTemp, tempDescription))
                    .content(impact)
                    .details(String.format("Każde 5°C zmienia szybkość fermentacji o ~50%%.\nPrzy %.0f°C fermentacja jest %s niż przy 22°C.",
                            roomTemp, roomTemp > 22 ? "szybsza" : "wolniejsza"))
                    .icon("🌡️")
                    .priority(2)
                    .build());
        }
        
        return tips;
    }
    
    private List<Tip> generateTemperatureWarnings(CalculationContext context) {
        List<Tip> warnings = new ArrayList<>();
        Double roomTemp = context.getRoomTemperature();
        int fermentationHours = context.getFermentationHours();
        
        if (roomTemp != null && roomTemp > 28 && fermentationHours > 6) {
            warnings.add(Tip.builder()
                    .type(TipType.WARNING)
                    .category(TipCategory.TEMPERATURE)
                    .title("Wysoka temperatura + długa fermentacja")
                    .content(String.format("Przy %.0f°C i %dh fermentacji ciasto może przefermentować.", roomTemp, fermentationHours))
                    .suggestion("Użyj lodówki dla części fermentacji lub skróć czas.")
                    .icon("⚠️")
                    .priority(1)
                    .build());
        }
        
        return warnings;
    }
    
    private List<Tip> explainTemperatureChange(Number oldValue, Number newValue, CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        double oldT = oldValue.doubleValue();
        double newT = newValue.doubleValue();
        double diff = newT - oldT;
        
        // Oblicz wpływ na czas fermentacji (Q10 ≈ 2)
        double fermentationFactor = Math.pow(2, diff / 10);
        
        tips.add(Tip.builder()
                .type(TipType.CHANGE_EXPLANATION)
                .category(TipCategory.TEMPERATURE)
                .title(String.format("Zmiana temperatury: %.0f°C → %.0f°C", oldT, newT))
                .content(String.format("""
                        %s temperatury o %.0f°C oznacza:
                        • Fermentacja %s o ~%.0f%%
                        • %s
                        • Algorytm automatycznie dostosuje ilość drożdży
                        """,
                        diff > 0 ? "Podwyższenie" : "Obniżenie",
                        Math.abs(diff),
                        diff > 0 ? "przyspieszona" : "spowolniona",
                        Math.abs(fermentationFactor - 1) * 100,
                        diff > 0 ? "Monitoruj ciasto częściej" : "Ciasto będzie rosło wolniej"))
                .icon("🌡️")
                .priority(1)
                .build());
        
        return tips;
    }
    
    // ========================================
    // TIPY DLA POGODY
    // ========================================
    
    private List<Tip> generateWeatherTips(CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        Double weatherTemp = context.getWeatherTemperature();
        Double humidity = context.getWeatherHumidity();
        
        if (weatherTemp != null) {
            tips.add(Tip.builder()
                    .type(TipType.INFO)
                    .category(TipCategory.WEATHER)
                    .title(String.format("Pogoda: %.0f°C", weatherTemp))
                    .content(String.format("""
                            Aktualna pogoda wpływa na ciasto:
                            • Temperatura otoczenia: %.0f°C
                            • Wilgotność: %.0f%%
                            • Algorytm automatycznie dostosował parametry
                            """, weatherTemp, humidity != null ? humidity : 60))
                    .icon("🌤️")
                    .priority(3)
                    .build());
        }
        
        return tips;
    }
    
    // ========================================
    // TIPY DLA PREFERMENTU
    // ========================================
    
    private List<Tip> explainPrefermentChange(Boolean oldValue, Boolean newValue, CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        
        if (Boolean.TRUE.equals(newValue)) {
            tips.add(Tip.builder()
                    .type(TipType.CHANGE_EXPLANATION)
                    .category(TipCategory.PREFERMENT)
                    .title("Włączono preferment")
                    .content("""
                            Preferment (poolish/biga) doda:
                            ✅ Głębszy, bardziej złożony smak
                            ✅ Lepszą strukturę miękiszu
                            ✅ Dłuższy czas świeżości
                            ✅ Lepszą strawność
                            
                            ⚠️ Wymaga wcześniejszego przygotowania (8-18h przed)
                            """)
                    .icon("🥖")
                    .priority(1)
                    .build());
        } else if (Boolean.TRUE.equals(oldValue)) {
            tips.add(Tip.builder()
                    .type(TipType.CHANGE_EXPLANATION)
                    .category(TipCategory.PREFERMENT)
                    .title("Wyłączono preferment")
                    .content("""
                            Bez prefermentu:
                            ⚡ Prostszy proces
                            ⚡ Krótszy czas przygotowania
                            ⚠️ Mniej złożony smak
                            
                            💡 Dla lepszego smaku wydłuż fermentację główną
                            """)
                    .icon("🥖")
                    .priority(1)
                    .build());
        }
        
        return tips;
    }
    
    private List<Tip> explainStyleChange(String oldValue, String newValue, CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        
        try {
            PizzaStyle oldStyle = PizzaStyle.valueOf(oldValue);
            PizzaStyle newStyle = PizzaStyle.valueOf(newValue);
            
            tips.add(Tip.builder()
                    .type(TipType.CHANGE_EXPLANATION)
                    .category(TipCategory.STYLE)
                    .title(String.format("Zmiana stylu: %s → %s", oldStyle.getDisplayName(), newStyle.getDisplayName()))
                    .content(String.format("""
                            Nowy styl wymaga innych parametrów:
                            • %s
                            
                            Algorytm automatycznie dostosuje zalecane wartości.
                            """, getStyleDescription(newStyle)))
                    .icon("🍕")
                    .priority(1)
                    .build());
        } catch (Exception e) {
            log.warn("Nie można sparsować stylu pizzy: {} -> {}", oldValue, newValue);
        }
        
        return tips;
    }
    
    private List<Tip> explainYeastTypeChange(String oldValue, String newValue, CalculationContext context) {
        List<Tip> tips = new ArrayList<>();
        
        tips.add(Tip.builder()
                .type(TipType.CHANGE_EXPLANATION)
                .category(TipCategory.FERMENTATION)
                .title(String.format("Zmiana drożdży: %s → %s", oldValue, newValue))
                .content(getYeastConversionInfo(oldValue, newValue))
                .icon("🍞")
                .priority(1)
                .build());
        
        return tips;
    }
    
    private String getYeastConversionInfo(String from, String to) {
        return """
                Przelicznik drożdży:
                • 10g świeżych = 4g suchych instant = 5g suchych aktywnych
                • Suche instant - bez aktywacji, bezpośrednio do mąki
                • Suche aktywne - wymagają aktywacji w ciepłej wodzie (5-10 min)
                • Świeże - najlepsza aktywność, krótszy czas przechowywania
                """;
    }
    
    // ========================================
    // REKOMENDACJE OPTYMALIZACYJNE
    // ========================================
    
    private List<Tip> generateOptimizationRecommendations(CalculationContext context) {
        List<Tip> recommendations = new ArrayList<>();
        
        // Rekomendacja dla początkujących
        if (context.getHydration() > 70 && context.getFermentationHours() < 24) {
            recommendations.add(Tip.builder()
                    .type(TipType.RECOMMENDATION)
                    .category(TipCategory.OPTIMIZATION)
                    .title("Sugestia dla lepszego wyniku")
                    .content("Przy wysokiej hydratacji (>70%) zalecana jest dłuższa fermentacja (24h+) dla pełnego rozwinięcia glutenu.")
                    .icon("💡")
                    .priority(2)
                    .build());
        }
        
        // Rekomendacja dla długiej fermentacji bez zimnej
        if (context.getFermentationHours() > 24 && 
            context.getFermentationMethod() == Recipe.FermentationMethod.ROOM_TEMPERATURE) {
            recommendations.add(Tip.builder()
                    .type(TipType.RECOMMENDATION)
                    .category(TipCategory.OPTIMIZATION)
                    .title("Rozważ fermentację zimną")
                    .content("Dla fermentacji >24h zalecana jest metoda zimna lub mieszana dla lepszej kontroli i głębszego smaku.")
                    .icon("❄️")
                    .priority(1)
                    .build());
        }
        
        return recommendations;
    }
    
    // ========================================
    // PODSUMOWANIE KONTEKSTU
    // ========================================
    
    private String generateContextSummary(CalculationContext context) {
        return String.format("""
                📊 Podsumowanie konfiguracji:
                🍕 Styl: %s
                💧 Hydratacja: %.0f%%
                ⏰ Fermentacja: %dh (%s)
                🌡️ Temp. pokojowa: %.0f°C
                %s
                """,
                context.getPizzaStyle() != null ? context.getPizzaStyle().getDisplayName() : "Nie wybrano",
                context.getHydration(),
                context.getFermentationHours(),
                context.getFermentationMethod() != null ? context.getFermentationMethod().getDisplayName() : "?",
                context.getRoomTemperature() != null ? context.getRoomTemperature() : 22,
                context.getFlourStrength() != null ? String.format("🌾 Mąka: W%d", context.getFlourStrength()) : "");
    }
    
    // ========================================
    // DTOs
    // ========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculationContext {
        private PizzaStyle pizzaStyle;
        private double hydration;
        private int fermentationHours;
        private Recipe.FermentationMethod fermentationMethod;
        private Double roomTemperature;
        private Double fridgeTemperature;
        private Integer flourStrength; // W
        private Double flourProtein;
        private String yeastType;
        private boolean usePreferment;
        private Double weatherTemperature;
        private Double weatherHumidity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipCollection {
        private List<Tip> tips;
        private List<Tip> warnings;
        private List<Tip> recommendations;
        private String contextSummary;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tip {
        private TipType type;
        private TipCategory category;
        private String title;
        private String content;
        private String details;
        private String suggestion;
        private String icon;
        private int priority; // 1 = najwyższy
    }
    
    public enum TipType {
        INFO,
        WARNING,
        RECOMMENDATION,
        SCIENCE,
        CHANGE_EXPLANATION
    }
    
    public enum TipCategory {
        STYLE,
        HYDRATION,
        FERMENTATION,
        FLOUR,
        TEMPERATURE,
        WEATHER,
        PREFERMENT,
        OPTIMIZATION
    }
}
