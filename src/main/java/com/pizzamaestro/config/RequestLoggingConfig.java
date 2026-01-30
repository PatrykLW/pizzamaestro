package com.pizzamaestro.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Konfiguracja rozbudowanego logowania requestów HTTP.
 * 
 * Loguje:
 * - Wszystkie przychodzące requesty (metoda, URL, parametry, nagłówki)
 * - Czas odpowiedzi
 * - Status odpowiedzi
 * - Błędy
 */
@Configuration
@Slf4j
public class RequestLoggingConfig {
    
    /**
     * Standardowy filtr logowania Spring.
     */
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setHeaderPredicate(header -> 
                !header.equalsIgnoreCase("Authorization") && 
                !header.equalsIgnoreCase("Cookie"));
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
    
    /**
     * Zaawansowany filtr logowania z czasem odpowiedzi.
     */
    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Slf4j
    public static class DetailedLoggingFilter extends OncePerRequestFilter {
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        FilterChain filterChain) 
                throws ServletException, IOException {
            
            // Opakuj request i response do cachowania zawartości
            ContentCachingRequestWrapper wrappedRequest = 
                    new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper wrappedResponse = 
                    new ContentCachingResponseWrapper(response);
            
            Instant start = Instant.now();
            String requestId = generateRequestId();
            
            try {
                // Log rozpoczęcia
                logRequest(wrappedRequest, requestId);
                
                // Wykonaj request
                filterChain.doFilter(wrappedRequest, wrappedResponse);
                
            } catch (Exception e) {
                log.error("═══════════════════════════════════════════════════════");
                log.error("🔴 BŁĄD PODCZAS PRZETWARZANIA REQUEST [{}]", requestId);
                log.error("   Wyjątek: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                log.error("   Stack trace:", e);
                log.error("═══════════════════════════════════════════════════════");
                throw e;
            } finally {
                // Log zakończenia
                Duration duration = Duration.between(start, Instant.now());
                logResponse(wrappedRequest, wrappedResponse, duration, requestId);
                
                // WAŻNE: Skopiuj response body do rzeczywistego outputu
                wrappedResponse.copyBodyToResponse();
            }
        }
        
        private void logRequest(ContentCachingRequestWrapper request, String requestId) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String clientIp = getClientIp(request);
            
            log.info("═══════════════════════════════════════════════════════");
            log.info("🟢 NOWY REQUEST [{}]", requestId);
            log.info("   {} {} {}", method, uri, queryString != null ? "?" + queryString : "");
            log.info("   IP klienta: {}", clientIp);
            log.info("   User-Agent: {}", request.getHeader("User-Agent"));
            
            // Loguj nagłówki (bez wrażliwych)
            Collections.list(request.getHeaderNames()).stream()
                    .filter(h -> !h.equalsIgnoreCase("Authorization") && 
                                 !h.equalsIgnoreCase("Cookie") &&
                                 !h.equalsIgnoreCase("User-Agent"))
                    .forEach(header -> 
                            log.debug("   Header: {} = {}", header, request.getHeader(header)));
            
            // Loguj parametry
            if (!request.getParameterMap().isEmpty()) {
                log.info("   Parametry: {}", request.getParameterMap().entrySet().stream()
                        .map(e -> e.getKey() + "=" + String.join(",", e.getValue()))
                        .collect(Collectors.joining(", ")));
            }
        }
        
        private void logResponse(ContentCachingRequestWrapper request,
                                ContentCachingResponseWrapper response,
                                Duration duration,
                                String requestId) {
            int status = response.getStatus();
            String statusEmoji = getStatusEmoji(status);
            
            log.info("───────────────────────────────────────────────────────");
            log.info("{} RESPONSE [{}]", statusEmoji, requestId);
            log.info("   Status: {} {}", status, getStatusDescription(status));
            log.info("   Czas: {} ms", duration.toMillis());
            log.info("   Content-Type: {}", response.getContentType());
            log.info("   Content-Length: {} bytes", response.getContentSize());
            
            // Loguj body dla błędów
            if (status >= 400) {
                String responseBody = getResponseBody(response);
                if (responseBody != null && !responseBody.isEmpty()) {
                    log.warn("   Response Body: {}", 
                            responseBody.length() > 500 ? 
                                    responseBody.substring(0, 500) + "..." : responseBody);
                }
            }
            
            log.info("═══════════════════════════════════════════════════════");
        }
        
        private String getClientIp(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        
        private String generateRequestId() {
            return String.format("%06d", (int)(Math.random() * 1000000));
        }
        
        private String getStatusEmoji(int status) {
            if (status >= 200 && status < 300) return "✅";
            if (status >= 300 && status < 400) return "↪️";
            if (status >= 400 && status < 500) return "⚠️";
            return "🔴";
        }
        
        private String getStatusDescription(int status) {
            return switch (status) {
                case 200 -> "OK";
                case 201 -> "Created";
                case 204 -> "No Content";
                case 301, 302 -> "Redirect";
                case 400 -> "Bad Request";
                case 401 -> "Unauthorized";
                case 403 -> "Forbidden";
                case 404 -> "Not Found";
                case 500 -> "Internal Server Error";
                default -> "";
            };
        }
        
        private String getResponseBody(ContentCachingResponseWrapper response) {
            try {
                byte[] content = response.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, response.getCharacterEncoding());
                }
            } catch (UnsupportedEncodingException e) {
                log.debug("Nie można odczytać response body: {}", e.getMessage());
            }
            return null;
        }
        
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String uri = request.getRequestURI();
            // Nie loguj statycznych zasobów
            return uri.startsWith("/static/") || 
                   uri.startsWith("/css/") || 
                   uri.startsWith("/js/") ||
                   uri.startsWith("/images/") ||
                   uri.endsWith(".ico") ||
                   uri.endsWith(".png") ||
                   uri.endsWith(".jpg") ||
                   uri.endsWith(".map");
        }
    }
}
