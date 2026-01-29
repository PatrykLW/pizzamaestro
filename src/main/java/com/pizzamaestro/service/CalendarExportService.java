package com.pizzamaestro.service;

import com.pizzamaestro.model.ActivePizza;
import com.pizzamaestro.model.ActivePizza.ScheduledStep;
import com.pizzamaestro.model.Recipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Serwis do eksportu harmonogramów do formatu iCalendar (ICS).
 * Pozwala na import harmonogramu pizzy do Google Calendar, Apple Calendar, Outlook itp.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarExportService {

    private static final DateTimeFormatter ICAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final String PRODID = "-//PizzaMaestro//Pizza Calendar//PL";

    /**
     * Generuje plik iCalendar (.ics) dla aktywnej pizzy.
     */
    public String generateICalForActivePizza(ActivePizza pizza) {
        log.info("📅 Generowanie pliku iCal dla pizzy: {}", pizza.getName());

        StringBuilder ics = new StringBuilder();
        
        // Nagłówek kalendarza
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:").append(PRODID).append("\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("X-WR-CALNAME:PizzaMaestro - ").append(escapeIcalText(pizza.getName())).append("\r\n");
        ics.append("X-WR-TIMEZONE:Europe/Warsaw\r\n");
        
        // Dodaj strefę czasową
        appendTimezone(ics);
        
        // Dodaj wydarzenia dla każdego kroku
        for (ScheduledStep step : pizza.getSteps()) {
            if (step.getScheduledTime() != null) {
                appendEvent(ics, pizza, step);
            }
        }
        
        // Zamknięcie kalendarza
        ics.append("END:VCALENDAR\r\n");
        
        log.info("✅ Plik iCal wygenerowany, {} wydarzeń", pizza.getSteps().size());
        return ics.toString();
    }

    /**
     * Generuje plik iCalendar dla przepisu (harmonogram fermentacji).
     */
    public String generateICalForRecipe(Recipe recipe, LocalDateTime startTime) {
        log.info("📅 Generowanie pliku iCal dla przepisu: {}", recipe.getName());

        StringBuilder ics = new StringBuilder();
        
        // Nagłówek
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:").append(PRODID).append("\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("X-WR-CALNAME:Pizza - ").append(escapeIcalText(recipe.getName())).append("\r\n");
        
        appendTimezone(ics);
        
        // Dodaj wydarzenia z harmonogramu przepisu
        if (recipe.getFermentationSteps() != null) {
            for (var step : recipe.getFermentationSteps()) {
                appendRecipeStepEvent(ics, recipe, step);
            }
        }
        
        ics.append("END:VCALENDAR\r\n");
        
        return ics.toString();
    }

    /**
     * Dodaje pojedyncze wydarzenie dla kroku pizzy.
     */
    private void appendEvent(StringBuilder ics, ActivePizza pizza, ScheduledStep step) {
        String uid = UUID.randomUUID().toString() + "@pizzamaestro.pl";
        LocalDateTime start = step.getScheduledTime();
        LocalDateTime end = start.plusMinutes(step.getDurationMinutes() != null ? step.getDurationMinutes() : 15);
        
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(uid).append("\r\n");
        ics.append("DTSTAMP:").append(formatDateTime(LocalDateTime.now())).append("\r\n");
        ics.append("DTSTART:").append(formatDateTime(start)).append("\r\n");
        ics.append("DTEND:").append(formatDateTime(end)).append("\r\n");
        ics.append("SUMMARY:🍕 ").append(escapeIcalText(step.getTitle())).append("\r\n");
        
        // Opis wydarzenia
        StringBuilder description = new StringBuilder();
        description.append("Pizza: ").append(pizza.getName()).append("\\n");
        if (step.getDescription() != null) {
            description.append("\\n").append(step.getDescription());
        }
        if (step.getTemperature() != null) {
            description.append("\\nTemperatura: ").append(step.getTemperature()).append("°C");
        }
        ics.append("DESCRIPTION:").append(escapeIcalText(description.toString())).append("\r\n");
        
        // Kategoria
        ics.append("CATEGORIES:Pizza,Gotowanie,PizzaMaestro\r\n");
        
        // Przypomnienie 15 minut przed
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-PT15M\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:Czas na: ").append(escapeIcalText(step.getTitle())).append("\r\n");
        ics.append("END:VALARM\r\n");
        
        // Drugie przypomnienie 5 minut przed
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-PT5M\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:Za 5 minut: ").append(escapeIcalText(step.getTitle())).append("\r\n");
        ics.append("END:VALARM\r\n");
        
        ics.append("END:VEVENT\r\n");
    }

    /**
     * Dodaje wydarzenie dla kroku przepisu.
     */
    private void appendRecipeStepEvent(StringBuilder ics, Recipe recipe, Recipe.FermentationStep step) {
        if (step.getScheduledTime() == null) {
            return;
        }

        String uid = UUID.randomUUID().toString() + "@pizzamaestro.pl";
        LocalDateTime start = step.getScheduledTime();
        LocalDateTime end = start.plusMinutes(step.getDurationMinutes() != null ? step.getDurationMinutes() : 15);
        
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(uid).append("\r\n");
        ics.append("DTSTAMP:").append(formatDateTime(LocalDateTime.now())).append("\r\n");
        ics.append("DTSTART:").append(formatDateTime(start)).append("\r\n");
        ics.append("DTEND:").append(formatDateTime(end)).append("\r\n");
        ics.append("SUMMARY:🍕 ").append(escapeIcalText(step.getTitle())).append("\r\n");
        
        if (step.getDescription() != null) {
            ics.append("DESCRIPTION:").append(escapeIcalText(step.getDescription())).append("\r\n");
        }
        
        ics.append("CATEGORIES:Pizza,Gotowanie\r\n");
        
        // Przypomnienie
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-PT10M\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:").append(escapeIcalText(step.getTitle())).append("\r\n");
        ics.append("END:VALARM\r\n");
        
        ics.append("END:VEVENT\r\n");
    }

    /**
     * Dodaje definicję strefy czasowej.
     */
    private void appendTimezone(StringBuilder ics) {
        ics.append("BEGIN:VTIMEZONE\r\n");
        ics.append("TZID:Europe/Warsaw\r\n");
        ics.append("BEGIN:STANDARD\r\n");
        ics.append("DTSTART:19701025T030000\r\n");
        ics.append("RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n");
        ics.append("TZOFFSETFROM:+0200\r\n");
        ics.append("TZOFFSETTO:+0100\r\n");
        ics.append("TZNAME:CET\r\n");
        ics.append("END:STANDARD\r\n");
        ics.append("BEGIN:DAYLIGHT\r\n");
        ics.append("DTSTART:19700329T020000\r\n");
        ics.append("RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n");
        ics.append("TZOFFSETFROM:+0100\r\n");
        ics.append("TZOFFSETTO:+0200\r\n");
        ics.append("TZNAME:CEST\r\n");
        ics.append("END:DAYLIGHT\r\n");
        ics.append("END:VTIMEZONE\r\n");
    }

    /**
     * Formatuje datę do formatu iCalendar.
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(ICAL_DATE_FORMAT);
    }

    /**
     * Escapuje znaki specjalne dla formatu iCalendar.
     */
    private String escapeIcalText(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
