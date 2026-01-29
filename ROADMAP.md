# PizzaMaestro - Roadmap i Analiza Projektu

**Ostatnia aktualizacja:** 29 stycznia 2026

## 1. PODSUMOWANIE WYMAGAŃ

### ✅ Zrealizowane wymagania:
- [x] Kalkulator ciasta z Baker's Math
- [x] Obliczanie ilości drożdży z uwzględnieniem temperatury (Arrhenius)
- [x] Różne style pizzy (Neapolitan, NY, Roman, Detroit, Sicilian, Focaccia, Thin Crust, Tavern, Pinsa)
- [x] Różne metody fermentacji (pokojowa, chłodnicza, mieszana, same-day)
- [x] Prefermenty (poolish, biga, zakwas)
- [x] Rozbudowana baza składników (27 mąk, 15 wód, 6 drożdży, 6 soli)
- [x] System użytkowników (FREE, PREMIUM, PRO, ADMIN)
- [x] JWT Authentication z refresh tokens
- [x] Integracja pogodowa (Open-Meteo API)
- [x] Harmonogram fermentacji
- [x] Zapisywanie receptur
- [x] Logowanie szczegółowe (Logback, wielu appenderów)
- [x] MongoDB + Docker
- [x] **Rate limiting dla API** ✨ NOWE
- [x] **Baza wiedzy o technikach** ✨ NOWE
- [x] **Interaktywne tipy podczas kalkulacji (TipEngine)** ✨ NOWE
- [x] **Indeksy MongoDB dla wydajności** ✨ NOWE
- [x] **Rozbudowana obsługa błędów (GlobalExceptionHandler)** ✨ NOWE

### 🔄 W trakcie / Do zrealizowania:
- [ ] Weryfikacja email
- [ ] SMS notifications (Twilio)
- [ ] Eksport do PDF
- [ ] Panel admina
- [ ] Testy jednostkowe i E2E
- [ ] PWA / Offline mode
- [ ] Ciemny motyw

---

## 2. ULEPSZENIA BACKENDOWE

### 2.1 Bezpieczeństwo (WYSOKI PRIORYTET)
- [ ] Rate limiting (bucket4j lub resilience4j)
- [ ] Walidacja siły hasła
- [ ] Blacklista tokenów JWT przy wylogowaniu
- [ ] Blokada konta po X nieudanych próbach
- [ ] CORS konfiguracja z application.properties

### 2.2 Wydajność (ŚREDNI PRIORYTET)
- [ ] Cache dla składników i stylów (Spring Cache + Redis/Caffeine)
- [ ] Indeksy MongoDB dla często wyszukiwanych pól
- [ ] Paginacja dla wszystkich list
- [ ] Retry logic dla zewnętrznych API

### 2.3 Jakość kodu (ŚREDNI PRIORYTET)
- [ ] Refaktoryzacja DoughCalculatorService (podzielić na mniejsze)
- [ ] Przeniesienie hardcoded wartości do konfiguracji
- [ ] Walidacja zależności między polami w DTO
- [ ] Osobne DTO dla response (nie używać modeli)

### 2.4 Nowe funkcje (NISKI PRIORYTET)
- [ ] Email service (reset hasła, weryfikacja)
- [ ] SMS service (Twilio)
- [ ] Admin endpoints
- [ ] API versioning (/api/v1/)

---

## 3. ULEPSZENIA FRONTENDOWE

### 3.1 UX/UI (WYSOKI PRIORYTET)
- [ ] Interaktywne tipy podczas tworzenia przepisu
- [ ] Podział CalculatorPage na komponenty
- [ ] Dark mode toggle
- [ ] Loading states i skeleton loaders
- [ ] Error boundaries

### 3.2 Nowe funkcje (ŚREDNI PRIORYTET)
- [ ] Baza wiedzy o technikach (biga, poolish, składanie, kulkowanie)
- [ ] Porównanie receptur
- [ ] Eksport do PDF
- [ ] Offline mode (PWA)

### 3.3 Jakość (NISKI PRIORYTET)
- [ ] SEO optimization
- [ ] Accessibility (ARIA)
- [ ] Internationalization (i18n)
- [ ] Testy E2E

---

## 4. BAZA WIEDZY - SZCZEGÓŁY

### 4.1 Techniki prefmentów:
- **Poolish**: 100% hydratacji, 12-18h fermentacji
- **Biga**: 50-60% hydratacji, 16-24h fermentacji
- **Lievito Madre**: zakwas pszenny, karmienie co 4-8h

### 4.2 Techniki składania ciasta:
- Stretch and fold
- Coil fold
- Slap and fold
- Letter fold
- Lamination

### 4.3 Techniki kulkowania:
- Metoda włoska (piegatura)
- Metoda pre-shape + final shape
- Metoda napinania powierzchni

### 4.4 Parametry mąk:
- W (siła) - znaczenie i zastosowanie
- P/L - elastyczność vs rozciągliwość
- Falling number - aktywność enzymatyczna
- Ash content - typ mąki

---

## 5. INTERAKTYWNE TIPY - KONCEPCJA

### Kiedy wyświetlać:
1. **Zmiana stylu** → "Neapolitańska wymaga W280-320, hydratacji 60-65%"
2. **Zmiana hydratacji** → "Zwiększenie hydratacji = bardziej puszyste ciasto, ale trudniejsze w obsłudze"
3. **Zmiana czasu fermentacji** → "Krótszy czas = więcej drożdży, mniej smaku. Dłuższy = więcej aromatu"
4. **Wybór mąki** → "Caputo Pizzeria ma W260-270, idealna dla 24h fermentacji"
5. **Temperatura** → "Każde 5°C zmienia czas fermentacji o ~50%"

### Format tipów:
```
💡 TIP: [Tytuł]
[Wyjaśnienie]
[Zalecenie]
```

---

## 6. PLAN IMPLEMENTACJI

### Faza 1: Stabilizacja (teraz)
1. Rate limiting
2. Walidacja danych
3. Error handling

### Faza 2: Baza wiedzy
1. Model TechniqueGuide
2. Endpointy API
3. Strona z przewodnikami

### Faza 3: Interaktywne tipy
1. TipEngine service
2. Komponent TipDisplay
3. Integracja z kalkulatorem

### Faza 4: Polish
1. Dark mode
2. PDF export
3. PWA

---

## 7. METRYKI SUKCESU

- Czas ładowania strony < 2s
- Brak błędów 500 w logach
- 100% pokrycie walidacji
- 80%+ pokrycie testami
- Lighthouse score > 90
