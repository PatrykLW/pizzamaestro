package com.pizzamaestro.model;

import lombok.Getter;

/**
 * Typy pieców do pizzy z ich charakterystykami.
 * 
 * Uwzględnia piece z oddzielnymi grzałkami góra/dół (np. Effeuno).
 */
@Getter
public enum OvenType {
    
    HOME_OVEN(
        "Piekarnik domowy",
        "Standardowy piekarnik domowy",
        180, 280,
        true, false,
        null, null, null, null,
        "Dla najlepszych efektów użyj kamienia lub stali do pizzy. " +
        "Rozgrzej piekarnik na maksymalną temperaturę przez minimum 45 minut."
    ),
    
    HOME_OVEN_WITH_STONE(
        "Piekarnik z kamieniem",
        "Piekarnik domowy z kamieniem do pizzy",
        200, 300,
        true, false,
        null, null, null, null,
        "Kamień do pizzy akumuluje ciepło i daje lepszy efekt wypieku. " +
        "Rozgrzewaj kamień przez minimum 1 godzinę na maksymalnej temperaturze."
    ),
    
    HOME_OVEN_WITH_STEEL(
        "Piekarnik ze stalą",
        "Piekarnik domowy ze stalą do pizzy (baking steel)",
        200, 300,
        true, false,
        null, null, null, null,
        "Stal przewodzi ciepło lepiej niż kamień - krótszy czas wypieku. " +
        "Rozgrzewaj stalową płytę przez minimum 45 minut."
    ),
    
    ELECTRIC_PIZZA_OVEN(
        "Piec elektryczny (Effeuno/Ooni)",
        "Kompaktowy piec elektryczny do pizzy typu Effeuno, Ooni Volt",
        350, 500,
        false, true, // hasSeparateTopBottom = true
        420, 480, 350, 400, // topTempMin, topTempMax, bottomTempMin, bottomTempMax
        "Idealne do pizzy neapolitańskiej w domowych warunkach. " +
        "Umożliwia osiągnięcie temperatur niedostępnych dla zwykłych piekarników. " +
        "Zalecane ustawienia: góra 450-480°C, dół 380-420°C dla pizzy neapolitańskiej."
    ),
    
    GAS_PIZZA_OVEN(
        "Piec gazowy (Ooni/Roccbox)",
        "Przenośny piec gazowy do pizzy",
        400, 500,
        false, false,
        null, null, null, null,
        "Szybkie nagrzewanie i wysokie temperatury. " +
        "Wymaga regularnego obracania pizzy dla równomiernego wypieku."
    ),
    
    WOOD_FIRED(
        "Piec na drewno",
        "Tradycyjny piec opalany drewnem",
        400, 500,
        false, false,
        null, null, null, null,
        "Autentyczny wypiek neapolitański. Pizza piecze się w 60-90 sekund. " +
        "Wymaga wprawy w zarządzaniu ogniem i obracaniu pizzy."
    ),
    
    DECK_OVEN(
        "Piec pokładowy",
        "Profesjonalny piec pokładowy (deck oven)",
        280, 400,
        false, true, // hasSeparateTopBottom = true (wiele pieców pokładowych ma tę opcję)
        300, 380, 280, 350,
        "Standardowy piec w profesjonalnych pizzeriach. " +
        "Równomierne rozprowadzenie ciepła, idealne dla większych ilości. " +
        "Regulacja góra/dół pozwala na idealne dopasowanie wypieku."
    ),
    
    CONVEYOR_OVEN(
        "Piec przelotowy",
        "Piec z taśmą przenośnikową",
        250, 320,
        false, false,
        null, null, null, null,
        "Używany w sieciach pizzerii. Stały czas wypieku."
    );
    
    private final String displayName;
    private final String description;
    private final int minTemperature;
    private final int maxTemperature;
    private final boolean preheatingRequired;
    private final boolean hasSeparateTopBottom;
    private final Integer topTempMin;
    private final Integer topTempMax;
    private final Integer bottomTempMin;
    private final Integer bottomTempMax;
    private final String tips;
    
    OvenType(String displayName, String description, 
             int minTemperature, int maxTemperature,
             boolean preheatingRequired, boolean hasSeparateTopBottom,
             Integer topTempMin, Integer topTempMax,
             Integer bottomTempMin, Integer bottomTempMax,
             String tips) {
        this.displayName = displayName;
        this.description = description;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.preheatingRequired = preheatingRequired;
        this.hasSeparateTopBottom = hasSeparateTopBottom;
        this.topTempMin = topTempMin;
        this.topTempMax = topTempMax;
        this.bottomTempMin = bottomTempMin;
        this.bottomTempMax = bottomTempMax;
        this.tips = tips;
    }
    
    /**
     * Zwraca zalecaną temperaturę dla danego typu pieca.
     */
    public int getRecommendedTemperature() {
        return (minTemperature + maxTemperature) / 2;
    }
    
    /**
     * Zwraca zalecaną temperaturę górnej grzałki (dla pieców z tą opcją).
     */
    public Integer getRecommendedTopTemperature() {
        if (!hasSeparateTopBottom || topTempMin == null || topTempMax == null) {
            return null;
        }
        return (topTempMin + topTempMax) / 2;
    }
    
    /**
     * Zwraca zalecaną temperaturę dolnej grzałki (dla pieców z tą opcją).
     */
    public Integer getRecommendedBottomTemperature() {
        if (!hasSeparateTopBottom || bottomTempMin == null || bottomTempMax == null) {
            return null;
        }
        return (bottomTempMin + bottomTempMax) / 2;
    }
}
