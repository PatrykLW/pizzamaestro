package com.pizzamaestro.model;

import lombok.Getter;

/**
 * Typ miksera/metoda wyrabiania ciasta.
 * Wpływa na obliczenia DDT (współczynnik tarcia) i czas wyrabiania.
 */
@Getter
public enum MixerType {
    HAND_KNEADING(
            "Ręczne wyrabianie",
            "Tradycyjna metoda - pełna kontrola nad ciastem",
            0.3,
            12,
            85.0
    ),
    STAND_MIXER_HOME(
            "Mikser planetarny domowy",
            "KitchenAid, Bosch MUM i podobne - do 1kg mąki",
            0.5,
            10,
            72.0
    ),
    STAND_MIXER_PRO(
            "Mikser planetarny profesjonalny",
            "Większe miksery gastronomiczne 5-10L",
            0.7,
            8,
            78.0
    ),
    SPIRAL_MIXER(
            "Mikser spiralny",
            "Profesjonalny mikser do pizzerii - najefektywniejszy",
            0.9,
            6,
            85.0
    ),
    FORK_MIXER(
            "Mikser widełkowy",
            "Delikatne mieszanie widłami - minimalne utlenianie ciasta",
            0.4,
            15,
            80.0
    );
    
    private final String displayName;
    private final String description;
    private final double frictionFactor; // °C na minutę wyrabiania
    private final int typicalMixingTime; // minuty
    private final double maxRecommendedHydration; // %
    
    MixerType(String displayName, String description, double frictionFactor, 
              int typicalMixingTime, double maxRecommendedHydration) {
        this.displayName = displayName;
        this.description = description;
        this.frictionFactor = frictionFactor;
        this.typicalMixingTime = typicalMixingTime;
        this.maxRecommendedHydration = maxRecommendedHydration;
    }
}
