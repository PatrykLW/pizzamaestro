package com.pizzamaestro.constants;

/**
 * Stałe używane w kalkulacjach ciasta na pizzę.
 * Centralizuje wszystkie magic numbers z serwisów kalkulacyjnych.
 */
public final class CalculatorConstants {
    
    // ===============================================
    // PERCENTAGES
    // ===============================================
    public static final double PERCENTAGE_BASE = 100.0;
    public static final double ESTIMATED_YEAST_PERCENTAGE = 0.5;
    public static final double POOLISH_YEAST_PERCENTAGE = 0.001;
    public static final double BIGA_YEAST_PERCENTAGE = 0.002;
    public static final double DEFAULT_PREFERMENT_PERCENTAGE = 30.0;
    public static final int YEAST_PERCENTAGE_DECIMAL_PLACES = 3;
    
    // ===============================================
    // TEMPERATURES
    // ===============================================
    public static final double DEFAULT_ROOM_TEMPERATURE = 22.0;
    public static final double DEFAULT_FRIDGE_TEMPERATURE = 4.0;
    public static final double HIGH_TEMPERATURE_THRESHOLD = 26.0;
    public static final double VERY_HIGH_TEMPERATURE_THRESHOLD = 28.0;
    public static final double LOW_TEMPERATURE_THRESHOLD = 18.0;
    
    // ===============================================
    // HYDRATION THRESHOLDS
    // ===============================================
    public static final double VERY_LOW_HYDRATION = 55.0;
    public static final double LOW_HYDRATION = 60.0;
    public static final double STANDARD_HYDRATION = 65.0;
    public static final double MEDIUM_HIGH_HYDRATION = 70.0;
    public static final double HIGH_HYDRATION = 75.0;
    public static final double VERY_HIGH_HYDRATION = 80.0;
    public static final double NEAPOLITAN_MAX_HYDRATION = 70.0;
    public static final double NY_MAX_HYDRATION = 68.0;
    
    // ===============================================
    // FERMENTATION TIME (hours)
    // ===============================================
    public static final int VERY_SHORT_FERMENTATION_HOURS = 6;
    public static final int SHORT_FERMENTATION_HOURS = 12;
    public static final int STANDARD_FERMENTATION_HOURS = 24;
    public static final int LONG_FERMENTATION_HOURS = 48;
    public static final int VERY_LONG_FERMENTATION_HOURS = 72;
    public static final int DEFAULT_PREFERMENT_HOURS = 12;
    
    // ===============================================
    // TIME INTERVALS (minutes)
    // ===============================================
    public static final int MINUTES_PER_DAY = 1440;
    public static final int MIXING_TIME_MINUTES = 10;
    public static final int SHAPING_TIME_MINUTES = 15;
    public static final int FINAL_REST_COLD_MINUTES = 120;
    public static final int FINAL_REST_MIXED_MINUTES = 90;
    public static final int FINAL_REST_DEFAULT_MINUTES = 30;
    
    // ===============================================
    // FERMENTATION CALCULATION
    // ===============================================
    public static final int ROOM_TEMP_HOURS_FOR_COLD = 4;
    public static final double MIXED_COLD_PERCENTAGE = 0.7;
    public static final int BULK_FERMENTATION_COLD_HOURS = 2;
    public static final double MIXED_BULK_PERCENTAGE = 0.3;
    public static final int MAX_FOLDS = 4;
    
    // ===============================================
    // FLOUR STRENGTH (W value)
    // ===============================================
    public static final int WEAK_FLOUR_STRENGTH = 200;
    public static final int MEDIUM_FLOUR_STRENGTH = 260;
    public static final int STRONG_FLOUR_STRENGTH = 300;
    public static final int VERY_STRONG_FLOUR_STRENGTH = 350;
    
    // ===============================================
    // ENVIRONMENTAL CORRECTIONS
    // ===============================================
    public static final double BASE_HUMIDITY = 50.0;
    public static final int BASE_ALTITUDE = 0;
    public static final double BASE_PRESSURE_HPA = 1013.25;
    public static final double HUMIDITY_CORRECTION_FACTOR = 0.05;
    public static final double MIN_HYDRATION_CORRECTION = -3.0;
    public static final double MAX_HYDRATION_CORRECTION = 3.0;
    public static final int ALTITUDE_THRESHOLD_METERS = 500;
    public static final double YEAST_CORRECTION_PER_1000M = 5.0;
    public static final double MAX_YEAST_CORRECTION = -20.0;
    public static final double FERMENTATION_CORRECTION_PER_1000M = 8.0;
    public static final double TEMP_CORRECTION_FACTOR = 5.0;
    public static final double MIN_FERMENTATION_CORRECTION = -30.0;
    public static final double MAX_FERMENTATION_CORRECTION = 50.0;
    public static final double BAROMETRIC_SCALE_HEIGHT = 8500.0;
    public static final double HIGH_HUMIDITY_THRESHOLD = 70.0;
    public static final double LOW_HUMIDITY_THRESHOLD = 30.0;
    
    // ===============================================
    // Q10 FACTOR (fermentation rate)
    // ===============================================
    public static final double Q10_FACTOR = 2.0;
    public static final double TEMP_BASE_DIFF = 10.0;
    
    private CalculatorConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
