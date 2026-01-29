package com.pizzamaestro.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Encja powiadomienia - SMS, email lub push.
 * Służy do planowania i śledzenia wysłanych powiadomień.
 */
@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String recipeId;
    
    private Recipe.StepType stepType;
    
    private int stepNumber;
    
    private NotificationType type;
    
    private String title;
    
    private String message;
    
    @Indexed
    private LocalDateTime scheduledTime;
    
    private NotificationStatus status;
    
    private LocalDateTime sentAt;
    
    private String errorMessage;
    
    private int retryCount;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    /**
     * Typ powiadomienia.
     */
    public enum NotificationType {
        SMS,
        EMAIL,
        PUSH,
        IN_APP
    }
    
    /**
     * Status powiadomienia.
     */
    public enum NotificationStatus {
        PENDING,     // Oczekuje na wysłanie
        SCHEDULED,   // Zaplanowane w schedulerze
        SENT,        // Wysłane
        DELIVERED,   // Dostarczone (jeśli mamy potwierdzenie)
        FAILED,      // Nie udało się wysłać
        CANCELLED    // Anulowane
    }
    
    /**
     * Sprawdza czy powiadomienie jest jeszcze do wysłania.
     */
    public boolean isPending() {
        return status == NotificationStatus.PENDING || status == NotificationStatus.SCHEDULED;
    }
}
