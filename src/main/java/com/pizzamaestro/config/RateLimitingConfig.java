package com.pizzamaestro.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Konfiguracja Rate Limiting dla API.
 * 
 * Chroni przed:
 * - Atakami brute force (login, register)
 * - Nadmiernym zużyciem zasobów (kalkulacje)
 * - DDoS
 * 
 * Limity:
 * - Publiczne kalkulacje: 30 req/min
 * - Login: 5 req/min
 * - Register: 3 req/min
 * - Ogólne API: 100 req/min
 */
@Configuration
@Slf4j
public class RateLimitingConfig {
    
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        registration.setName("rateLimitFilter");
        return registration;
    }
    
    @Component
    @Slf4j
    public static class RateLimitFilter implements Filter {
        
        // Rate limit buckets: IP -> bucket
        private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
        
        // Config
        private static final int GENERAL_LIMIT = 100; // req per minute
        private static final int LOGIN_LIMIT = 5;
        private static final int REGISTER_LIMIT = 3;
        private static final int CALCULATION_LIMIT = 30;
        private static final Duration WINDOW = Duration.ofMinutes(1);
        
        // Cleanup old buckets periodically
        private Instant lastCleanup = Instant.now();
        private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(5);
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            String ip = getClientIP(httpRequest);
            String path = httpRequest.getRequestURI();
            
            // Determine rate limit for this path
            int limit = determineLimit(path);
            String bucketKey = ip + ":" + getBucketType(path);
            
            // Get or create bucket
            RateLimitBucket bucket = buckets.computeIfAbsent(bucketKey, 
                    k -> new RateLimitBucket(limit));
            
            // Check rate limit
            if (!bucket.tryConsume()) {
                log.warn("🚫 Rate limit exceeded: IP={}, path={}, bucketKey={}", ip, path, bucketKey);
                
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(String.format(
                        "{\"error\":\"Rate limit exceeded\",\"message\":\"Zbyt wiele żądań. Poczekaj chwilę i spróbuj ponownie.\",\"retryAfter\":%d}",
                        bucket.getSecondsUntilReset()
                ));
                return;
            }
            
            // Add rate limit headers
            httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining()));
            httpResponse.setHeader("X-RateLimit-Reset", String.valueOf(bucket.getResetTime().getEpochSecond()));
            
            // Periodic cleanup
            maybeCleanup();
            
            chain.doFilter(request, response);
        }
        
        private int determineLimit(String path) {
            if (path.contains("/auth/login")) {
                return LOGIN_LIMIT;
            }
            if (path.contains("/auth/register")) {
                return REGISTER_LIMIT;
            }
            if (path.contains("/calculator/")) {
                return CALCULATION_LIMIT;
            }
            return GENERAL_LIMIT;
        }
        
        private String getBucketType(String path) {
            if (path.contains("/auth/login")) {
                return "login";
            }
            if (path.contains("/auth/register")) {
                return "register";
            }
            if (path.contains("/calculator/")) {
                return "calculator";
            }
            return "general";
        }
        
        private String getClientIP(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }
            return request.getRemoteAddr();
        }
        
        private void maybeCleanup() {
            if (Instant.now().isAfter(lastCleanup.plus(CLEANUP_INTERVAL))) {
                synchronized (this) {
                    if (Instant.now().isAfter(lastCleanup.plus(CLEANUP_INTERVAL))) {
                        Instant cutoff = Instant.now().minus(WINDOW.multipliedBy(2));
                        buckets.entrySet().removeIf(entry -> 
                                entry.getValue().getResetTime().isBefore(cutoff));
                        lastCleanup = Instant.now();
                        log.debug("🧹 Cleaned up rate limit buckets, remaining: {}", buckets.size());
                    }
                }
            }
        }
    }
    
    /**
     * Simple sliding window rate limit bucket.
     */
    public static class RateLimitBucket {
        private final int limit;
        private final AtomicInteger count;
        private volatile Instant windowStart;
        
        public RateLimitBucket(int limit) {
            this.limit = limit;
            this.count = new AtomicInteger(0);
            this.windowStart = Instant.now();
        }
        
        public synchronized boolean tryConsume() {
            Instant now = Instant.now();
            
            // Reset window if expired
            if (now.isAfter(windowStart.plus(Duration.ofMinutes(1)))) {
                windowStart = now;
                count.set(0);
            }
            
            // Check and increment
            if (count.get() < limit) {
                count.incrementAndGet();
                return true;
            }
            
            return false;
        }
        
        public int getRemaining() {
            return Math.max(0, limit - count.get());
        }
        
        public Instant getResetTime() {
            return windowStart.plus(Duration.ofMinutes(1));
        }
        
        public long getSecondsUntilReset() {
            return Math.max(0, Duration.between(Instant.now(), getResetTime()).getSeconds());
        }
    }
}
