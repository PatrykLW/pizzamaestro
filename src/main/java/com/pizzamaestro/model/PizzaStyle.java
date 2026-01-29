package com.pizzamaestro.model;

import lombok.Getter;

/**
 * Enumaracja stylów pizzy z domyślnymi parametrami.
 * Każdy styl ma predefiniowane wartości nawodnienia, czasu fermentacji,
 * dodatków i zalecanych temperatur wypieku.
 */
@Getter
public enum PizzaStyle {
    
    NEAPOLITAN(
        "Neapolitańska",
        "Klasyczna pizza z Neapolu - cienkie, miękkie ciasto z charakterystycznym brzegiem (cornicione)",
        65.0, // domyślna hydratacja
        60.0, 70.0, // zakres hydratacji
        250, // domyślna waga kulki
        24, // domyślny czas fermentacji (godziny)
        2.8, // % soli
        0.0, // % oliwy (brak w tradycyjnej)
        0.0, // % cukru
        OvenType.WOOD_FIRED,
        450, // temperatura pieca
        90 // czas pieczenia w sekundach
    ),
    
    NEW_YORK(
        "Nowojorska",
        "Duże, cienkie kawałki z chrupiącym spodem i elastycznym ciastem",
        60.0,
        55.0, 65.0,
        280,
        24,
        2.5,
        2.0, // z oliwą
        1.0, // z cukrem
        OvenType.DECK_OVEN,
        290,
        420
    ),
    
    ROMAN(
        "Rzymska (scrocchiarella)",
        "Bardzo cienka, chrupiąca pizza z Rzymu",
        70.0,
        65.0, 80.0,
        220,
        48,
        2.5,
        3.0,
        0.0,
        OvenType.ELECTRIC_PIZZA_OVEN,
        350,
        180
    ),
    
    DETROIT(
        "Detroit",
        "Gruba, prostokątna pizza z chrupiącymi brzegami z sera",
        70.0,
        65.0, 75.0,
        350,
        4,
        2.5,
        4.0,
        2.0,
        OvenType.HOME_OVEN,
        250,
        900
    ),
    
    CHICAGO_DEEP_DISH(
        "Chicago Deep Dish",
        "Głęboka pizza z Chicaco z obfitym nadzieniem",
        55.0,
        50.0, 60.0,
        400,
        24,
        2.0,
        5.0,
        1.0,
        OvenType.HOME_OVEN,
        220,
        1800
    ),
    
    SICILIAN(
        "Sycylijska (sfincione)",
        "Gruba, puszysta pizza z blachy",
        65.0,
        60.0, 70.0,
        350,
        12,
        2.5,
        3.0,
        0.0,
        OvenType.HOME_OVEN,
        250,
        1200
    ),
    
    FOCACCIA(
        "Focaccia",
        "Włoski chlebek z oliwą - baza dla różnych dodatków",
        75.0,
        70.0, 85.0,
        300,
        8,
        2.5,
        6.0,
        0.0,
        OvenType.HOME_OVEN,
        220,
        1500
    ),
    
    PIZZA_BIANCA(
        "Pizza Bianca",
        "Rzymska pizza bez sosu - tylko oliwa, sól i rozmaryn",
        80.0,
        75.0, 85.0,
        280,
        72,
        2.8,
        4.0,
        0.0,
        OvenType.ELECTRIC_PIZZA_OVEN,
        300,
        420
    ),
    
    GRANDMA(
        "Grandma Style",
        "Cienka, prostokątna pizza z Long Island",
        60.0,
        55.0, 65.0,
        300,
        6,
        2.5,
        3.0,
        1.0,
        OvenType.HOME_OVEN,
        260,
        900
    ),
    
    PAN(
        "Pan Pizza",
        "Gruba pizza z patelni z chrupiącym spodem",
        65.0,
        60.0, 70.0,
        320,
        8,
        2.5,
        4.0,
        2.0,
        OvenType.HOME_OVEN,
        250,
        1200
    ),
    
    THIN_CRUST(
        "Cienka chrupiąca",
        "Bardzo cienka i chrupiąca pizza w stylu barowym/restauracyjnym",
        55.0,
        50.0, 60.0,
        200,
        6,
        2.5,
        2.0,
        1.0,
        OvenType.HOME_OVEN,
        280,
        480
    ),
    
    TAVERN_STYLE(
        "Tavern Style (Chicago)",
        "Cienka, chrupiąca pizza krojona w kwadraty - popularna w Chicago",
        52.0,
        48.0, 56.0,
        220,
        4,
        2.5,
        3.0,
        2.0,
        OvenType.HOME_OVEN,
        260,
        600
    ),
    
    PINSA_ROMANA(
        "Pinsa Romana",
        "Starożytny rzymski przodek pizzy - mieszanka mąk, bardzo wysoka hydratacja",
        80.0,
        75.0, 85.0,
        260,
        72,
        2.5,
        2.0,
        0.0,
        OvenType.ELECTRIC_PIZZA_OVEN,
        350,
        240
    ),
    
    CUSTOM(
        "Własny styl",
        "Zdefiniuj własne parametry",
        62.0,
        45.0, 90.0,
        250,
        12,
        2.5,
        0.0,
        0.0,
        OvenType.HOME_OVEN,
        250,
        600
    );
    
    private final String displayName;
    private final String description;
    private final double defaultHydration;
    private final double minHydration;
    private final double maxHydration;
    private final int defaultBallWeight;
    private final int defaultFermentationHours;
    private final double defaultSaltPercentage;
    private final double defaultOilPercentage;
    private final double defaultSugarPercentage;
    private final OvenType recommendedOven;
    private final int ovenTemperature;
    private final int bakingTimeSeconds;
    
    PizzaStyle(String displayName, String description, 
               double defaultHydration, double minHydration, double maxHydration,
               int defaultBallWeight, int defaultFermentationHours,
               double defaultSaltPercentage, double defaultOilPercentage, double defaultSugarPercentage,
               OvenType recommendedOven, int ovenTemperature, int bakingTimeSeconds) {
        this.displayName = displayName;
        this.description = description;
        this.defaultHydration = defaultHydration;
        this.minHydration = minHydration;
        this.maxHydration = maxHydration;
        this.defaultBallWeight = defaultBallWeight;
        this.defaultFermentationHours = defaultFermentationHours;
        this.defaultSaltPercentage = defaultSaltPercentage;
        this.defaultOilPercentage = defaultOilPercentage;
        this.defaultSugarPercentage = defaultSugarPercentage;
        this.recommendedOven = recommendedOven;
        this.ovenTemperature = ovenTemperature;
        this.bakingTimeSeconds = bakingTimeSeconds;
    }
}
