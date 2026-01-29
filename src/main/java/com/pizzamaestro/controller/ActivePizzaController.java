package com.pizzamaestro.controller;

import com.pizzamaestro.model.ActivePizza;
import com.pizzamaestro.model.ActivePizza.*;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.service.ActivePizzaService;
import com.pizzamaestro.service.CalendarExportService;
import com.pizzamaestro.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kontroler zarządzania aktywną pizzą.
 * Obsługuje tworzenie, śledzenie postępu i przesuwanie harmonogramu.
 */
@RestController
@RequestMapping("/api/active-pizza")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Aktywna Pizza", description = "Zarządzanie pizzą w trakcie przygotowania")
@SecurityRequirement(name = "bearerAuth")
public class ActivePizzaController {

    private final ActivePizzaService activePizzaService;
    private final RecipeService recipeService;
    private final CalendarExportService calendarExportService;

    // ==================== Pobieranie ====================

    @GetMapping("/current")
    @Operation(summary = "Pobierz aktualną aktywną pizzę użytkownika")
    public ResponseEntity<ActivePizzaResponse> getCurrentActivePizza(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("🍕 Pobieranie aktywnej pizzy dla: {}", userDetails.getUsername());
        
        Optional<ActivePizza> activePizza = activePizzaService.getActiveByUserId(getUserId(userDetails));
        
        if (activePizza.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(toResponse(activePizza.get()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz aktywną pizzę po ID")
    public ResponseEntity<ActivePizzaResponse> getById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ActivePizza pizza = activePizzaService.getById(id);
        return ResponseEntity.ok(toResponse(pizza));
    }

    @GetMapping("/history")
    @Operation(summary = "Pobierz historię aktywnych pizz")
    public ResponseEntity<List<ActivePizzaResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        List<ActivePizza> history = activePizzaService.getHistoryByUserId(getUserId(userDetails));
        List<ActivePizzaResponse> responses = history.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    // ==================== Tworzenie ====================

    @PostMapping("/from-recipe/{recipeId}")
    @Operation(summary = "Utwórz aktywną pizzę z zapisanego przepisu")
    public ResponseEntity<ActivePizzaResponse> createFromRecipe(
            @PathVariable String recipeId,
            @Valid @RequestBody CreateFromRecipeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("🍕 Tworzenie aktywnej pizzy z przepisu: {} dla: {}", recipeId, userDetails.getUsername());
        
        Recipe recipe = recipeService.findById(recipeId);
        
        ActivePizza activePizza = activePizzaService.createFromRecipe(
                getUserId(userDetails),
                recipe,
                request.getTargetBakeTime()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(activePizza));
    }

    @PostMapping("/new")
    @Operation(summary = "Utwórz nową aktywną pizzę bez zapisanego przepisu")
    public ResponseEntity<ActivePizzaResponse> createNew(
            @Valid @RequestBody CreateNewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("🍕 Tworzenie nowej aktywnej pizzy: {} dla: {}", request.getName(), userDetails.getUsername());
        
        ActivePizza activePizza = activePizzaService.createNew(
                getUserId(userDetails),
                request.getName(),
                request.getPizzaStyle(),
                request.getNumberOfPizzas(),
                request.getTargetBakeTime(),
                request.getFermentationMethod(),
                request.getFermentationHours()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(activePizza));
    }

    // ==================== Zarządzanie statusem ====================

    @PostMapping("/{id}/start")
    @Operation(summary = "Rozpocznij aktywną pizzę")
    public ResponseEntity<ActivePizzaResponse> start(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ActivePizza pizza = activePizzaService.start(id, getUserId(userDetails));
        return ResponseEntity.ok(toResponse(pizza));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Wstrzymaj aktywną pizzę")
    public ResponseEntity<ActivePizzaResponse> pause(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ActivePizza pizza = activePizzaService.pause(id, getUserId(userDetails));
        return ResponseEntity.ok(toResponse(pizza));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Wznów wstrzymaną pizzę")
    public ResponseEntity<ActivePizzaResponse> resume(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ActivePizza pizza = activePizzaService.resume(id, getUserId(userDetails));
        return ResponseEntity.ok(toResponse(pizza));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Anuluj aktywną pizzę")
    public ResponseEntity<ActivePizzaResponse> cancel(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ActivePizza pizza = activePizzaService.cancel(id, getUserId(userDetails));
        return ResponseEntity.ok(toResponse(pizza));
    }

    // ==================== Zarządzanie krokami ====================

    @PostMapping("/{id}/steps/{stepNumber}/complete")
    @Operation(summary = "Oznacz krok jako ukończony")
    public ResponseEntity<ActivePizzaResponse> completeStep(
            @PathVariable String id,
            @PathVariable int stepNumber,
            @RequestBody(required = false) CompleteStepRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        StepStatus status = request != null && request.getStatus() != null 
                ? request.getStatus() 
                : null;
        
        ActivePizza pizza = activePizzaService.completeStep(id, stepNumber, getUserId(userDetails), status);
        return ResponseEntity.ok(toResponse(pizza));
    }

    @PostMapping("/{id}/steps/{stepNumber}/skip")
    @Operation(summary = "Pomiń krok")
    public ResponseEntity<ActivePizzaResponse> skipStep(
            @PathVariable String id,
            @PathVariable int stepNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ActivePizza pizza = activePizzaService.skipStep(id, stepNumber, getUserId(userDetails));
        return ResponseEntity.ok(toResponse(pizza));
    }

    // ==================== Przesuwanie harmonogramu ====================

    @PostMapping("/{id}/reschedule")
    @Operation(summary = "Przesuń harmonogram do nowej godziny wypieku")
    public ResponseEntity<ActivePizzaResponse> reschedule(
            @PathVariable String id,
            @Valid @RequestBody RescheduleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("📅 Przesuwanie harmonogramu pizzy {} na {}", id, request.getNewTargetBakeTime());
        
        ActivePizza pizza = activePizzaService.reschedule(id, getUserId(userDetails), request.getNewTargetBakeTime());
        return ResponseEntity.ok(toResponse(pizza));
    }

    @PostMapping("/{id}/reschedule-by-minutes")
    @Operation(summary = "Przesuń harmonogram o określoną liczbę minut")
    public ResponseEntity<ActivePizzaResponse> rescheduleByMinutes(
            @PathVariable String id,
            @RequestParam int minutes,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("📅 Przesuwanie harmonogramu pizzy {} o {} minut", id, minutes);
        
        ActivePizza pizza = activePizzaService.rescheduleByMinutes(id, getUserId(userDetails), minutes);
        return ResponseEntity.ok(toResponse(pizza));
    }

    // ==================== Powiadomienia ====================

    @PostMapping("/{id}/notifications/enable")
    @Operation(summary = "Włącz powiadomienia SMS")
    public ResponseEntity<ActivePizzaResponse> enableNotifications(
            @PathVariable String id,
            @Valid @RequestBody EnableNotificationsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ActivePizza pizza = activePizzaService.enableSmsNotifications(
                id, 
                getUserId(userDetails), 
                request.getPhoneNumber(),
                request.getReminderMinutesBefore() != null ? request.getReminderMinutesBefore() : 15
        );
        return ResponseEntity.ok(toResponse(pizza));
    }

    @PostMapping("/{id}/notifications/disable")
    @Operation(summary = "Wyłącz powiadomienia SMS")
    public ResponseEntity<ActivePizzaResponse> disableNotifications(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ActivePizza pizza = activePizzaService.disableSmsNotifications(id, getUserId(userDetails));
        return ResponseEntity.ok(toResponse(pizza));
    }

    // ==================== Export ====================

    @GetMapping(value = "/{id}/calendar.ics", produces = "text/calendar")
    @Operation(summary = "Eksportuj harmonogram do pliku iCalendar")
    public ResponseEntity<String> exportToCalendar(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("📅 Eksport harmonogramu do iCal dla pizzy: {}", id);
        
        ActivePizza pizza = activePizzaService.getById(id);
        String icalContent = calendarExportService.generateICalForActivePizza(pizza);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDispositionFormData("attachment", "pizza-" + id + ".ics");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(icalContent);
    }

    // ==================== DTO ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateFromRecipeRequest {
        @NotNull(message = "Czas wypieku jest wymagany")
        private LocalDateTime targetBakeTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateNewRequest {
        @NotBlank(message = "Nazwa jest wymagana")
        private String name;
        
        @NotNull(message = "Styl pizzy jest wymagany")
        private PizzaStyle pizzaStyle;
        
        @Min(value = 1, message = "Liczba pizz musi być co najmniej 1")
        private int numberOfPizzas;
        
        @NotNull(message = "Czas wypieku jest wymagany")
        private LocalDateTime targetBakeTime;
        
        private String fermentationMethod;
        
        @Min(value = 2, message = "Fermentacja musi trwać co najmniej 2 godziny")
        private int fermentationHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RescheduleRequest {
        @NotNull(message = "Nowy czas wypieku jest wymagany")
        private LocalDateTime newTargetBakeTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteStepRequest {
        private StepStatus status;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnableNotificationsRequest {
        @NotBlank(message = "Numer telefonu jest wymagany")
        private String phoneNumber;
        private Integer reminderMinutesBefore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivePizzaResponse {
        private String id;
        private String userId;
        private String recipeId;
        private String name;
        private String pizzaStyle;
        private String pizzaStyleName;
        private Integer numberOfPizzas;
        private LocalDateTime targetBakeTime;
        private LocalDateTime adjustedBakeTime;
        private List<ScheduledStepResponse> steps;
        private String status;
        private String statusName;
        private String notes;
        private boolean smsNotificationsEnabled;
        private String notificationPhone;
        private Integer reminderMinutesBefore;
        private int completionPercentage;
        private Long minutesToNextStep;
        private ScheduledStepResponse nextStep;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduledStepResponse {
        private int stepNumber;
        private String type;
        private String typeName;
        private String title;
        private String description;
        private LocalDateTime scheduledTime;
        private LocalDateTime actualTime;
        private Integer durationMinutes;
        private Double temperature;
        private String status;
        private String statusName;
        private boolean notificationSent;
        private String note;
        private String icon;
    }

    // ==================== Metody pomocnicze ====================

    private String getUserId(UserDetails userDetails) {
        // W rzeczywistej implementacji pobierz ID z bazy danych
        // Na razie używamy email jako identyfikatora
        return userDetails.getUsername();
    }

    private ActivePizzaResponse toResponse(ActivePizza pizza) {
        ScheduledStep nextStep = pizza.getNextPendingStep();
        
        return ActivePizzaResponse.builder()
                .id(pizza.getId())
                .userId(pizza.getUserId())
                .recipeId(pizza.getRecipeId())
                .name(pizza.getName())
                .pizzaStyle(pizza.getPizzaStyle() != null ? pizza.getPizzaStyle().name() : null)
                .pizzaStyleName(pizza.getPizzaStyle() != null ? pizza.getPizzaStyle().getDisplayName() : null)
                .numberOfPizzas(pizza.getNumberOfPizzas())
                .targetBakeTime(pizza.getTargetBakeTime())
                .adjustedBakeTime(pizza.getAdjustedBakeTime())
                .steps(pizza.getSteps().stream().map(this::toStepResponse).collect(Collectors.toList()))
                .status(pizza.getStatus().name())
                .statusName(pizza.getStatus().getDisplayName())
                .notes(pizza.getNotes())
                .smsNotificationsEnabled(pizza.isSmsNotificationsEnabled())
                .notificationPhone(pizza.getNotificationPhone())
                .reminderMinutesBefore(pizza.getReminderMinutesBefore())
                .completionPercentage(pizza.getCompletionPercentage())
                .minutesToNextStep(pizza.getMinutesToNextStep())
                .nextStep(nextStep != null ? toStepResponse(nextStep) : null)
                .createdAt(pizza.getCreatedAt())
                .lastUpdatedAt(pizza.getLastUpdatedAt())
                .build();
    }

    private ScheduledStepResponse toStepResponse(ScheduledStep step) {
        return ScheduledStepResponse.builder()
                .stepNumber(step.getStepNumber())
                .type(step.getType().name())
                .typeName(step.getType().getDisplayName())
                .title(step.getTitle())
                .description(step.getDescription())
                .scheduledTime(step.getScheduledTime())
                .actualTime(step.getActualTime())
                .durationMinutes(step.getDurationMinutes())
                .temperature(step.getTemperature())
                .status(step.getStatus().name())
                .statusName(step.getStatus().getDisplayName())
                .notificationSent(step.isNotificationSent())
                .note(step.getNote())
                .icon(step.getIcon())
                .build();
    }
}
