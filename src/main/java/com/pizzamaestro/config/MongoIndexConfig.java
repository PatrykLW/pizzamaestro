package com.pizzamaestro.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * Konfiguracja indeksów MongoDB.
 * 
 * Tworzy indeksy automatycznie przy starcie aplikacji dla:
 * - Szybszego wyszukiwania użytkowników
 * - Wydajniejszych zapytań o receptury
 * - Optymalizacji zapytań o składniki
 * - Szybszego wyszukiwania przewodników
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoIndexConfig {
    
    private final MongoTemplate mongoTemplate;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initIndexes() {
        log.info("📇 Inicjalizacja indeksów MongoDB...");
        
        try {
            createUserIndexes();
        } catch (Exception e) {
            log.warn("⚠️ Problem z indeksami users: {}", e.getMessage());
        }
        
        try {
            createRecipeIndexes();
        } catch (Exception e) {
            log.warn("⚠️ Problem z indeksami recipes: {}", e.getMessage());
        }
        
        try {
            createIngredientIndexes();
        } catch (Exception e) {
            log.warn("⚠️ Problem z indeksami ingredients: {}", e.getMessage());
        }
        
        try {
            createTechniqueGuideIndexes();
        } catch (Exception e) {
            log.warn("⚠️ Problem z indeksami technique_guides: {}", e.getMessage());
        }
        
        try {
            createNotificationIndexes();
        } catch (Exception e) {
            log.warn("⚠️ Problem z indeksami notifications: {}", e.getMessage());
        }
        
        try {
            createActivePizzaIndexes();
        } catch (Exception e) {
            log.warn("⚠️ Problem z indeksami active_pizzas: {}", e.getMessage());
        }
        
        log.info("✅ Inicjalizacja indeksów MongoDB zakończona");
    }
    
    /**
     * Bezpiecznie tworzy indeks, ignorując konflikt nazw.
     */
    private void safeEnsureIndex(IndexOperations indexOps, Index index, String description) {
        try {
            indexOps.ensureIndex(index);
            log.debug("    ✓ {}", description);
        } catch (Exception e) {
            // Ignoruj konflikty nazw indeksów - indeks już istnieje
            log.debug("    ⚠ {} - indeks już istnieje lub konflikt: {}", description, e.getMessage());
        }
    }
    
    private void createUserIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("users");
        
        // Unikalny indeks na email - może już istnieć z inną nazwą
        safeEnsureIndex(indexOps, new Index()
                .on("email", Sort.Direction.ASC)
                .unique()
                .named("email_unique_idx"), "email_unique_idx");
        
        // Indeks na username
        safeEnsureIndex(indexOps, new Index()
                .on("username", Sort.Direction.ASC)
                .named("username_idx"), "username_idx");
        
        // Indeks na typ konta (dla raportów admina)
        safeEnsureIndex(indexOps, new Index()
                .on("accountType", Sort.Direction.ASC)
                .named("account_type_idx"), "account_type_idx");
        
        // Indeks na datę ostatniego logowania
        safeEnsureIndex(indexOps, new Index()
                .on("lastLoginAt", Sort.Direction.DESC)
                .named("last_login_idx"), "last_login_idx");
        
        // Indeks na wygaśnięcie premium
        safeEnsureIndex(indexOps, new Index()
                .on("premiumExpiresAt", Sort.Direction.ASC)
                .sparse()
                .named("premium_expires_idx"), "premium_expires_idx");
        
        log.debug("  ✓ Indeksy users przetworzone");
    }
    
    private void createRecipeIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("recipes");
        
        // Indeks na userId + createdAt (lista receptur użytkownika)
        safeEnsureIndex(indexOps, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .named("user_recipes_idx"), "user_recipes_idx");
        
        // Indeks na publiczne receptury
        safeEnsureIndex(indexOps, new Index()
                .on("isPublic", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .named("public_recipes_idx"), "public_recipes_idx");
        
        // Indeks na styl pizzy
        safeEnsureIndex(indexOps, new Index()
                .on("pizzaStyle", Sort.Direction.ASC)
                .named("pizza_style_idx"), "pizza_style_idx");
        
        // Indeks na ulubione
        safeEnsureIndex(indexOps, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("isFavorite", Sort.Direction.ASC)
                .named("favorites_idx"), "favorites_idx");
        
        // Wyszukiwanie tekstowe po nazwie i opisie
        safeEnsureIndex(indexOps, new Index()
                .on("name", Sort.Direction.ASC)
                .named("recipe_name_idx"), "recipe_name_idx");
        
        log.debug("  ✓ Indeksy recipes przetworzone");
    }
    
    private void createIngredientIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("ingredients");
        
        // Indeks na typ składnika + aktywność
        safeEnsureIndex(indexOps, new Index()
                .on("type", Sort.Direction.ASC)
                .on("active", Sort.Direction.ASC)
                .named("type_active_idx"), "type_active_idx");
        
        // Indeks na nazwę
        safeEnsureIndex(indexOps, new Index()
                .on("name", Sort.Direction.ASC)
                .named("ingredient_name_idx"), "ingredient_name_idx");
        
        // Indeks na producenta
        safeEnsureIndex(indexOps, new Index()
                .on("brand", Sort.Direction.ASC)
                .named("brand_idx"), "brand_idx");
        
        // Indeks na weryfikację
        safeEnsureIndex(indexOps, new Index()
                .on("verified", Sort.Direction.ASC)
                .named("verified_idx"), "verified_idx");
        
        log.debug("  ✓ Indeksy ingredients przetworzone");
    }
    
    private void createTechniqueGuideIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("technique_guides");
        
        // Unikalny indeks na slug
        safeEnsureIndex(indexOps, new Index()
                .on("slug", Sort.Direction.ASC)
                .unique()
                .named("slug_unique_idx"), "slug_unique_idx");
        
        // Indeks na kategorię + aktywność
        safeEnsureIndex(indexOps, new Index()
                .on("category", Sort.Direction.ASC)
                .on("active", Sort.Direction.ASC)
                .named("category_active_idx"), "category_active_idx");
        
        // Indeks na poziom trudności
        safeEnsureIndex(indexOps, new Index()
                .on("difficulty", Sort.Direction.ASC)
                .named("difficulty_idx"), "difficulty_idx");
        
        // Indeks na liczbę wyświetleń (popularne)
        safeEnsureIndex(indexOps, new Index()
                .on("viewCount", Sort.Direction.DESC)
                .named("view_count_idx"), "view_count_idx");
        
        // Indeks na premium
        safeEnsureIndex(indexOps, new Index()
                .on("premium", Sort.Direction.ASC)
                .on("active", Sort.Direction.ASC)
                .named("premium_active_idx"), "premium_active_idx");
        
        // Indeks na polecane style pizzy
        safeEnsureIndex(indexOps, new Index()
                .on("recommendedForStyles", Sort.Direction.ASC)
                .named("recommended_styles_idx"), "recommended_styles_idx");
        
        log.debug("  ✓ Indeksy technique_guides przetworzone");
    }
    
    private void createNotificationIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("notifications");
        
        // Indeks na userId + nieprzeczytane
        safeEnsureIndex(indexOps, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("read", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .named("user_unread_idx"), "user_unread_idx");
        
        // TTL indeks - automatyczne usuwanie starych powiadomień po 30 dniach
        safeEnsureIndex(indexOps, new Index()
                .on("createdAt", Sort.Direction.ASC)
                .expire(30 * 24 * 60 * 60) // 30 dni w sekundach
                .named("notification_ttl_idx"), "notification_ttl_idx");
        
        log.debug("  ✓ Indeksy notifications przetworzone");
    }
    
    private void createActivePizzaIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("active_pizzas");
        
        // Indeks na userId + status (znajdowanie aktywnej pizzy użytkownika)
        safeEnsureIndex(indexOps, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .named("user_status_idx"), "user_status_idx");
        
        // Indeks na status (dla schedulera powiadomień)
        safeEnsureIndex(indexOps, new Index()
                .on("status", Sort.Direction.ASC)
                .on("smsNotificationsEnabled", Sort.Direction.ASC)
                .named("status_notifications_idx"), "status_notifications_idx");
        
        // Indeks na targetBakeTime (dla harmonogramu)
        safeEnsureIndex(indexOps, new Index()
                .on("targetBakeTime", Sort.Direction.ASC)
                .named("target_bake_time_idx"), "target_bake_time_idx");
        
        // Indeks na userId + createdAt (historia)
        safeEnsureIndex(indexOps, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .named("user_history_idx"), "user_history_idx");
        
        // TTL indeks - automatyczne usuwanie zakończonych pizz po 90 dniach
        safeEnsureIndex(indexOps, new Index()
                .on("lastUpdatedAt", Sort.Direction.ASC)
                .expire(90 * 24 * 60 * 60) // 90 dni w sekundach
                .named("active_pizza_ttl_idx"), "active_pizza_ttl_idx");
        
        log.debug("  ✓ Indeksy active_pizzas przetworzone");
    }
}
