package com.pizzamaestro.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTOs związane z autentykacją.
 */
public class AuthRequest {
    
    /**
     * Żądanie rejestracji.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format email")
        private String email;
        
        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 8, max = 100, message = "Hasło musi mieć od 8 do 100 znaków")
        @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Hasło musi zawierać: wielką literę, małą literę, cyfrę i znak specjalny"
        )
        private String password;
        
        @Size(max = 50, message = "Imię nie może przekroczyć 50 znaków")
        private String firstName;
        
        @Size(max = 50, message = "Nazwisko nie może przekroczyć 50 znaków")
        private String lastName;
        
        private String language;
    }
    
    /**
     * Żądanie logowania.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format email")
        private String email;
        
        @NotBlank(message = "Hasło jest wymagane")
        private String password;
        
        private boolean rememberMe;
    }
    
    /**
     * Żądanie odświeżenia tokenu.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        
        @NotBlank(message = "Token odświeżania jest wymagany")
        private String refreshToken;
    }
    
    /**
     * Żądanie resetu hasła.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format email")
        private String email;
    }
    
    /**
     * Żądanie ustawienia nowego hasła.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        
        @NotBlank(message = "Token resetu jest wymagany")
        private String token;
        
        @NotBlank(message = "Nowe hasło jest wymagane")
        @Size(min = 8, max = 100, message = "Hasło musi mieć od 8 do 100 znaków")
        @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Hasło musi zawierać: wielką literę, małą literę, cyfrę i znak specjalny"
        )
        private String newPassword;
    }
    
    /**
     * Żądanie zmiany hasła.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        
        @NotBlank(message = "Obecne hasło jest wymagane")
        private String currentPassword;
        
        @NotBlank(message = "Nowe hasło jest wymagane")
        @Size(min = 8, max = 100, message = "Hasło musi mieć od 8 do 100 znaków")
        @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Hasło musi zawierać: wielką literę, małą literę, cyfrę i znak specjalny"
        )
        private String newPassword;
    }
}
