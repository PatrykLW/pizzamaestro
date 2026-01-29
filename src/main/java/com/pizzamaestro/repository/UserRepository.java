package com.pizzamaestro.repository;

import com.pizzamaestro.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repozytorium użytkowników.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByVerificationToken(String token);
    
    Optional<User> findByResetPasswordToken(String token);
    
    List<User> findByAccountType(User.AccountType accountType);
    
    List<User> findByPremiumExpiresAtBefore(LocalDateTime dateTime);
    
    long countByAccountType(User.AccountType accountType);
    
    long countByCreatedAtAfter(LocalDateTime dateTime);
}
