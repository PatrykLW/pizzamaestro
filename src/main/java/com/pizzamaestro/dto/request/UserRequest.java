package com.pizzamaestro.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * DTOs dla żądań związanych z użytkownikiem i jego profilem.
 */
public class UserRequest {

    /**
     * Aktualizacja ustawień sprzętu użytkownika.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateEquipmentRequest {
        
        private String defaultOvenType; // np. "HOME_OVEN", "ELECTRIC_PIZZA_OVEN"
        
        private String defaultMixerType; // np. "HAND_KNEADING", "STAND_MIXER_HOME"
        
        @Min(value = 100, message = "Moc miksera musi być co najmniej 100W")
        @Max(value = 5000, message = "Moc miksera nie może przekraczać 5000W")
        private Integer mixerWattage;
        
        private List<String> availableFlourIds;
        
        private String defaultWaterId;
    }

    /**
     * Aktualizacja warunków środowiskowych użytkownika.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateEnvironmentRequest {
        
        @DecimalMin(value = "10.0", message = "Temperatura pokojowa musi być co najmniej 10°C")
        @DecimalMax(value = "35.0", message = "Temperatura pokojowa nie może przekraczać 35°C")
        private Double typicalRoomTemperature;
        
        @DecimalMin(value = "-5.0", message = "Temperatura lodówki musi być co najmniej -5°C")
        @DecimalMax(value = "10.0", message = "Temperatura lodówki nie może przekraczać 10°C")
        private Double typicalFridgeTemperature;
        
        @Size(max = 100, message = "Nazwa miasta nie może przekraczać 100 znaków")
        private String defaultCity;
        
        @DecimalMin(value = "-90.0", message = "Szerokość geograficzna musi być w zakresie -90 do 90")
        @DecimalMax(value = "90.0", message = "Szerokość geograficzna musi być w zakresie -90 do 90")
        private Double defaultLatitude;
        
        @DecimalMin(value = "-180.0", message = "Długość geograficzna musi być w zakresie -180 do 180")
        @DecimalMax(value = "180.0", message = "Długość geograficzna musi być w zakresie -180 do 180")
        private Double defaultLongitude;
    }

    /**
     * Aktualizacja ustawień powiadomień.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateNotificationsRequest {
        
        private Boolean emailNotifications;
        
        private Boolean smsNotifications;
        
        private Boolean pushNotifications;
        
        @Min(value = 5, message = "Przypomnienie SMS musi być co najmniej 5 minut przed")
        @Max(value = 120, message = "Przypomnienie SMS nie może być więcej niż 120 minut przed")
        private Integer smsReminderMinutesBefore;
    }

    /**
     * Aktualizacja numeru telefonu.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePhoneRequest {
        
        @NotBlank(message = "Numer telefonu jest wymagany")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Nieprawidłowy format numeru telefonu")
        private String phoneNumber;
    }

    /**
     * Weryfikacja numeru telefonu kodem SMS.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyPhoneRequest {
        
        @NotBlank(message = "Kod weryfikacyjny jest wymagany")
        @Size(min = 6, max = 6, message = "Kod weryfikacyjny musi mieć 6 znaków")
        private String verificationCode;
    }

    /**
     * Pełna aktualizacja wszystkich preferencji.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePreferencesRequest {
        
        // Ustawienia ogólne
        @Size(min = 2, max = 5, message = "Kod języka musi mieć od 2 do 5 znaków")
        private String language;
        
        @Pattern(regexp = "^(light|dark)$", message = "Motyw musi być 'light' lub 'dark'")
        private String theme;
        
        private String temperatureUnit; // CELSIUS, FAHRENHEIT
        
        private String weightUnit; // GRAMS, OUNCES
        
        private String defaultPizzaStyle; // np. "NEAPOLITAN"
        
        // Powiadomienia
        private Boolean emailNotifications;
        private Boolean smsNotifications;
        private Boolean pushNotifications;
        private Integer smsReminderMinutesBefore;
        
        // Domyślny sprzęt
        private String defaultOvenType;
        private String defaultMixerType;
        private Integer mixerWattage;
        private List<String> availableFlourIds;
        private String defaultWaterId;
        
        // Warunki środowiskowe
        private Double typicalRoomTemperature;
        private Double typicalFridgeTemperature;
        private String defaultCity;
        private Double defaultLatitude;
        private Double defaultLongitude;
    }
}
