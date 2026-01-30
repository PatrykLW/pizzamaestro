## Opis zmian / Description

<!-- Krótki opis tego, co zostało zmienione -->

## Typ zmiany / Type of change

- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] 📝 Documentation update
- [ ] 🔧 Configuration change
- [ ] ♻️ Refactoring (no functional changes)
- [ ] 🧪 Tests (adding or updating tests)

## Checklist

### Ogólne / General
- [ ] Kod kompiluje się bez błędów
- [ ] Testy przechodzą lokalnie
- [ ] Kod jest zgodny ze stylem projektu (linting)
- [ ] Dokumentacja została zaktualizowana (jeśli potrzeba)

### Backend (Java/Spring)
- [ ] Brak problemów N+1 w nowych zapytaniach
- [ ] Dodano odpowiednie `@Transactional` gdzie potrzeba
- [ ] Walidacja inputu (`@Valid`, `@NotNull`, etc.)
- [ ] Testy jednostkowe dla nowej logiki

### Frontend (React/TypeScript)
- [ ] Brak błędów TypeScript (`npm run type-check`)
- [ ] Brak błędów ESLint (`npm run lint`)
- [ ] React Hooks są używane poprawnie
- [ ] UI jest responsywne

### Security
- [ ] Brak hardcoded secrets
- [ ] Sprawdzono uprawnienia użytkowników
- [ ] Input jest walidowany

## Screenshots (jeśli dotyczy UI)

<!-- Dodaj screenshoty przed/po jeśli są zmiany w UI -->

## Dodatkowe informacje

<!-- Dodatkowy kontekst, linki do issues, itp. -->
