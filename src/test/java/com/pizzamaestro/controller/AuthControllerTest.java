package com.pizzamaestro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzamaestro.dto.request.AuthRequest;
import com.pizzamaestro.model.User;
import com.pizzamaestro.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy integracyjne dla AuthController.
 * Testuje rejestrację, logowanie i odświeżanie tokenu.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_EMAIL = "authtest@example.com";
    private static final String TEST_PASSWORD = "TestPassword123";
    private static String accessToken;
    private static String refreshToken;

    @BeforeEach
    void setUp() {
        // Wyczyść użytkownika testowego przed każdym testem
    }

    @AfterAll
    static void cleanUp(@Autowired UserRepository userRepository) {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
    }

    // ==================== TESTY REJESTRACJI ====================

    @Nested
    @DisplayName("Rejestracja użytkownika")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterTests {

        @Test
        @Order(1)
        @DisplayName("Powinien zarejestrować nowego użytkownika")
        void shouldRegisterNewUser() throws Exception {
            // Najpierw usuń użytkownika jeśli istnieje
            userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);

            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Test");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.user.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.user.firstName").value("Test"))
                    .andExpect(jsonPath("$.user.accountType").value("FREE"));
        }

        @Test
        @Order(2)
        @DisplayName("Powinien odrzucić rejestrację z istniejącym emailem")
        void shouldRejectDuplicateEmail() throws Exception {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Another");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(containsString("email")));
        }

        @Test
        @DisplayName("Powinien odrzucić rejestrację bez emaila")
        void shouldRejectRegistrationWithoutEmail() throws Exception {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Test");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Powinien odrzucić rejestrację z nieprawidłowym emailem")
        void shouldRejectInvalidEmail() throws Exception {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("invalid-email");
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Test");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Powinien odrzucić rejestrację z za krótkim hasłem")
        void shouldRejectShortPassword() throws Exception {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail("newuser@example.com");
            request.setPassword("short");
            request.setFirstName("Test");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== TESTY LOGOWANIA ====================

    @Nested
    @DisplayName("Logowanie użytkownika")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class LoginTests {

        @Test
        @Order(1)
        @DisplayName("Powinien zalogować użytkownika z poprawnymi danymi")
        void shouldLoginWithValidCredentials() throws Exception {
            // Upewnij się że użytkownik istnieje
            ensureTestUserExists();

            AuthRequest.LoginRequest request = new AuthRequest.LoginRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.user.email").value(TEST_EMAIL))
                    .andReturn();

            // Zapisz tokeny do następnych testów
            String response = result.getResponse().getContentAsString();
            accessToken = objectMapper.readTree(response).get("accessToken").asText();
            refreshToken = objectMapper.readTree(response).get("refreshToken").asText();

            assertNotNull(accessToken);
            assertNotNull(refreshToken);
        }

        @Test
        @DisplayName("Powinien odrzucić logowanie z nieprawidłowym hasłem")
        void shouldRejectLoginWithWrongPassword() throws Exception {
            ensureTestUserExists();

            AuthRequest.LoginRequest request = new AuthRequest.LoginRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword("WrongPassword123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Powinien odrzucić logowanie nieistniejącego użytkownika")
        void shouldRejectLoginNonExistentUser() throws Exception {
            AuthRequest.LoginRequest request = new AuthRequest.LoginRequest();
            request.setEmail("nonexistent@example.com");
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Powinien odrzucić logowanie bez emaila")
        void shouldRejectLoginWithoutEmail() throws Exception {
            AuthRequest.LoginRequest request = new AuthRequest.LoginRequest();
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Powinien odrzucić logowanie bez hasła")
        void shouldRejectLoginWithoutPassword() throws Exception {
            AuthRequest.LoginRequest request = new AuthRequest.LoginRequest();
            request.setEmail(TEST_EMAIL);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== TESTY ODŚWIEŻANIA TOKENU ====================

    @Nested
    @DisplayName("Odświeżanie tokenu")
    class RefreshTokenTests {

        @Test
        @DisplayName("Powinien odświeżyć token z prawidłowym refresh tokenem")
        void shouldRefreshTokenWithValidRefreshToken() throws Exception {
            // Najpierw zaloguj się aby uzyskać refresh token
            ensureTestUserExists();

            AuthRequest.LoginRequest loginRequest = new AuthRequest.LoginRequest();
            loginRequest.setEmail(TEST_EMAIL);
            loginRequest.setPassword(TEST_PASSWORD);

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            String currentRefreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

            // Teraz odśwież token
            AuthRequest.RefreshTokenRequest refreshRequest = new AuthRequest.RefreshTokenRequest();
            refreshRequest.setRefreshToken(currentRefreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists());
        }

        @Test
        @DisplayName("Powinien odrzucić nieprawidłowy refresh token")
        void shouldRejectInvalidRefreshToken() throws Exception {
            AuthRequest.RefreshTokenRequest request = new AuthRequest.RefreshTokenRequest();
            request.setRefreshToken("invalid-refresh-token");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== TESTY DOSTĘPU Z TOKENEM ====================

    @Nested
    @DisplayName("Dostęp z tokenem")
    class AuthenticatedAccessTests {

        @Test
        @DisplayName("Powinien zezwolić na dostęp do chronionego zasobu z tokenem")
        void shouldAllowAccessWithValidToken() throws Exception {
            ensureTestUserExists();

            // Zaloguj się
            AuthRequest.LoginRequest loginRequest = new AuthRequest.LoginRequest();
            loginRequest.setEmail(TEST_EMAIL);
            loginRequest.setPassword(TEST_PASSWORD);

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

            // Spróbuj uzyskać dostęp do chronionego zasobu
            mockMvc.perform(get("/api/features/my-access")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Powinien odmówić dostępu bez tokenu")
        void shouldDenyAccessWithoutToken() throws Exception {
            mockMvc.perform(get("/api/features/my-access"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Powinien odmówić dostępu z nieprawidłowym tokenem")
        void shouldDenyAccessWithInvalidToken() throws Exception {
            mockMvc.perform(get("/api/features/my-access")
                            .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== POMOCNICZE ====================

    private void ensureTestUserExists() throws Exception {
        if (userRepository.findByEmail(TEST_EMAIL).isEmpty()) {
            AuthRequest.RegisterRequest request = new AuthRequest.RegisterRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Test");
            request.setLastName("User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}
