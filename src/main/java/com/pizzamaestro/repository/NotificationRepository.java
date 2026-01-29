package com.pizzamaestro.repository;

import com.pizzamaestro.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozytorium powiadomień.
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    // Powiadomienia użytkownika
    List<Notification> findByUserIdOrderByScheduledTimeDesc(String userId);
    
    List<Notification> findByUserIdAndStatus(String userId, Notification.NotificationStatus status);
    
    // Powiadomienia dla receptury
    List<Notification> findByRecipeIdOrderByScheduledTimeAsc(String recipeId);
    
    List<Notification> findByRecipeIdAndStatus(String recipeId, Notification.NotificationStatus status);
    
    // Powiadomienia do wysłania
    @Query("{'status': {$in: ['PENDING', 'SCHEDULED']}, 'scheduledTime': {$lte: ?0}}")
    List<Notification> findPendingNotificationsToSend(LocalDateTime time);
    
    @Query("{'status': 'FAILED', 'retryCount': {$lt: ?0}}")
    List<Notification> findFailedNotificationsForRetry(int maxRetries);
    
    // Statystyki
    long countByUserIdAndTypeAndStatusAndSentAtAfter(
            String userId, 
            Notification.NotificationType type, 
            Notification.NotificationStatus status, 
            LocalDateTime after);
    
    // Anulowanie
    @Query(value = "{'recipeId': ?0, 'status': {$in: ['PENDING', 'SCHEDULED']}}", delete = true)
    void cancelPendingNotificationsForRecipe(String recipeId);
    
    void deleteByRecipeId(String recipeId);
}
