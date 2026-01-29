package com.pizzamaestro.controller;

import com.pizzamaestro.dto.request.UserRequest;
import com.pizzamaestro.dto.response.AuthResponse;
import com.pizzamaestro.model.MixerType;
import com.pizzamaestro.model.OvenType;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.User;
import com.pizzamaestro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Kontroler zarządzania profilem użytkownika.
 * Obsługuje ustawienia sprzętu, preferencje i profil.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profil użytkownika", description = "Zarządzanie profilem, ustawieniami i sprzętem")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ==================== Profil ====================

    @GetMapping("/profile")
    @Operation(summary = "Pobierz profil użytkownika")
    public ResponseEntity<AuthResponse.UserInfo> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("📋 Pobieranie profilu użytkownika: {}", userDetails.getUsername());
        
        User user = userService.findByEmail(userDetails.getUsername());
        AuthResponse.UserInfo userInfo = userService.toUserInfo(user);
        
        log.debug("✅ Profil użytkownika pobrany pomyślnie");
        return ResponseEntity.ok(userInfo);
    }

    // ==================== Ustawienia sprzętu ====================

    @PutMapping("/equipment")
    @Operation(summary = "Aktualizuj domyślny sprzęt")
    public ResponseEntity<AuthResponse.UserPreferencesInfo> updateEquipment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest.UpdateEquipmentRequest request) {
        
        log.info("🔧 Aktualizacja sprzętu użytkownika: {}", userDetails.getUsername());
        log.debug("   📍 Piec: {}", request.getDefaultOvenType());
        log.debug("   📍 Mikser: {}", request.getDefaultMixerType());
        log.debug("   📍 Moc: {}W", request.getMixerWattage());
        log.debug("   📍 Mąki: {}", request.getAvailableFlourIds());
        
        User user = userService.findByEmail(userDetails.getUsername());
        User.UserPreferences prefs = user.getPreferences();
        
        // Aktualizuj piec
        if (request.getDefaultOvenType() != null) {
            try {
                prefs.setDefaultOvenType(OvenType.valueOf(request.getDefaultOvenType()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Nieznany typ pieca: {}", request.getDefaultOvenType());
            }
        }
        
        // Aktualizuj mikser
        if (request.getDefaultMixerType() != null) {
            try {
                prefs.setDefaultMixerType(MixerType.valueOf(request.getDefaultMixerType()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Nieznany typ miksera: {}", request.getDefaultMixerType());
            }
        }
        
        // Aktualizuj moc miksera
        if (request.getMixerWattage() != null) {
            prefs.setMixerWattage(request.getMixerWattage());
        }
        
        // Aktualizuj dostępne mąki
        if (request.getAvailableFlourIds() != null) {
            prefs.setAvailableFlourIds(new ArrayList<>(request.getAvailableFlourIds()));
        }
        
        // Aktualizuj domyślną wodę
        if (request.getDefaultWaterId() != null) {
            prefs.setDefaultWaterId(request.getDefaultWaterId());
        }
        
        User updatedUser = userService.updatePreferences(user.getId(), prefs);
        
        log.info("✅ Sprzęt zaktualizowany pomyślnie");
        return ResponseEntity.ok(buildPreferencesInfo(updatedUser.getPreferences()));
    }

    @GetMapping("/equipment")
    @Operation(summary = "Pobierz ustawienia sprzętu")
    public ResponseEntity<Map<String, Object>> getEquipment(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("🔧 Pobieranie ustawień sprzętu: {}", userDetails.getUsername());
        
        User user = userService.findByEmail(userDetails.getUsername());
        User.UserPreferences prefs = user.getPreferences();
        
        Map<String, Object> equipment = new HashMap<>();
        equipment.put("defaultOvenType", prefs.getDefaultOvenType() != null ? prefs.getDefaultOvenType().name() : null);
        equipment.put("defaultMixerType", prefs.getDefaultMixerType() != null ? prefs.getDefaultMixerType().name() : null);
        equipment.put("mixerWattage", prefs.getMixerWattage());
        equipment.put("availableFlourIds", prefs.getAvailableFlourIds());
        equipment.put("defaultWaterId", prefs.getDefaultWaterId());
        
        // Dodaj szczegóły pieca jeśli ustawiony
        if (prefs.getDefaultOvenType() != null) {
            OvenType oven = prefs.getDefaultOvenType();
            Map<String, Object> ovenDetails = new HashMap<>();
            ovenDetails.put("displayName", oven.getDisplayName());
            ovenDetails.put("description", oven.getDescription());
            ovenDetails.put("minTemperature", oven.getMinTemperature());
            ovenDetails.put("maxTemperature", oven.getMaxTemperature());
            ovenDetails.put("hasSeparateTopBottom", oven.isHasSeparateTopBottom());
            ovenDetails.put("recommendedTemperature", oven.getRecommendedTemperature());
            if (oven.isHasSeparateTopBottom()) {
                ovenDetails.put("recommendedTopTemperature", oven.getRecommendedTopTemperature());
                ovenDetails.put("recommendedBottomTemperature", oven.getRecommendedBottomTemperature());
            }
            equipment.put("ovenDetails", ovenDetails);
        }
        
        // Dodaj szczegóły miksera jeśli ustawiony
        if (prefs.getDefaultMixerType() != null) {
            MixerType mixer = prefs.getDefaultMixerType();
            Map<String, Object> mixerDetails = new HashMap<>();
            mixerDetails.put("displayName", mixer.getDisplayName());
            mixerDetails.put("description", mixer.getDescription());
            mixerDetails.put("frictionFactor", mixer.getFrictionFactor());
            mixerDetails.put("typicalMixingTime", mixer.getTypicalMixingTime());
            mixerDetails.put("maxRecommendedHydration", mixer.getMaxRecommendedHydration());
            equipment.put("mixerDetails", mixerDetails);
        }
        
        return ResponseEntity.ok(equipment);
    }

    // ==================== Warunki środowiskowe ====================

    @PutMapping("/environment")
    @Operation(summary = "Aktualizuj warunki środowiskowe")
    public ResponseEntity<AuthResponse.UserPreferencesInfo> updateEnvironment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest.UpdateEnvironmentRequest request) {
        
        log.info("🌡️ Aktualizacja warunków środowiskowych: {}", userDetails.getUsername());
        log.debug("   📍 Temp. pokojowa: {}°C", request.getTypicalRoomTemperature());
        log.debug("   📍 Temp. lodówki: {}°C", request.getTypicalFridgeTemperature());
        log.debug("   📍 Miasto: {}", request.getDefaultCity());
        
        User user = userService.findByEmail(userDetails.getUsername());
        User.UserPreferences prefs = user.getPreferences();
        
        if (request.getTypicalRoomTemperature() != null) {
            prefs.setTypicalRoomTemperature(request.getTypicalRoomTemperature());
        }
        
        if (request.getTypicalFridgeTemperature() != null) {
            prefs.setTypicalFridgeTemperature(request.getTypicalFridgeTemperature());
        }
        
        if (request.getDefaultCity() != null) {
            prefs.setDefaultCity(request.getDefaultCity());
        }
        
        if (request.getDefaultLatitude() != null) {
            prefs.setDefaultLatitude(request.getDefaultLatitude());
        }
        
        if (request.getDefaultLongitude() != null) {
            prefs.setDefaultLongitude(request.getDefaultLongitude());
        }
        
        User updatedUser = userService.updatePreferences(user.getId(), prefs);
        
        log.info("✅ Warunki środowiskowe zaktualizowane pomyślnie");
        return ResponseEntity.ok(buildPreferencesInfo(updatedUser.getPreferences()));
    }

    // ==================== Powiadomienia ====================

    @PutMapping("/notifications")
    @Operation(summary = "Aktualizuj ustawienia powiadomień")
    public ResponseEntity<AuthResponse.UserPreferencesInfo> updateNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest.UpdateNotificationsRequest request) {
        
        log.info("🔔 Aktualizacja ustawień powiadomień: {}", userDetails.getUsername());
        
        User user = userService.findByEmail(userDetails.getUsername());
        User.UserPreferences prefs = user.getPreferences();
        
        if (request.getEmailNotifications() != null) {
            prefs.setEmailNotifications(request.getEmailNotifications());
        }
        
        if (request.getSmsNotifications() != null) {
            prefs.setSmsNotifications(request.getSmsNotifications());
        }
        
        if (request.getPushNotifications() != null) {
            prefs.setPushNotifications(request.getPushNotifications());
        }
        
        if (request.getSmsReminderMinutesBefore() != null) {
            prefs.setSmsReminderMinutesBefore(request.getSmsReminderMinutesBefore());
        }
        
        User updatedUser = userService.updatePreferences(user.getId(), prefs);
        
        log.info("✅ Ustawienia powiadomień zaktualizowane");
        return ResponseEntity.ok(buildPreferencesInfo(updatedUser.getPreferences()));
    }

    // ==================== Pełne preferencje ====================

    @PutMapping("/preferences")
    @Operation(summary = "Aktualizuj wszystkie preferencje")
    public ResponseEntity<AuthResponse.UserPreferencesInfo> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest.UpdatePreferencesRequest request) {
        
        log.info("⚙️ Aktualizacja preferencji użytkownika: {}", userDetails.getUsername());
        
        User user = userService.findByEmail(userDetails.getUsername());
        User.UserPreferences prefs = user.getPreferences();
        
        // Ustawienia ogólne
        if (request.getLanguage() != null) {
            prefs.setLanguage(request.getLanguage());
        }
        if (request.getTheme() != null) {
            prefs.setTheme(request.getTheme());
        }
        if (request.getTemperatureUnit() != null) {
            try {
                prefs.setTemperatureUnit(User.TemperatureUnit.valueOf(request.getTemperatureUnit()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Nieznana jednostka temperatury: {}", request.getTemperatureUnit());
            }
        }
        if (request.getWeightUnit() != null) {
            try {
                prefs.setWeightUnit(User.WeightUnit.valueOf(request.getWeightUnit()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Nieznana jednostka wagi: {}", request.getWeightUnit());
            }
        }
        if (request.getDefaultPizzaStyle() != null) {
            try {
                prefs.setDefaultPizzaStyle(PizzaStyle.valueOf(request.getDefaultPizzaStyle()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Nieznany styl pizzy: {}", request.getDefaultPizzaStyle());
            }
        }
        
        // Powiadomienia
        if (request.getEmailNotifications() != null) {
            prefs.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getSmsNotifications() != null) {
            prefs.setSmsNotifications(request.getSmsNotifications());
        }
        if (request.getPushNotifications() != null) {
            prefs.setPushNotifications(request.getPushNotifications());
        }
        if (request.getSmsReminderMinutesBefore() != null) {
            prefs.setSmsReminderMinutesBefore(request.getSmsReminderMinutesBefore());
        }
        
        // Sprzęt
        if (request.getDefaultOvenType() != null) {
            try {
                prefs.setDefaultOvenType(OvenType.valueOf(request.getDefaultOvenType()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Nieznany typ pieca: {}", request.getDefaultOvenType());
            }
        }
        if (request.getDefaultMixerType() != null) {
            try {
                prefs.setDefaultMixerType(MixerType.valueOf(request.getDefaultMixerType()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Nieznany typ miksera: {}", request.getDefaultMixerType());
            }
        }
        if (request.getMixerWattage() != null) {
            prefs.setMixerWattage(request.getMixerWattage());
        }
        if (request.getAvailableFlourIds() != null) {
            prefs.setAvailableFlourIds(new ArrayList<>(request.getAvailableFlourIds()));
        }
        if (request.getDefaultWaterId() != null) {
            prefs.setDefaultWaterId(request.getDefaultWaterId());
        }
        
        // Warunki środowiskowe
        if (request.getTypicalRoomTemperature() != null) {
            prefs.setTypicalRoomTemperature(request.getTypicalRoomTemperature());
        }
        if (request.getTypicalFridgeTemperature() != null) {
            prefs.setTypicalFridgeTemperature(request.getTypicalFridgeTemperature());
        }
        if (request.getDefaultCity() != null) {
            prefs.setDefaultCity(request.getDefaultCity());
        }
        if (request.getDefaultLatitude() != null) {
            prefs.setDefaultLatitude(request.getDefaultLatitude());
        }
        if (request.getDefaultLongitude() != null) {
            prefs.setDefaultLongitude(request.getDefaultLongitude());
        }
        
        User updatedUser = userService.updatePreferences(user.getId(), prefs);
        
        log.info("✅ Preferencje zaktualizowane pomyślnie");
        return ResponseEntity.ok(buildPreferencesInfo(updatedUser.getPreferences()));
    }

    // ==================== Telefon ====================

    @PutMapping("/phone")
    @Operation(summary = "Aktualizuj numer telefonu (wymaga weryfikacji)")
    public ResponseEntity<Map<String, Object>> updatePhone(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest.UpdatePhoneRequest request) {
        
        log.info("📱 Aktualizacja numeru telefonu: {}", userDetails.getUsername());
        
        User user = userService.findByEmail(userDetails.getUsername());
        
        // Zapisz numer ale oznacz jako niezweryfikowany
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPhoneVerified(false);
        
        // TODO: Wysłać kod weryfikacyjny SMS przez TwilioService
        
        userService.updatePreferences(user.getId(), user.getPreferences());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Numer telefonu zapisany. Kod weryfikacyjny został wysłany.");
        response.put("phoneNumber", request.getPhoneNumber());
        response.put("verified", false);
        
        return ResponseEntity.ok(response);
    }

    // ==================== Metody pomocnicze ====================

    private AuthResponse.UserPreferencesInfo buildPreferencesInfo(User.UserPreferences prefs) {
        return AuthResponse.UserPreferencesInfo.builder()
                .language(prefs.getLanguage())
                .theme(prefs.getTheme())
                .temperatureUnit(prefs.getTemperatureUnit().name())
                .weightUnit(prefs.getWeightUnit().name())
                .emailNotifications(prefs.isEmailNotifications())
                .smsNotifications(prefs.isSmsNotifications())
                .pushNotifications(prefs.isPushNotifications())
                .smsReminderMinutesBefore(prefs.getSmsReminderMinutesBefore())
                .defaultPizzaStyle(prefs.getDefaultPizzaStyle().name())
                .defaultOvenType(prefs.getDefaultOvenType() != null ? prefs.getDefaultOvenType().name() : null)
                .defaultMixerType(prefs.getDefaultMixerType() != null ? prefs.getDefaultMixerType().name() : null)
                .mixerWattage(prefs.getMixerWattage())
                .availableFlourIds(prefs.getAvailableFlourIds())
                .defaultWaterId(prefs.getDefaultWaterId())
                .typicalRoomTemperature(prefs.getTypicalRoomTemperature())
                .typicalFridgeTemperature(prefs.getTypicalFridgeTemperature())
                .defaultCity(prefs.getDefaultCity())
                .defaultLatitude(prefs.getDefaultLatitude())
                .defaultLongitude(prefs.getDefaultLongitude())
                .build();
    }
}
