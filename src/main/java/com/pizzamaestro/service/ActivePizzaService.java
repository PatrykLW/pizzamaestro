package com.pizzamaestro.service;

import com.pizzamaestro.exception.ResourceNotFoundException;
import com.pizzamaestro.model.ActivePizza;
import com.pizzamaestro.model.ActivePizza.*;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.repository.ActivePizzaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serwis zarządzania aktywną pizzą.
 * Obsługuje tworzenie, śledzenie postępu, przesuwanie harmonogramu i powiadomienia.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivePizzaService {

    private final ActivePizzaRepository activePizzaRepository;

    // ==================== Tworzenie ====================

    /**
     * Tworzy nową aktywną pizzę na podstawie przepisu.
     */
    @Transactional
    public ActivePizza createFromRecipe(String userId, Recipe recipe, LocalDateTime targetBakeTime) {
        log.info("🍕 Tworzenie aktywnej pizzy z przepisu: {} dla użytkownika: {}", recipe.getName(), userId);

        // Sprawdź czy użytkownik nie ma już aktywnej pizzy
        Optional<ActivePizza> existing = activePizzaRepository.findActiveByUserId(userId);
        if (existing.isPresent()) {
            log.warn("⚠️ Użytkownik {} ma już aktywną pizzę: {}", userId, existing.get().getId());
            throw new IllegalStateException("Masz już aktywną pizzę w trakcie przygotowania. Zakończ ją przed rozpoczęciem nowej.");
        }

        ActivePizza activePizza = ActivePizza.builder()
                .userId(userId)
                .recipeId(recipe.getId())
                .name(recipe.getName())
                .pizzaStyle(recipe.getPizzaStyle())
                .numberOfPizzas(recipe.getNumberOfPizzas())
                .targetBakeTime(targetBakeTime)
                .adjustedBakeTime(targetBakeTime)
                .status(ActivePizzaStatus.PLANNING)
                .build();

        // Wygeneruj harmonogram kroków
        List<ScheduledStep> steps = generateScheduleFromRecipe(recipe, targetBakeTime);
        activePizza.setSteps(steps);

        ActivePizza saved = activePizzaRepository.save(activePizza);
        log.info("✅ Utworzono aktywną pizzę: {} z {} krokami", saved.getId(), steps.size());

        return saved;
    }

    /**
     * Tworzy aktywną pizzę bez zapisanego przepisu.
     */
    @Transactional
    public ActivePizza createNew(
            String userId,
            String name,
            PizzaStyle pizzaStyle,
            int numberOfPizzas,
            LocalDateTime targetBakeTime,
            String fermentationMethod,
            int fermentationHours
    ) {
        log.info("🍕 Tworzenie nowej aktywnej pizzy: {} dla użytkownika: {}", name, userId);

        // Sprawdź czy użytkownik nie ma już aktywnej pizzy
        Optional<ActivePizza> existing = activePizzaRepository.findActiveByUserId(userId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Masz już aktywną pizzę w trakcie przygotowania.");
        }

        ActivePizza activePizza = ActivePizza.builder()
                .userId(userId)
                .name(name)
                .pizzaStyle(pizzaStyle)
                .numberOfPizzas(numberOfPizzas)
                .targetBakeTime(targetBakeTime)
                .adjustedBakeTime(targetBakeTime)
                .status(ActivePizzaStatus.PLANNING)
                .build();

        // Wygeneruj podstawowe kroki na podstawie metody fermentacji
        List<ScheduledStep> steps = generateBasicSchedule(fermentationMethod, fermentationHours, targetBakeTime);
        activePizza.setSteps(steps);

        return activePizzaRepository.save(activePizza);
    }

    // ==================== Pobieranie ====================

    /**
     * Pobiera aktywną pizzę użytkownika.
     */
    public Optional<ActivePizza> getActiveByUserId(String userId) {
        return activePizzaRepository.findActiveByUserId(userId);
    }

    /**
     * Pobiera aktywną pizzę po ID.
     */
    public ActivePizza getById(String id) {
        return activePizzaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aktywna pizza nie znaleziona"));
    }

    /**
     * Pobiera historię pizz użytkownika.
     */
    public List<ActivePizza> getHistoryByUserId(String userId) {
        return activePizzaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ==================== Zarządzanie krokami ====================

    /**
     * Rozpoczyna aktywną pizzę (zmiana statusu na IN_PROGRESS).
     */
    @Transactional
    public ActivePizza start(String activePizzaId, String userId) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        if (pizza.getStatus() != ActivePizzaStatus.PLANNING) {
            throw new IllegalStateException("Można rozpocząć tylko pizzę w statusie PLANNING");
        }

        pizza.setStatus(ActivePizzaStatus.IN_PROGRESS);

        // Oznacz pierwszy krok jako w trakcie
        if (!pizza.getSteps().isEmpty()) {
            pizza.getSteps().get(0).setStatus(StepStatus.IN_PROGRESS);
        }

        log.info("▶️ Rozpoczęto aktywną pizzę: {}", activePizzaId);
        return activePizzaRepository.save(pizza);
    }

    /**
     * Oznacza krok jako ukończony.
     */
    @Transactional
    public ActivePizza completeStep(String activePizzaId, int stepNumber, String userId, StepStatus completionStatus) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        ScheduledStep step = pizza.getSteps().stream()
                .filter(s -> s.getStepNumber() == stepNumber)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Krok nie znaleziony"));

        // Określ status na podstawie czasu
        LocalDateTime now = LocalDateTime.now();
        if (completionStatus == null) {
            if (step.getScheduledTime() == null) {
                completionStatus = StepStatus.COMPLETED;
            } else if (now.isBefore(step.getScheduledTime().minusMinutes(5))) {
                completionStatus = StepStatus.COMPLETED_EARLY;
            } else if (now.isAfter(step.getScheduledTime().plusMinutes(15))) {
                completionStatus = StepStatus.COMPLETED_LATE;
            } else {
                completionStatus = StepStatus.COMPLETED;
            }
        }

        step.setStatus(completionStatus);
        step.setActualTime(now);

        log.info("✅ Ukończono krok {} w aktywnej pizzy {}: {}", stepNumber, activePizzaId, completionStatus);

        // Znajdź i oznacz następny krok jako w trakcie
        Optional<ScheduledStep> nextStep = pizza.getSteps().stream()
                .filter(s -> s.getStatus() == StepStatus.PENDING)
                .findFirst();

        nextStep.ifPresent(s -> s.setStatus(StepStatus.IN_PROGRESS));

        // Sprawdź czy wszystkie kroki ukończone
        if (pizza.isCompleted()) {
            pizza.setStatus(ActivePizzaStatus.COMPLETED);
            log.info("🎉 Aktywna pizza {} zakończona!", activePizzaId);
        }

        return activePizzaRepository.save(pizza);
    }

    /**
     * Pomija krok.
     */
    @Transactional
    public ActivePizza skipStep(String activePizzaId, int stepNumber, String userId) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        ScheduledStep step = pizza.getSteps().stream()
                .filter(s -> s.getStepNumber() == stepNumber)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Krok nie znaleziony"));

        step.setStatus(StepStatus.SKIPPED);
        step.setActualTime(LocalDateTime.now());

        log.info("⏭️ Pominięto krok {} w aktywnej pizzy {}", stepNumber, activePizzaId);

        return activePizzaRepository.save(pizza);
    }

    // ==================== Przesuwanie harmonogramu ====================

    /**
     * Przesuwa czas wypieku i przelicza harmonogram.
     */
    @Transactional
    public ActivePizza reschedule(String activePizzaId, String userId, LocalDateTime newTargetBakeTime) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        LocalDateTime oldTime = pizza.getAdjustedBakeTime();
        Duration shift = Duration.between(oldTime, newTargetBakeTime);

        log.info("📅 Przesuwanie harmonogramu pizzy {} o {}", activePizzaId, shift);

        pizza.setAdjustedBakeTime(newTargetBakeTime);

        // Przesuń wszystkie oczekujące kroki
        for (ScheduledStep step : pizza.getSteps()) {
            if (step.getStatus() == StepStatus.PENDING && step.getScheduledTime() != null) {
                step.setScheduledTime(step.getScheduledTime().plus(shift));
                step.setNotificationSent(false); // Zresetuj status powiadomienia
            }
        }

        log.info("✅ Harmonogram przesunięty. Nowy czas wypieku: {}", newTargetBakeTime);
        return activePizzaRepository.save(pizza);
    }

    /**
     * Przesuwa harmonogram o określoną liczbę minut.
     */
    @Transactional
    public ActivePizza rescheduleByMinutes(String activePizzaId, String userId, int minutes) {
        ActivePizza pizza = getById(activePizzaId);
        LocalDateTime newTime = pizza.getAdjustedBakeTime().plusMinutes(minutes);
        return reschedule(activePizzaId, userId, newTime);
    }

    // ==================== Zarządzanie statusem ====================

    /**
     * Wstrzymuje aktywną pizzę.
     */
    @Transactional
    public ActivePizza pause(String activePizzaId, String userId) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        pizza.setStatus(ActivePizzaStatus.PAUSED);
        log.info("⏸️ Wstrzymano aktywną pizzę: {}", activePizzaId);

        return activePizzaRepository.save(pizza);
    }

    /**
     * Wznawia wstrzymaną pizzę.
     */
    @Transactional
    public ActivePizza resume(String activePizzaId, String userId) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        if (pizza.getStatus() != ActivePizzaStatus.PAUSED) {
            throw new IllegalStateException("Można wznowić tylko wstrzymaną pizzę");
        }

        pizza.setStatus(ActivePizzaStatus.IN_PROGRESS);
        log.info("▶️ Wznowiono aktywną pizzę: {}", activePizzaId);

        return activePizzaRepository.save(pizza);
    }

    /**
     * Anuluje aktywną pizzę.
     */
    @Transactional
    public ActivePizza cancel(String activePizzaId, String userId) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        pizza.setStatus(ActivePizzaStatus.CANCELLED);
        log.info("❌ Anulowano aktywną pizzę: {}", activePizzaId);

        return activePizzaRepository.save(pizza);
    }

    // ==================== Powiadomienia ====================

    /**
     * Włącza powiadomienia SMS dla aktywnej pizzy.
     */
    @Transactional
    public ActivePizza enableSmsNotifications(String activePizzaId, String userId, String phoneNumber, int reminderMinutesBefore) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        pizza.setSmsNotificationsEnabled(true);
        pizza.setNotificationPhone(phoneNumber);
        pizza.setReminderMinutesBefore(reminderMinutesBefore);

        log.info("📱 Włączono powiadomienia SMS dla pizzy {} na numer {}", activePizzaId, phoneNumber);
        return activePizzaRepository.save(pizza);
    }

    /**
     * Wyłącza powiadomienia SMS.
     */
    @Transactional
    public ActivePizza disableSmsNotifications(String activePizzaId, String userId) {
        ActivePizza pizza = getById(activePizzaId);
        validateOwnership(pizza, userId);

        pizza.setSmsNotificationsEnabled(false);
        log.info("🔇 Wyłączono powiadomienia SMS dla pizzy {}", activePizzaId);

        return activePizzaRepository.save(pizza);
    }

    /**
     * Oznacza krok jako powiadomiony.
     */
    @Transactional
    public void markStepNotified(String activePizzaId, int stepNumber) {
        ActivePizza pizza = getById(activePizzaId);
        
        pizza.getSteps().stream()
                .filter(s -> s.getStepNumber() == stepNumber)
                .findFirst()
                .ifPresent(step -> {
                    step.setNotificationSent(true);
                    step.setNotificationSentAt(LocalDateTime.now());
                });

        activePizzaRepository.save(pizza);
    }

    // ==================== Generowanie harmonogramu ====================

    private List<ScheduledStep> generateScheduleFromRecipe(Recipe recipe, LocalDateTime targetBakeTime) {
        List<ScheduledStep> steps = new ArrayList<>();
        int stepNumber = 1;

        // Oblicz czasy wstecz od wypieku
        LocalDateTime bakeTime = targetBakeTime;
        
        // Krok: Wypiek
        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.BAKE)
                .title("Wypiek pizzy")
                .description("Piecz pizzę w rozgrzanym piecu")
                .scheduledTime(bakeTime)
                .durationMinutes(2)
                .icon(StepType.BAKE.getIcon())
                .build());

        // Krok: Formowanie (15 min przed)
        LocalDateTime shapeTime = bakeTime.minusMinutes(15);
        steps.add(0, ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.SHAPE)
                .title("Formowanie pizzy")
                .description("Rozciągnij ciasto na pizzę")
                .scheduledTime(shapeTime)
                .durationMinutes(10)
                .icon(StepType.SHAPE.getIcon())
                .build());

        // Krok: Rozgrzewanie pieca (45 min przed)
        LocalDateTime preheatTime = bakeTime.minusMinutes(45);
        steps.add(0, ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.PREHEAT_OVEN)
                .title("Rozgrzewanie pieca")
                .description("Rozgrzej piec do temperatury wypieku")
                .scheduledTime(preheatTime)
                .durationMinutes(30)
                .icon(StepType.PREHEAT_OVEN.getIcon())
                .build());

        // Krok: Wyjęcie z lodówki (2h przed dla fermentacji w lodówce)
        if (recipe.getFermentationMethod() != null && 
            recipe.getFermentationMethod().contains("COLD")) {
            LocalDateTime removeTime = bakeTime.minusHours(2);
            steps.add(0, ScheduledStep.builder()
                    .stepNumber(stepNumber++)
                    .type(StepType.REMOVE_FROM_FRIDGE)
                    .title("Wyjęcie ciasta z lodówki")
                    .description("Wyjmij ciasto aby osiągnęło temperaturę pokojową")
                    .scheduledTime(removeTime)
                    .durationMinutes(120)
                    .icon(StepType.REMOVE_FROM_FRIDGE.getIcon())
                    .build());
        }

        // Krok: Kulkowanie (po fermentacji w bloku)
        int fermentationHours = recipe.getTotalFermentationHours() != null 
                ? recipe.getTotalFermentationHours() 
                : 24;
        LocalDateTime ballTime = bakeTime.minusHours(Math.max(4, fermentationHours / 3));
        steps.add(0, ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.DIVIDE_AND_BALL)
                .title("Kulkowanie ciasta")
                .description("Podziel ciasto na kulki o wadze " + recipe.getBallWeight() + "g")
                .scheduledTime(ballTime)
                .durationMinutes(15)
                .icon(StepType.DIVIDE_AND_BALL.getIcon())
                .build());

        // Krok: Fermentacja w bloku
        LocalDateTime bulkStart = bakeTime.minusHours(fermentationHours);
        steps.add(0, ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.BULK_FERMENTATION)
                .title("Fermentacja w bloku")
                .description("Pozostaw ciasto do fermentacji")
                .scheduledTime(bulkStart)
                .durationMinutes(fermentationHours * 60 / 2)
                .icon(StepType.BULK_FERMENTATION.getIcon())
                .build());

        // Krok: Wyrabianie
        LocalDateTime kneadTime = bulkStart.minusMinutes(15);
        steps.add(0, ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.KNEAD)
                .title("Wyrabianie ciasta")
                .description("Wyrabiaj ciasto do uzyskania gładkiej struktury")
                .scheduledTime(kneadTime)
                .durationMinutes(10)
                .icon(StepType.KNEAD.getIcon())
                .build());

        // Krok: Mieszanie składników
        LocalDateTime mixTime = kneadTime.minusMinutes(10);
        steps.add(0, ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.MIX_DOUGH)
                .title("Mieszanie składników")
                .description("Połącz mąkę, wodę, drożdże i sól")
                .scheduledTime(mixTime)
                .durationMinutes(5)
                .icon(StepType.MIX_DOUGH.getIcon())
                .build());

        // Krok: Przygotowanie składników
        LocalDateTime prepTime = mixTime.minusMinutes(15);
        steps.add(0, ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.PREPARE_INGREDIENTS)
                .title("Przygotowanie składników")
                .description("Odważ wszystkie składniki")
                .scheduledTime(prepTime)
                .durationMinutes(10)
                .icon(StepType.PREPARE_INGREDIENTS.getIcon())
                .build());

        // Ponumeruj kroki od początku
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setStepNumber(i + 1);
        }

        return steps;
    }

    private List<ScheduledStep> generateBasicSchedule(String fermentationMethod, int fermentationHours, LocalDateTime targetBakeTime) {
        List<ScheduledStep> steps = new ArrayList<>();
        int stepNumber = 1;

        LocalDateTime bakeTime = targetBakeTime;

        // Podstawowe kroki
        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.PREPARE_INGREDIENTS)
                .title("Przygotowanie składników")
                .scheduledTime(bakeTime.minusHours(fermentationHours + 1))
                .durationMinutes(10)
                .icon(StepType.PREPARE_INGREDIENTS.getIcon())
                .build());

        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.MIX_DOUGH)
                .title("Mieszanie ciasta")
                .scheduledTime(bakeTime.minusHours(fermentationHours))
                .durationMinutes(5)
                .icon(StepType.MIX_DOUGH.getIcon())
                .build());

        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.KNEAD)
                .title("Wyrabianie")
                .scheduledTime(bakeTime.minusHours(fermentationHours).plusMinutes(5))
                .durationMinutes(10)
                .icon(StepType.KNEAD.getIcon())
                .build());

        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.BULK_FERMENTATION)
                .title("Fermentacja")
                .scheduledTime(bakeTime.minusHours(fermentationHours).plusMinutes(15))
                .durationMinutes(fermentationHours * 60)
                .icon(StepType.BULK_FERMENTATION.getIcon())
                .build());

        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.DIVIDE_AND_BALL)
                .title("Kulkowanie")
                .scheduledTime(bakeTime.minusHours(4))
                .durationMinutes(15)
                .icon(StepType.DIVIDE_AND_BALL.getIcon())
                .build());

        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.PREHEAT_OVEN)
                .title("Rozgrzewanie pieca")
                .scheduledTime(bakeTime.minusMinutes(45))
                .durationMinutes(30)
                .icon(StepType.PREHEAT_OVEN.getIcon())
                .build());

        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.SHAPE)
                .title("Formowanie")
                .scheduledTime(bakeTime.minusMinutes(15))
                .durationMinutes(10)
                .icon(StepType.SHAPE.getIcon())
                .build());

        steps.add(ScheduledStep.builder()
                .stepNumber(stepNumber++)
                .type(StepType.BAKE)
                .title("Wypiek")
                .scheduledTime(bakeTime)
                .durationMinutes(2)
                .icon(StepType.BAKE.getIcon())
                .build());

        return steps;
    }

    private void validateOwnership(ActivePizza pizza, String userId) {
        if (!pizza.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Brak dostępu do tej aktywnej pizzy");
        }
    }
}
