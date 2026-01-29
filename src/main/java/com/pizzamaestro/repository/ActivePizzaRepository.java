package com.pizzamaestro.repository;

import com.pizzamaestro.model.ActivePizza;
import com.pizzamaestro.model.ActivePizza.ActivePizzaStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repozytorium dla aktywnych pizz (w trakcie przygotowania).
 */
@Repository
public interface ActivePizzaRepository extends MongoRepository<ActivePizza, String> {

    /**
     * Znajduje aktywną pizzę użytkownika (status IN_PROGRESS lub PLANNING)
     */
    Optional<ActivePizza> findByUserIdAndStatusIn(String userId, List<ActivePizzaStatus> statuses);

    /**
     * Znajduje aktywną pizzę użytkownika, która jest w toku
     */
    default Optional<ActivePizza> findActiveByUserId(String userId) {
        return findByUserIdAndStatusIn(userId, List.of(ActivePizzaStatus.PLANNING, ActivePizzaStatus.IN_PROGRESS));
    }

    /**
     * Znajduje wszystkie pizze użytkownika
     */
    List<ActivePizza> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Znajduje pizze po statusie
     */
    List<ActivePizza> findByStatus(ActivePizzaStatus status);

    /**
     * Znajduje wszystkie aktywne pizze (do wysyłania powiadomień)
     */
    List<ActivePizza> findByStatusIn(List<ActivePizzaStatus> statuses);

    /**
     * Znajduje pizze z krokami, które wymagają powiadomienia
     * (powiadomienia SMS włączone, status aktywny)
     */
    @Query("{ 'smsNotificationsEnabled': true, 'status': { $in: ['PLANNING', 'IN_PROGRESS'] } }")
    List<ActivePizza> findPizzasForNotifications();

    /**
     * Znajduje pizze z zaplanowanym czasem wypieku w określonym przedziale
     */
    List<ActivePizza> findByTargetBakeTimeBetweenAndStatusIn(
            LocalDateTime from,
            LocalDateTime to,
            List<ActivePizzaStatus> statuses
    );

    /**
     * Sprawdza czy użytkownik ma aktywną pizzę
     */
    boolean existsByUserIdAndStatusIn(String userId, List<ActivePizzaStatus> statuses);

    /**
     * Liczy aktywne pizze użytkownika
     */
    long countByUserIdAndStatusIn(String userId, List<ActivePizzaStatus> statuses);

    /**
     * Usuwa zakończone pizze starsze niż podana data
     */
    void deleteByStatusAndLastUpdatedAtBefore(ActivePizzaStatus status, LocalDateTime before);

    /**
     * Znajduje ostatnio ukończone pizze użytkownika
     */
    List<ActivePizza> findByUserIdAndStatusOrderByLastUpdatedAtDesc(String userId, ActivePizzaStatus status);
}
