# Changelog - PizzaMaestro

## [2.1.0] - 2026-01-29 - Przegląd i Dopracowanie

### POPRAWKI WALIDACJI

#### Backend - Kontrolery
- **WeatherController** - dodano walidację współrzędnych geograficznych (@DecimalMin/@DecimalMax)
- **TipController** - dodano @Valid do request body, walidację parametrów
- **IngredientController** - dodano walidację wyszukiwania i parametrów białka
- Wszystkie kontrolery mają teraz @Validated na poziomie klasy

#### Backend - Serwisy
- **WeatherService** - walidacja współrzędnych (-90 do 90 lat, -180 do 180 lon)
- **UserService** - walidacja email (format, długość), hasła (siła, złożoność)
- Wszystkie metody serwisów mają dokumentację JavaDoc

### LOGOWANIE
- **SecurityConfig** - dodano szczegółowe logowanie konfiguracji (@PostConstruct)
- Logowanie publicznych endpointów i konfiguracji CORS
- Dodano logowanie debug w kontrolerach (TipController, WeatherController)

### POPRAWKI TYPESCRIPT (Frontend)
- Utworzono centralny plik typów `types/index.ts`
- Usunięto 18+ wystąpień `any` z kodu
- Dodano typy dla: PizzaStyle, OvenType, YeastType, FermentationMethod, Recipe
- Poprawiono typy odpowiedzi API w `services/api.ts`
- Dodano loading states do StylesGuidePage i ProfilePage
- Poprawiono obsługę błędów w LoginPage i RegisterPage (AxiosError)

### NOWE TESTY
- **AuthControllerTest** - testy integracyjne autentykacji:
  - Rejestracja (poprawna, duplikat email, walidacja)
  - Logowanie (poprawne, błędne hasło, nieistniejący user)
  - Refresh token
  - Dostęp z tokenem/bez tokenu
- **UserServiceTest** - testy jednostkowe:
  - Walidacja email i hasła
  - Rejestracja użytkownika
  - Wyszukiwanie po ID/email
  - Statystyki i limity
  - Zarządzanie hasłem
- **WeatherServiceTest** - testy jednostkowe:
  - Walidacja współrzędnych
  - Walidacja nazwy miasta
  - Pobieranie danych pogodowych
  - Obliczanie wpływu na fermentację

### TECHNICZNE
- Backend kompiluje się bez błędów
- Frontend kompiluje się bez błędów TypeScript
- Testy jednostkowe i integracyjne kompilują się poprawnie

---

## [2.0.0] - 2026-01-29 - Wielka aktualizacja

### NOWE FUNKCJE

#### 🧠 Interaktywne Wskazówki (TipEngine)
- **TipEngineService** - silnik generowania kontekstowych wskazówek
- Tipy przy zmianie każdego parametru (hydratacja, fermentacja, mąka, temp.)
- Ostrzeżenia o potencjalnych problemach (np. za wysoka hydratacja dla słabej mąki)
- Rekomendacje optymalizacyjne
- Wyjaśnienia naukowe (chemia fermentacji, gluten)
- **TipController** - REST API dla wskazówek
- **TipDisplay** - komponent React do wyświetlania wskazówek

#### 📚 Baza Wiedzy
- **TechniqueGuide** - model przewodników po technikach
- **TechniqueGuideService** - zarządzanie przewodnikami
- **KnowledgeBaseController** - API bazy wiedzy
- **KnowledgeBasePage** - strona React z przewodnikami
- Szczegółowe przewodniki:
  - Poolish (polski preferment)
  - Biga (włoski preferment)
  - Zakwas (lievito madre)
  - Stretch and Fold
  - Coil Fold
  - Slap and Fold
  - Kulkowanie (ball shaping)
  - Pre-shape
  - Rozciąganie ręczne
  - Fermentacja zimna
- Szybkie przewodniki: siła mąki (W), przeliczniki drożdży, hydratacja

#### 🔒 Bezpieczeństwo
- **Rate Limiting** - ochrona przed nadużyciami:
  - Login: 5 req/min
  - Register: 3 req/min
  - Kalkulacje: 30 req/min
  - Ogólne API: 100 req/min
- Rozbudowana obsługa błędów (GlobalExceptionHandler):
  - ConstraintViolationException
  - AccessDeniedException
  - HttpRequestMethodNotSupportedException
  - MissingServletRequestParameterException
  - MethodArgumentTypeMismatchException
  - HttpMessageNotReadableException

#### 📇 Wydajność
- **MongoIndexConfig** - automatyczne tworzenie indeksów:
  - Users: email (unique), accountType, lastLoginAt
  - Recipes: userId+createdAt, pizzaStyle, favorites
  - Ingredients: type+active, name, brand
  - TechniqueGuides: slug (unique), category, viewCount
  - Notifications: userId+read, TTL 30 dni

#### 🐳 Docker
- **application-docker.yml** - profil produkcyjny
- **.dockerignore** - optymalizacja budowania obrazów
- **.env.example** - szablon konfiguracji

### ULEPSZENIA

#### Backend
- Lepsza walidacja w CalculationRequest
- Rozbudowane logowanie błędów z path
- Cache dla często używanych danych
- Retry logic dla API pogodowego

#### Frontend
- **useDebounce** hook do optymalizacji wywołań API
- Link do bazy wiedzy w nawigacji
- Komponenty TipDisplay dla interaktywnych wskazówek

### PLIKI DODANE/ZMIENIONE

#### Nowe pliki:
```
src/main/java/com/pizzamaestro/
├── config/
│   ├── RateLimitingConfig.java
│   ├── MongoIndexConfig.java
│   └── TechniqueDataInitializer.java
├── controller/
│   ├── TipController.java
│   └── KnowledgeBaseController.java
├── model/
│   └── TechniqueGuide.java
├── repository/
│   └── TechniqueGuideRepository.java
└── service/
    ├── TipEngineService.java
    └── TechniqueGuideService.java

src/main/resources/
└── application-docker.yml

frontend/src/
├── components/Calculator/
│   └── TipDisplay.tsx
├── hooks/
│   └── useDebounce.ts
├── pages/
│   └── KnowledgeBasePage.tsx
└── services/
    └── api.ts (rozszerzone o tipApi, knowledgeApi)

Root:
├── .dockerignore
├── .env.example
├── ROADMAP.md
└── CHANGELOG.md
```

---

## [1.5.0] - Wcześniejsze zmiany

### Funkcje
- Zaawansowane obliczenia DDT (Desired Dough Temperature)
- Integracja pogodowa (Open-Meteo API)
- System poziomów użytkowników (FREE, PREMIUM, PRO)
- Szczegółowe logowanie (Logback)
- WeatherService i WeatherController
- FeatureAccessService

### Baza składników
- 27 różnych mąk z parametrami W, białko
- 15 rodzajów wody z twardością i pH
- 6 typów drożdży
- 6 rodzajów soli

---

## Co dalej?

### Priorytet wysoki:
1. Integracja TipDisplay z CalculatorPage
2. Testy jednostkowe dla TipEngineService
3. Weryfikacja email przy rejestracji

### Priorytet średni:
1. Eksport receptur do PDF
2. Panel administracyjny
3. SMS notifications (Twilio)
4. Ciemny motyw

### Priorytet niski:
1. PWA / Offline mode
2. Integracja z kalendarzem
3. Analiza kosztów składników
4. Social features (udostępnianie receptur)
