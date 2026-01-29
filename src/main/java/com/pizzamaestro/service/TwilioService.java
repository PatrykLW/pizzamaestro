package com.pizzamaestro.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Serwis do wysyłania SMS przez Twilio.
 * Obsługuje wysyłanie powiadomień i weryfikację numerów telefonów.
 */
@Service
@Slf4j
public class TwilioService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromPhoneNumber;

    @Value("${twilio.verify-service-sid:}")
    private String verifyServiceSid;

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                initialized = true;
                log.info("✅ Twilio zainicjalizowany pomyślnie");
                log.debug("   📱 Numer wysyłający: {}", fromPhoneNumber);
            } catch (Exception e) {
                log.warn("⚠️ Nie udało się zainicjalizować Twilio: {}", e.getMessage());
                initialized = false;
            }
        } else {
            log.warn("⚠️ Twilio nie skonfigurowane - brak ACCOUNT_SID lub AUTH_TOKEN");
            initialized = false;
        }
    }

    /**
     * Sprawdza czy Twilio jest skonfigurowane i gotowe do użycia.
     */
    public boolean isAvailable() {
        return initialized && fromPhoneNumber != null && !fromPhoneNumber.isEmpty();
    }

    /**
     * Wysyła SMS na podany numer.
     *
     * @param toPhoneNumber numer telefonu odbiorcy (format E.164, np. +48123456789)
     * @param messageBody treść wiadomości
     * @return true jeśli wysłano pomyślnie
     */
    public boolean sendSms(String toPhoneNumber, String messageBody) {
        if (!isAvailable()) {
            log.warn("⚠️ SMS nie wysłany - Twilio nie jest skonfigurowane");
            return false;
        }

        if (toPhoneNumber == null || toPhoneNumber.isEmpty()) {
            log.warn("⚠️ SMS nie wysłany - brak numeru telefonu");
            return false;
        }

        if (messageBody == null || messageBody.isEmpty()) {
            log.warn("⚠️ SMS nie wysłany - pusta wiadomość");
            return false;
        }

        try {
            log.info("📱 Wysyłanie SMS na {}: {}", maskPhoneNumber(toPhoneNumber), truncateMessage(messageBody));
            
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            log.info("✅ SMS wysłany pomyślnie, SID: {}", message.getSid());
            return true;

        } catch (ApiException e) {
            log.error("❌ Błąd Twilio API podczas wysyłania SMS: {} - {}", e.getCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Nieoczekiwany błąd podczas wysyłania SMS: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Wysyła SMS asynchronicznie (nie blokuje wątku).
     */
    @Async
    public void sendSmsAsync(String toPhoneNumber, String messageBody) {
        sendSms(toPhoneNumber, messageBody);
    }

    /**
     * Wysyła powiadomienie o nadchodzącym kroku w pizzy.
     */
    public boolean sendStepReminder(String toPhoneNumber, String stepTitle, int minutesBefore) {
        String message = String.format(
                "🍕 PizzaMaestro: Za %d min - %s. Przygotuj się!",
                minutesBefore,
                stepTitle
        );
        return sendSms(toPhoneNumber, message);
    }

    /**
     * Wysyła powiadomienie o czasie wykonania kroku (teraz!).
     */
    public boolean sendStepNow(String toPhoneNumber, String stepTitle) {
        String message = String.format(
                "🍕 PizzaMaestro: TERAZ - %s! Czas działać!",
                stepTitle
        );
        return sendSms(toPhoneNumber, message);
    }

    /**
     * Wysyła powiadomienie o opóźnieniu w kroku.
     */
    public boolean sendStepOverdue(String toPhoneNumber, String stepTitle, int minutesOverdue) {
        String message = String.format(
                "⚠️ PizzaMaestro: Krok '%s' jest już %d min opóźniony. Czy wykonałeś go?",
                stepTitle,
                minutesOverdue
        );
        return sendSms(toPhoneNumber, message);
    }

    /**
     * Wysyła powiadomienie o gotowości pizzy.
     */
    public boolean sendPizzaReady(String toPhoneNumber, String pizzaName) {
        String message = String.format(
                "🎉 PizzaMaestro: Pizza '%s' gotowa do pieczenia! Buon appetito!",
                pizzaName
        );
        return sendSms(toPhoneNumber, message);
    }

    // ==================== Weryfikacja numeru telefonu ====================

    /**
     * Rozpoczyna weryfikację numeru telefonu (wysyła kod SMS).
     * Wymaga skonfigurowanego Twilio Verify Service.
     *
     * @param phoneNumber numer telefonu do weryfikacji
     * @return true jeśli kod został wysłany
     */
    public boolean startPhoneVerification(String phoneNumber) {
        if (!initialized || verifyServiceSid == null || verifyServiceSid.isEmpty()) {
            log.warn("⚠️ Weryfikacja niedostępna - brak Twilio Verify Service SID");
            return false;
        }

        try {
            log.info("📱 Rozpoczynanie weryfikacji numeru: {}", maskPhoneNumber(phoneNumber));

            Verification verification = Verification.creator(
                    verifyServiceSid,
                    phoneNumber,
                    "sms"
            ).create();

            log.info("✅ Kod weryfikacyjny wysłany, status: {}", verification.getStatus());
            return true;

        } catch (ApiException e) {
            log.error("❌ Błąd Twilio Verify: {} - {}", e.getCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Nieoczekiwany błąd podczas weryfikacji: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sprawdza kod weryfikacyjny.
     *
     * @param phoneNumber numer telefonu
     * @param code        kod weryfikacyjny wprowadzony przez użytkownika
     * @return true jeśli kod jest poprawny
     */
    public boolean checkVerificationCode(String phoneNumber, String code) {
        if (!initialized || verifyServiceSid == null || verifyServiceSid.isEmpty()) {
            log.warn("⚠️ Weryfikacja niedostępna - brak Twilio Verify Service SID");
            return false;
        }

        if (code == null || code.isEmpty()) {
            log.warn("⚠️ Pusty kod weryfikacyjny");
            return false;
        }

        try {
            log.info("🔍 Sprawdzanie kodu weryfikacyjnego dla: {}", maskPhoneNumber(phoneNumber));

            VerificationCheck verificationCheck = VerificationCheck.creator(verifyServiceSid)
                    .setTo(phoneNumber)
                    .setCode(code)
                    .create();

            boolean isApproved = "approved".equals(verificationCheck.getStatus());
            
            if (isApproved) {
                log.info("✅ Kod weryfikacyjny poprawny");
            } else {
                log.warn("⚠️ Niepoprawny kod weryfikacyjny, status: {}", verificationCheck.getStatus());
            }

            return isApproved;

        } catch (ApiException e) {
            log.error("❌ Błąd Twilio Verify Check: {} - {}", e.getCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Nieoczekiwany błąd podczas weryfikacji kodu: {}", e.getMessage(), e);
            return false;
        }
    }

    // ==================== Metody pomocnicze ====================

    /**
     * Maskuje numer telefonu dla logów (np. +48***456789).
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 6) {
            return "***";
        }
        return phoneNumber.substring(0, 3) + "***" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * Skraca wiadomość dla logów.
     */
    private String truncateMessage(String message) {
        if (message == null) return "";
        if (message.length() <= 50) return message;
        return message.substring(0, 47) + "...";
    }

    /**
     * Formatuje numer telefonu do formatu E.164 (jeśli potrzeba).
     */
    public String formatPhoneNumber(String phoneNumber, String defaultCountryCode) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }

        // Usuń wszystkie spacje, myślniki, nawiasy
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Jeśli już ma prefix +, zwróć
        if (cleaned.startsWith("+")) {
            return cleaned;
        }

        // Jeśli zaczyna się od 00, zamień na +
        if (cleaned.startsWith("00")) {
            return "+" + cleaned.substring(2);
        }

        // Dodaj domyślny prefix kraju
        String countryCode = defaultCountryCode != null ? defaultCountryCode : "+48";
        if (!countryCode.startsWith("+")) {
            countryCode = "+" + countryCode;
        }

        return countryCode + cleaned;
    }
}
