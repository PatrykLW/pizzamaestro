package com.pizzamaestro.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Provider tokenów JWT.
 * Odpowiada za generowanie i walidację tokenów.
 */
@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;
    
    /**
     * Generuje token dostępu.
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), jwtExpiration);
    }
    
    /**
     * Generuje token dostępu dla użytkownika.
     */
    public String generateAccessToken(String email) {
        return generateToken(email, jwtExpiration);
    }
    
    /**
     * Generuje token odświeżania.
     */
    public String generateRefreshToken(String email) {
        return generateToken(email, refreshExpiration);
    }
    
    private String generateToken(String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Pobiera email z tokenu.
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    /**
     * Waliduje token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Nieprawidłowy token JWT");
        } catch (ExpiredJwtException ex) {
            log.error("Token JWT wygasł");
        } catch (UnsupportedJwtException ex) {
            log.error("Nieobsługiwany token JWT");
        } catch (IllegalArgumentException ex) {
            log.error("Pusty claim JWT");
        }
        return false;
    }
    
    /**
     * Pobiera czas wygaśnięcia tokenu.
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
