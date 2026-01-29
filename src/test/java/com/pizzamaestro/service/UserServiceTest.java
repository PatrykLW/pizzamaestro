package com.pizzamaestro.service;

import com.pizzamaestro.dto.request.AuthRequest;
import com.pizzamaestro.exception.ResourceNotFoundException;
import com.pizzamaestro.exception.UserAlreadyExistsException;
import com.pizzamaestro.model.User;
import com.pizzamaestro.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla UserService.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_EMAIL = "userservicetest@example.com";
    private static final String TEST_PASSWORD = "TestPassword123";
    private static String createdUserId;

    @BeforeEach
    void setUp() {
        // Wyczyść dane testowe przed każdym testem walidacji
    }

    @AfterAll
    static void cleanUp(@Autowired UserRepository userRepository) {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        userRepository.findByEmail("another@example.com").ifPresent(userRepository::delete);
    }

    // ==================== TESTY WALIDACJI ====================

    @Nested
    @DisplayName("Walidacja wejścia")
    class ValidationTests {

        @Test
        @DisplayName("Powinien odrzucić null request")
        void shouldRejectNullRequest() {
            assertThrows(IllegalArgumentException.class, () -> 
                    userService.registerUser(null));
        }

        @Test
        @DisplayName("Powinien odrzucić pusty email")
        void shouldRejectEmptyEmail() {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("");
            request.setPassword(TEST_PASSWORD);

            assertThrows(IllegalArgumentException.class, () -> 
                    userService.registerUser(request));
        }

        @Test
        @DisplayName("Powinien odrzucić nieprawidłowy format emaila")
        void shouldRejectInvalidEmailFormat() {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("invalid-email");
            request.setPassword(TEST_PASSWORD);

            assertThrows(IllegalArgumentException.class, () -> 
                    userService.registerUser(request));
        }

        @Test
        @DisplayName("Powinien odrzucić za krótkie hasło")
        void shouldRejectShortPassword() {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("valid@example.com");
            request.setPassword("short");

            assertThrows(IllegalArgumentException.class, () -> 
                    userService.registerUser(request));
        }

        @Test
        @DisplayName("Powinien odrzucić hasło bez dużej litery")
        void shouldRejectPasswordWithoutUppercase() {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("valid@example.com");
            request.setPassword("password123");

            assertThrows(IllegalArgumentException.class, () -> 
                    userService.registerUser(request));
        }

        @Test
        @DisplayName("Powinien odrzucić hasło bez cyfry")
        void shouldRejectPasswordWithoutDigit() {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("valid@example.com");
            request.setPassword("PasswordOnly");

            assertThrows(IllegalArgumentException.class, () -> 
                    userService.registerUser(request));
        }

        @Test
        @DisplayName("Powinien odrzucić null ID w findById")
        void shouldRejectNullId() {
            assertThrows(IllegalArgumentException.class, () -> 
                    userService.findById(null));
        }

        @Test
        @DisplayName("Powinien odrzucić pusty ID w findById")
        void shouldRejectEmptyId() {
            assertThrows(IllegalArgumentException.class, () -> 
                    userService.findById(""));
        }

        @Test
        @DisplayName("Powinien odrzucić null email w findByEmail")
        void shouldRejectNullEmail() {
            assertThrows(IllegalArgumentException.class, () -> 
                    userService.findByEmail(null));
        }
    }

    // ==================== TESTY REJESTRACJI ====================

    @Nested
    @DisplayName("Rejestracja użytkownika")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterTests {

        @Test
        @Order(1)
        @DisplayName("Powinien zarejestrować nowego użytkownika")
        void shouldRegisterNewUser() {
            // Wyczyść przed testem
            userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);

            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Test");
            request.setLastName("User");

            User user = userService.registerUser(request);

            assertNotNull(user);
            assertNotNull(user.getId());
            assertEquals(TEST_EMAIL.toLowerCase(), user.getEmail());
            assertEquals("Test", user.getFirstName());
            assertEquals("User", user.getLastName());
            assertEquals(User.AccountType.FREE, user.getAccountType());
            assertTrue(user.isEnabled());
            assertFalse(user.isEmailVerified());
            assertNotNull(user.getVerificationToken());

            createdUserId = user.getId();
        }

        @Test
        @Order(2)
        @DisplayName("Powinien odrzucić duplikat emaila")
        void shouldRejectDuplicateEmail() {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Another");
            request.setLastName("User");

            assertThrows(UserAlreadyExistsException.class, () -> 
                    userService.registerUser(request));
        }

        @Test
        @DisplayName("Powinien znormalizować email do lowercase")
        void shouldNormalizeEmailToLowercase() {
            userRepository.findByEmail("uppercase@example.com").ifPresent(userRepository::delete);

            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("UPPERCASE@EXAMPLE.COM");
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Test");
            request.setLastName("User");

            User user = userService.registerUser(request);

            assertEquals("uppercase@example.com", user.getEmail());

            // Cleanup
            userRepository.delete(user);
        }

        @Test
        @DisplayName("Powinien ustawić domyślne preferencje")
        void shouldSetDefaultPreferences() {
            userRepository.findByEmail("preferences@example.com").ifPresent(userRepository::delete);

            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("preferences@example.com");
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Pref");
            request.setLastName("Test");

            User user = userService.registerUser(request);

            assertNotNull(user.getPreferences());
            assertEquals("pl", user.getPreferences().getLanguage());

            // Cleanup
            userRepository.delete(user);
        }
    }

    // ==================== TESTY WYSZUKIWANIA ====================

    @Nested
    @DisplayName("Wyszukiwanie użytkowników")
    class FindTests {

        @BeforeEach
        void ensureUserExists() {
            if (userRepository.findByEmail(TEST_EMAIL).isEmpty()) {
                AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);
                request.setFirstName("Test");
                request.setLastName("User");
                User user = userService.registerUser(request);
                createdUserId = user.getId();
            }
        }

        @Test
        @DisplayName("Powinien znaleźć użytkownika po ID")
        void shouldFindUserById() {
            User user = userService.findById(createdUserId);

            assertNotNull(user);
            assertEquals(createdUserId, user.getId());
            assertEquals(TEST_EMAIL.toLowerCase(), user.getEmail());
        }

        @Test
        @DisplayName("Powinien rzucić wyjątek dla nieistniejącego ID")
        void shouldThrowForNonExistentId() {
            assertThrows(ResourceNotFoundException.class, () -> 
                    userService.findById("nonexistent-id-12345"));
        }

        @Test
        @DisplayName("Powinien znaleźć użytkownika po email")
        void shouldFindUserByEmail() {
            User user = userService.findByEmail(TEST_EMAIL);

            assertNotNull(user);
            assertEquals(TEST_EMAIL.toLowerCase(), user.getEmail());
        }

        @Test
        @DisplayName("Powinien znaleźć użytkownika ignorując wielkość liter")
        void shouldFindUserByEmailCaseInsensitive() {
            User user = userService.findByEmail(TEST_EMAIL.toUpperCase());

            assertNotNull(user);
            assertEquals(TEST_EMAIL.toLowerCase(), user.getEmail());
        }

        @Test
        @DisplayName("Powinien rzucić wyjątek dla nieistniejącego emaila")
        void shouldThrowForNonExistentEmail() {
            assertThrows(ResourceNotFoundException.class, () -> 
                    userService.findByEmail("nonexistent@example.com"));
        }
    }

    // ==================== TESTY STATYSTYK ====================

    @Nested
    @DisplayName("Statystyki użytkownika")
    class StatsTests {

        @BeforeEach
        void ensureUserExists() {
            if (userRepository.findByEmail(TEST_EMAIL).isEmpty()) {
                AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);
                request.setFirstName("Test");
                request.setLastName("User");
                User user = userService.registerUser(request);
                createdUserId = user.getId();
            } else {
                createdUserId = userRepository.findByEmail(TEST_EMAIL).get().getId();
            }
        }

        @Test
        @DisplayName("Powinien inkrementować licznik kalkulacji")
        void shouldIncrementCalculationCount() {
            User before = userService.findById(createdUserId);
            int initialCount = before.getUsageStats().getTotalCalculations();

            userService.incrementCalculationCount(createdUserId);

            User after = userService.findById(createdUserId);
            assertEquals(initialCount + 1, after.getUsageStats().getTotalCalculations());
            assertNotNull(after.getUsageStats().getLastCalculationAt());
        }

        @Test
        @DisplayName("Powinien sprawdzić limit kalkulacji dla FREE")
        void shouldCheckCalculationLimitForFree() {
            // Użytkownik FREE powinien mieć limit
            boolean canPerform = userService.canPerformCalculation(createdUserId, 10);
            assertTrue(canPerform); // Na początku powinien móc
        }

        @Test
        @DisplayName("Powinien aktualizować datę ostatniego logowania")
        void shouldUpdateLastLogin() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            userService.updateLastLogin(createdUserId);

            User user = userService.findById(createdUserId);
            assertNotNull(user.getLastLoginAt());
            assertTrue(user.getLastLoginAt().isAfter(before));
        }
    }

    // ==================== TESTY HASŁA ====================

    @Nested
    @DisplayName("Zarządzanie hasłem")
    class PasswordTests {

        @BeforeEach
        void ensureUserExists() {
            if (userRepository.findByEmail(TEST_EMAIL).isEmpty()) {
                AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);
                request.setFirstName("Test");
                request.setLastName("User");
                User user = userService.registerUser(request);
                createdUserId = user.getId();
            } else {
                createdUserId = userRepository.findByEmail(TEST_EMAIL).get().getId();
            }
        }

        @Test
        @DisplayName("Powinien zmienić hasło z prawidłowym obecnym hasłem")
        void shouldChangePasswordWithCorrectCurrent() {
            String newPassword = "NewPassword456";

            assertDoesNotThrow(() -> 
                    userService.changePassword(createdUserId, TEST_PASSWORD, newPassword));

            // Zmień z powrotem dla następnych testów
            userService.changePassword(createdUserId, newPassword, TEST_PASSWORD);
        }

        @Test
        @DisplayName("Powinien odrzucić zmianę hasła z nieprawidłowym obecnym hasłem")
        void shouldRejectChangePasswordWithWrongCurrent() {
            assertThrows(IllegalArgumentException.class, () -> 
                    userService.changePassword(createdUserId, "WrongPassword123", "NewPassword456"));
        }

        @Test
        @DisplayName("Powinien wygenerować token resetu hasła")
        void shouldGeneratePasswordResetToken() {
            String token = userService.generatePasswordResetToken(TEST_EMAIL);

            assertNotNull(token);
            assertFalse(token.isEmpty());

            User user = userService.findByEmail(TEST_EMAIL);
            assertEquals(token, user.getResetPasswordToken());
            assertNotNull(user.getResetPasswordExpires());
            assertTrue(user.getResetPasswordExpires().isAfter(LocalDateTime.now()));
        }
    }

    // ==================== TESTY KONWERSJI ====================

    @Nested
    @DisplayName("Konwersja do DTO")
    class ConversionTests {

        @BeforeEach
        void ensureUserExists() {
            if (userRepository.findByEmail(TEST_EMAIL).isEmpty()) {
                AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);
                request.setFirstName("Test");
                request.setLastName("User");
                User user = userService.registerUser(request);
                createdUserId = user.getId();
            } else {
                createdUserId = userRepository.findByEmail(TEST_EMAIL).get().getId();
            }
        }

        @Test
        @DisplayName("Powinien poprawnie konwertować User do UserInfo")
        void shouldConvertUserToUserInfo() {
            User user = userService.findById(createdUserId);

            var userInfo = userService.toUserInfo(user);

            assertNotNull(userInfo);
            assertEquals(user.getId(), userInfo.getId());
            assertEquals(user.getEmail(), userInfo.getEmail());
            assertEquals(user.getFirstName(), userInfo.getFirstName());
            assertEquals(user.getLastName(), userInfo.getLastName());
            assertEquals(user.getAccountType(), userInfo.getAccountType());
            assertNotNull(userInfo.getPreferences());
            assertNotNull(userInfo.getStats());
        }
    }
}
