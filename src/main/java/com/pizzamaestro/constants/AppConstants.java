package com.pizzamaestro.constants;

/**
 * Stałe aplikacji używane w całym projekcie.
 * Zapobiega duplikacji literałów string i ułatwia utrzymanie kodu.
 */
public final class AppConstants {
    
    // ===============================================
    // THEME
    // ===============================================
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    
    // ===============================================
    // LANGUAGE
    // ===============================================
    public static final String DEFAULT_LANGUAGE = "pl";
    public static final String LANGUAGE_ENGLISH = "en";
    
    // ===============================================
    // COUNTRIES
    // ===============================================
    public static final String COUNTRY_ITALY = "Włochy";
    public static final String COUNTRY_POLAND = "Polska";
    public static final String COUNTRY_USA = "USA";
    public static final String COUNTRY_FRANCE = "Francja";
    public static final String COUNTRY_GERMANY = "Niemcy";
    public static final String COUNTRY_UK = "Wielka Brytania";
    public static final String COUNTRY_PAKISTAN = "Pakistan";
    public static final String COUNTRY_VARIOUS = "Różne";
    
    // ===============================================
    // FLOUR BRANDS
    // ===============================================
    public static final String BRAND_CAPUTO = "Caputo";
    public static final String BRAND_LE_5_STAGIONI = "Le 5 Stagioni";
    public static final String BRAND_MOLINO_GRASSI = "Molino Grassi";
    public static final String BRAND_DALLAGIOVANNA = "Dallagiovanna";
    public static final String BRAND_LUBELLA = "Lubella";
    public static final String BRAND_KING_ARTHUR = "King Arthur";
    public static final String BRAND_GENERAL_MILLS = "General Mills";
    public static final String BRAND_VARIOUS = "Różni";
    
    // ===============================================
    // GRAIN TYPES
    // ===============================================
    public static final String GRAIN_WHEAT = "pszenna";
    public static final String GRAIN_RYE = "żytnia";
    public static final String GRAIN_SPELT = "orkiszowa";
    
    // ===============================================
    // WATER SOURCES
    // ===============================================
    public static final String WATER_SOURCE_BOTTLED = "butelkowana";
    public static final String WATER_SOURCE_TAP = "kranowa";
    public static final String WATER_SOURCE_FILTERED = "filtrowana";
    
    // ===============================================
    // NUMERIC CONSTANTS
    // ===============================================
    public static final double FLOUR_PERCENTAGE = 100.0;
    public static final int MINUTES_PER_DAY = 1440;
    public static final int DEFAULT_REST_MINUTES = 120;
    public static final double BASE_ATMOSPHERIC_PRESSURE = 1013.25;
    
    // ===============================================
    // YEAST CONVERSION FACTORS
    // ===============================================
    public static final double FRESH_YEAST_FACTOR = 1.0;
    public static final double INSTANT_DRY_YEAST_FACTOR = 0.33;
    public static final double ACTIVE_DRY_YEAST_FACTOR = 0.40;
    
    private AppConstants() {
        // Prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
