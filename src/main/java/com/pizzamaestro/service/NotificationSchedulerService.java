package com.pizzamaestro.service;

import com.pizzamaestro.model.ActivePizza;
import com.pizzamaestro.model.ActivePizza.*;
import com.pizzamaestro.repository.ActivePizzaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Serwis harmonogramu powiadomień.
 * Sprawdza aktywne pizze i wysyła powiadomienia SMS o nadchodzących krokach.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSchedulerService {

    private final ActivePizzaRepository activePizzaRepository;
    private final TwilioService twilioService;
    private final ActivePizzaService activePizzaService;

    /**
     * Sprawdza i wysyła powiadomienia co minutę.
     */
    @Scheduled(fixedRate = 60000) // Co minutę
    @Transactional
    public void processScheduledNotifications() {
        if (!twilioService.isAvailable()) {
            log.trace("⏭️ Twilio niedostępne - pomijam sprawdzanie powiadomień");
            return;
        }

        log.debug("🔔 Sprawdzanie powiadomień SMS...");
        
        // Pobierz wszystkie aktywne pizze z włączonymi powiadomieniami
        List<ActivePizza> pizzasForNotifications = activePizzaRepository.findPizzasForNotifications();
        
        if (pizzasForNotifications.isEmpty()) {
            log.trace("   Brak pizz do powiadomień");
            return;
        }

        log.debug("   Znaleziono {} pizz z włączonymi powiadomieniami", pizzasForNotifications.size());
        
        LocalDateTime now = LocalDateTime.now();
        
        for (ActivePizza pizza : pizzasForNotifications) {
            processNotificationsForPizza(pizza, now);
        }
    }

    /**
     * Przetwarza powiadomienia dla pojedynczej pizzy.
     */
    private void processNotificationsForPizza(ActivePizza pizza, LocalDateTime now) {
        if (pizza.getNotificationPhone() == null || pizza.getNotificationPhone().isEmpty()) {
            log.trace("   Pizza {} - brak numeru telefonu", pizza.getId());
            return;
        }

        int reminderMinutes = pizza.getReminderMinutesBefore() != null ? pizza.getReminderMinutesBefore() : 15;
        
        for (ScheduledStep step : pizza.getSteps()) {
            // Pomiń już ukończone/pominięte kroki
            if (step.getStatus() != StepStatus.PENDING && step.getStatus() != StepStatus.IN_PROGRESS) {
                continue;
            }
            
            // Pomiń kroki bez zaplanowanego czasu
            if (step.getScheduledTime() == null) {
                continue;
            }
            
            // Pomiń już powiadomione kroki
            if (step.isNotificationSent()) {
                continue;
            }
            
            long minutesToStep = ChronoUnit.MINUTES.between(now, step.getScheduledTime());
            
            // Sprawdź czy czas na wysłanie przypomnienia (X minut przed)
            if (minutesToStep > 0 && minutesToStep <= reminderMinutes) {
                sendReminderNotification(pizza, step, (int) minutesToStep);
            }
            // Sprawdź czy to czas na wykonanie (teraz!)
            else if (minutesToStep <= 0 && minutesToStep >= -2) {
                sendNowNotification(pizza, step);
            }
            // Sprawdź czy krok jest opóźniony (więcej niż 10 minut po)
            else if (minutesToStep < -10 && minutesToStep >= -30) {
                sendOverdueNotification(pizza, step, (int) Math.abs(minutesToStep));
            }
        }
    }

    /**
     * Wysyła przypomnienie przed krokiem.
     */
    private void sendReminderNotification(ActivePizza pizza, ScheduledStep step, int minutesBefore) {
        log.info("📱 Wysyłanie przypomnienia dla pizzy {}, krok {}: {} (za {} min)",
                pizza.getId(), step.getStepNumber(), step.getTitle(), minutesBefore);
        
        boolean sent = twilioService.sendStepReminder(
                pizza.getNotificationPhone(),
                step.getTitle(),
                minutesBefore
        );
        
        if (sent) {
            activePizzaService.markStepNotified(pizza.getId(), step.getStepNumber());
        }
    }

    /**
     * Wysyła powiadomienie "teraz!".
     */
    private void sendNowNotification(ActivePizza pizza, ScheduledStep step) {
        log.info("📱 Wysyłanie powiadomienia TERAZ dla pizzy {}, krok {}: {}",
                pizza.getId(), step.getStepNumber(), step.getTitle());
        
        boolean sent = twilioService.sendStepNow(
                pizza.getNotificationPhone(),
                step.getTitle()
        );
        
        if (sent) {
            activePizzaService.markStepNotified(pizza.getId(), step.getStepNumber());
        }
    }

    /**
     * Wysyła powiadomienie o opóźnieniu.
     */
    private void sendOverdueNotification(ActivePizza pizza, ScheduledStep step, int minutesOverdue) {
        log.info("📱 Wysyłanie powiadomienia o opóźnieniu dla pizzy {}, krok {}: {} ({} min temu)",
                pizza.getId(), step.getStepNumber(), step.getTitle(), minutesOverdue);
        
        boolean sent = twilioService.sendStepOverdue(
                pizza.getNotificationPhone(),
                step.getTitle(),
                minutesOverdue
        );
        
        if (sent) {
            // Oznacz jako powiadomione aby nie spamować
            activePizzaService.markStepNotified(pizza.getId(), step.getStepNumber());
        }
    }

    /**
     * Sprawdza ukończone pizze i wysyła gratulacje.
     */
    @Scheduled(fixedRate = 300000) // Co 5 minut
    @Transactional
    public void checkCompletedPizzas() {
        if (!twilioService.isAvailable()) {
            return;
        }

        // Znajdź pizze które właśnie zostały ukończone (ostatnia aktualizacja w ciągu 5 min)
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<ActivePizza> completedPizzas = activePizzaRepository.findByStatusIn(
                List.of(ActivePizzaStatus.COMPLETED)
        );

        for (ActivePizza pizza : completedPizzas) {
            if (pizza.getLastUpdatedAt() != null && pizza.getLastUpdatedAt().isAfter(fiveMinutesAgo)) {
                if (pizza.isSmsNotificationsEnabled() && pizza.getNotificationPhone() != null) {
                    log.info("🎉 Wysyłanie gratulacji dla ukończonej pizzy: {}", pizza.getName());
                    twilioService.sendPizzaReady(pizza.getNotificationPhone(), pizza.getName());
                }
            }
        }
    }

    /**
     * Czyści stare dane (zakończone pizze starsze niż 30 dni).
     */
    @Scheduled(cron = "0 0 3 * * *") // Codziennie o 3:00
    @Transactional
    public void cleanupOldPizzas() {
        log.info("🧹 Czyszczenie starych zakończonych pizz...");
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        activePizzaRepository.deleteByStatusAndLastUpdatedAtBefore(
                ActivePizzaStatus.COMPLETED,
                thirtyDaysAgo
        );
        
        activePizzaRepository.deleteByStatusAndLastUpdatedAtBefore(
                ActivePizzaStatus.CANCELLED,
                thirtyDaysAgo
        );
        
        log.info("✅ Czyszczenie zakończone");
    }

    /**
     * Ręczne testowanie wysyłki SMS (dla adminów).
     */
    public boolean sendTestSms(String phoneNumber, String message) {
        if (!twilioService.isAvailable()) {
            log.warn("⚠️ Twilio niedostępne dla testu SMS");
            return false;
        }
        
        log.info("🧪 Wysyłanie testowego SMS na: {}", phoneNumber);
        return twilioService.sendSms(phoneNumber, message);
    }
}
