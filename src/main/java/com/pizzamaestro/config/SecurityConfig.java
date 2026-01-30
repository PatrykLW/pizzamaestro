package com.pizzamaestro.config;

import com.pizzamaestro.security.JwtAuthenticationFilter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Konfiguracja Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOriginsConfig;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }
    
    @PostConstruct
    public void init() {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║ 🔐 INICJALIZACJA KONFIGURACJI SECURITY                   ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("   ✅ JWT Authentication Filter: aktywny");
        log.info("   ✅ BCrypt Password Encoder: strength=12");
        log.info("   ✅ Session Management: STATELESS");
        log.info("   ✅ CORS: localhost:3000, localhost:8080");
        log.info("   ✅ Method Security: @PreAuthorize/@PostAuthorize enabled");
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔧 Konfiguracja Security Filter Chain...");
        
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publiczne endpointy
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/calculator/public/**").permitAll()
                        .requestMatchers("/api/ingredients/public/**").permitAll()
                        .requestMatchers("/api/styles/**").permitAll()
                        .requestMatchers("/api/tips/**").permitAll()
                        .requestMatchers("/api/weather/**").permitAll()
                        .requestMatchers("/api/knowledge/**").permitAll()
                        .requestMatchers("/api/recipes/shared/**").permitAll()
                        
                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**", "/api-docs.yaml").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        
                        // Statyczne zasoby
                        .requestMatchers("/", "/index.html", "/static/**").permitAll()
                        .requestMatchers("/favicon.ico", "/manifest.json", "/logo*.png").permitAll()
                        .requestMatchers("/*.js", "/*.css", "/*.map").permitAll()
                        
                        // Actuator
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // Pozostałe wymagają autentykacji
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        log.info("   ✅ Publiczne endpointy: /api/auth/**, /api/calculator/public/**, /api/ingredients/public/**");
        log.info("   ✅ Publiczne endpointy: /api/tips/**, /api/weather/**, /api/knowledge/**");
        log.info("   ✅ Swagger UI: /swagger-ui/**");
        log.info("   🔒 Pozostałe endpointy wymagają autentykacji");
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("🌐 Konfiguracja CORS...");
        
        CorsConfiguration configuration = new CorsConfiguration();
        // Origins z konfiguracji (zmienna środowiskowa APP_CORS_ALLOWED_ORIGINS)
        List<String> allowedOrigins = Arrays.asList(allowedOriginsConfig.split(","));
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        log.debug("   Allowed origins: {}", allowedOrigins);
        log.debug("   Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS");
        log.debug("   Max age: 3600s (1 hour)");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.debug("🔑 Tworzenie DaoAuthenticationProvider...");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug("🔧 Tworzenie AuthenticationManager...");
        return config.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("🔒 Tworzenie BCryptPasswordEncoder (strength={})...", 
                com.pizzamaestro.constants.CalculatorConstants.BCRYPT_STRENGTH);
        return new BCryptPasswordEncoder(com.pizzamaestro.constants.CalculatorConstants.BCRYPT_STRENGTH);
    }
}
