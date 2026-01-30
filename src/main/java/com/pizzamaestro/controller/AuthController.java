package com.pizzamaestro.controller;

import com.pizzamaestro.dto.request.AuthRequest;
import com.pizzamaestro.dto.response.AuthResponse;
import com.pizzamaestro.model.User;
import com.pizzamaestro.security.JwtTokenProvider;
import com.pizzamaestro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler autentykacji.
 * Obsługuje rejestrację, logowanie i zarządzanie tokenami.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autentykacja", description = "Endpointy rejestracji i logowania")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Rejestracja nowego użytkownika")
    public ResponseEntity<AuthResponse.JwtResponse> register(
            @Valid @RequestBody AuthRequest.RegisterRequest request) {
        
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║ 📝 REJESTRACJA NOWEGO UŻYTKOWNIKA                        ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📧 Email: {}", request.getEmail());
        log.info("👤 Imię: {} {}", request.getFirstName(), request.getLastName());
        
        try {
            User user = userService.registerUser(request);
            
            log.info("✅ Użytkownik utworzony pomyślnie!");
            log.info("   🆔 ID: {}", user.getId());
            log.info("   📧 Email: {}", user.getEmail());
            log.info("   🔐 Role: {}", user.getRoles());
            
            // Automatyczne logowanie po rejestracji
            String accessToken = tokenProvider.generateAccessToken(user.getEmail());
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
            
            log.info("🔑 Token wygenerowany, użytkownik zalogowany automatycznie");
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(AuthResponse.JwtResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .tokenType("Bearer")
                            .expiresIn(tokenProvider.getExpirationTime())
                            .user(userService.toUserInfo(user))
                            .build());
        } catch (Exception e) {
            log.error("❌ Błąd rejestracji: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Logowanie użytkownika")
    public ResponseEntity<AuthResponse.JwtResponse> login(
            @Valid @RequestBody AuthRequest.LoginRequest request) {
        
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║ 🔐 LOGOWANIE UŻYTKOWNIKA                                 ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📧 Email: {}", request.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userService.findByEmail(request.getEmail());
            userService.updateLastLogin(user.getId());
            
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
            
            log.info("✅ Logowanie udane!");
            log.info("   🆔 User ID: {}", user.getId());
            log.info("   👤 Imię: {} {}", user.getFirstName(), user.getLastName());
            log.info("   🔐 Role: {}", user.getRoles());
            log.info("   💎 Typ konta: {}", user.getAccountType());
            log.info("   🔑 Access token wygenerowany (ważny {}s)", tokenProvider.getExpirationTime()/1000);
            
            return ResponseEntity.ok(AuthResponse.JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTime())
                    .user(userService.toUserInfo(user))
                    .build());
        } catch (Exception e) {
            log.warn("❌ Błąd logowania dla {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Odświeżenie tokenu dostępu")
    public ResponseEntity<AuthResponse.JwtResponse> refreshToken(
            @Valid @RequestBody AuthRequest.RefreshTokenRequest request) {
        
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String email = tokenProvider.getEmailFromToken(request.getRefreshToken());
        User user = userService.findByEmail(email);
        
        String newAccessToken = tokenProvider.generateAccessToken(email);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);
        
        return ResponseEntity.ok(AuthResponse.JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime())
                .user(userService.toUserInfo(user))
                .build());
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Żądanie resetu hasła")
    public ResponseEntity<AuthResponse.MessageResponse> forgotPassword(
            @Valid @RequestBody AuthRequest.ForgotPasswordRequest request) {
        
        log.info("Żądanie resetu hasła dla: {}", request.getEmail());
        
        try {
            String token = userService.generatePasswordResetToken(request.getEmail());
            // Email z linkiem do resetu jest wysyłany przez EmailService (jeśli skonfigurowany)
            
            return ResponseEntity.ok(AuthResponse.MessageResponse.builder()
                    .success(true)
                    .message("Jeśli konto istnieje, link do resetu hasła został wysłany na podany adres email")
                    .build());
        } catch (Exception ignored) {
            // Nie zdradzaj czy konto istnieje
            return ResponseEntity.ok(AuthResponse.MessageResponse.builder()
                    .success(true)
                    .message("Jeśli konto istnieje, link do resetu hasła został wysłany na podany adres email")
                    .build());
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset hasła przy użyciu tokenu")
    public ResponseEntity<AuthResponse.MessageResponse> resetPassword(
            @Valid @RequestBody AuthRequest.ResetPasswordRequest request) {
        
        userService.resetPassword(request.getToken(), request.getNewPassword());
        
        return ResponseEntity.ok(AuthResponse.MessageResponse.builder()
                .success(true)
                .message("Hasło zostało zmienione. Możesz się teraz zalogować.")
                .build());
    }
}
