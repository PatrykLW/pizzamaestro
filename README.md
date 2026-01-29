# 🍕 PizzaMaestro

<div align="center">

![PizzaMaestro Logo](https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400&q=80)

**Profesjonalny kalkulator ciasta na pizzę**

[![React](https://img.shields.io/badge/React-18.2-blue?logo=react)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?logo=mongodb)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-Commercial-red)](LICENSE)

[Demo](#demo) • [Instalacja](#-szybka-instalacja) • [Dokumentacja](#-dokumentacja-api) • [Kontakt](#kontakt)

</div>

---

## ✨ O projekcie

**PizzaMaestro** to profesjonalna aplikacja do obliczania receptur ciasta na pizzę, stworzona dla entuzjastów i profesjonalnych pizzaioli. Wykorzystuje zaawansowane algorytmy fermentacji oparte na modelach aktywności drożdży w różnych temperaturach.

### Kluczowe funkcje

- 🧮 **Precyzyjne kalkulacje** - algorytmy oparte na procentach piekarskich
- 🍕 **10+ stylów pizzy** - od neapolitańskiej po Detroit
- ⏰ **Inteligentny harmonogram** - automatyczne planowanie fermentacji
- 📱 **Powiadomienia SMS/Email** - przypomnienia o każdym kroku
- 📊 **Historia i statystyki** - śledź swoje postępy
- 🔥 **Dopasowanie do pieca** - piekarnik, Ooni, piec na drewno

---

## 🚀 Szybka instalacja

### Wymagania

- **Windows 10/11** (64-bit)
- **Docker Desktop** - [Pobierz](https://www.docker.com/products/docker-desktop)
- **Node.js 20+** - [Pobierz](https://nodejs.org/)
- **Java 21+** - [Pobierz](https://adoptium.net/)

### Instalacja (3 kroki)

```bash
# 1. Sklonuj lub pobierz projekt
cd C:\Users\TwojaNazwa\Desktop\pizzacalculatorproject

# 2. Uruchom instalator (podwójne kliknięcie)
INSTALL.bat

# 3. Uruchom aplikację
START-PIZZAMAESTRO.bat
```

**To wszystko!** Aplikacja otworzy się w przeglądarce.

---

## 🔑 Dane logowania

| Konto | Email | Hasło | Typ |
|-------|-------|-------|-----|
| **Admin** | admin@pizzamaestro.pl | Admin123!@# | PRO |
| **Test** | test@pizzamaestro.pl | Test123!@# | FREE |
| **Premium** | premium@pizzamaestro.pl | Premium123!@# | PREMIUM |

---

## 📱 Adresy aplikacji

| Serwis | URL |
|--------|-----|
| **Aplikacja** | http://localhost:3000 |
| **API Backend** | http://localhost:8080 |
| **Swagger API Docs** | http://localhost:8080/swagger-ui.html |
| **MongoDB GUI** | http://localhost:8081 (admin/admin123) |

---

## 💰 Model biznesowy

### Plany cenowe

| Plan | Cena | Funkcje |
|------|------|---------|
| **Starter** | 0 zł | 10 kalkulacji/msc, 5 receptur, reklamy |
| **Premium** | 29 zł/msc | Bez limitów, SMS (50/msc), bez reklam |
| **Pro** | 99 zł/msc | Multi-user, API, eksport PDF |

### Źródła przychodu

1. **Subskrypcje Premium/Pro** - główne źródło
2. **Reklamy kontekstowe** - dla użytkowników Free
3. **Partnerstwa** - Ooni, Effeuno, Caputo, Polselli
4. **Afiliacja** - linki do sprzętu i składników
5. **API dla pizzerii** - integracja z systemami POS
6. **Marketplace przepisów** - sprzedaż receptur od znanych pizzaioli
7. **Kursy online** - szkolenia wideo

---

## 🛠️ Stack technologiczny

### Backend
```
Java 21 + Spring Boot 3.2
├── Spring Security + JWT
├── Spring Data MongoDB
├── Spring Mail
├── Twilio SDK (SMS)
└── OpenAPI/Swagger
```

### Frontend
```
React 18 + TypeScript
├── Material-UI 5
├── React Query (TanStack)
├── React Hook Form
├── Zustand (state)
├── Framer Motion
└── Chart.js
```

### Infrastruktura
```
Docker + Docker Compose
├── MongoDB 7.0
├── Mongo Express (GUI)
└── Multi-stage builds
```

---

## 📁 Struktura projektu

```
pizzamaestro/
├── 📄 INSTALL.bat              # Instalator Windows
├── 📄 START-PIZZAMAESTRO.bat   # Uruchomienie
├── 📄 docker-compose.yml       # Konfiguracja Docker
├── 📄 pom.xml                  # Maven
│
├── 📁 src/main/java/com/pizzamaestro/
│   ├── 📁 config/              # Konfiguracja
│   ├── 📁 controller/          # REST API
│   ├── 📁 dto/                 # Request/Response
│   ├── 📁 model/               # Encje MongoDB
│   ├── 📁 repository/          # Repozytoria
│   ├── 📁 security/            # JWT, Auth
│   └── 📁 service/             # Logika biznesowa
│       └── 📁 strategy/        # Algorytmy fermentacji
│
├── 📁 frontend/
│   ├── 📁 public/
│   └── 📁 src/
│       ├── 📁 components/      # Komponenty React
│       ├── 📁 pages/           # Strony
│       ├── 📁 services/        # API
│       ├── 📁 store/           # Zustand
│       └── 📁 constants/       # Obrazki, stałe
│
├── 📁 scripts/                 # Skrypty PS1
└── 📁 docker/                  # Inicjalizacja MongoDB
```

---

## 📡 Dokumentacja API

### Autentykacja

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
```

### Kalkulator

```http
POST /api/calculator/public/calculate  # Publiczna kalkulacja
POST /api/calculator/calculate         # Z zapisem (auth)
GET  /api/calculator/styles            # Style pizzy
GET  /api/calculator/ovens             # Typy pieców
```

### Receptury

```http
GET    /api/recipes                    # Lista
GET    /api/recipes/{id}               # Szczegóły
POST   /api/recipes/{id}/favorite      # Dodaj do ulubionych
DELETE /api/recipes/{id}               # Usuń
```

📖 **Pełna dokumentacja:** http://localhost:8080/swagger-ui.html

---

## 🧪 Testowanie

```bash
# Testy backend
mvn test

# Testy frontend
cd frontend && npm test

# Testy E2E (Cypress - opcjonalnie)
npm run cypress
```

---

## 🌐 Deployment na telefon

### Opcja 1: Localhost przez WiFi

1. Znajdź IP komputera: `ipconfig`
2. Na telefonie otwórz: `http://192.168.x.x:3000`

### Opcja 2: Ngrok (publiczny URL)

```bash
# Zainstaluj ngrok
choco install ngrok

# Uruchom tunel
ngrok http 3000
```

### Opcja 3: Vercel/Netlify (produkcja)

```bash
# Frontend
cd frontend
npm run build
npx vercel

# Backend - Railway/Render
# Użyj docker-compose.yml
```

---

## 🐛 Rozwiązywanie problemów

### Docker nie działa
```bash
# Sprawdź status
docker info

# Uruchom Docker Desktop ręcznie
```

### Port 3000 zajęty
```bash
# Znajdź proces
netstat -ano | findstr :3000

# Zabij proces
taskkill /PID <numer> /F
```

### MongoDB nie startuje
```bash
# Sprawdź logi
docker logs pizzamaestro-mongodb

# Zrestartuj
docker-compose restart mongodb
```

---

## 📈 Roadmap

- [ ] PWA (Progressive Web App)
- [ ] Aplikacja mobilna (React Native)
- [ ] Integracja z Google Calendar
- [ ] AI - porady personalizowane
- [ ] Marketplace przepisów
- [ ] Multi-language (EN, DE, IT)

---

## 📞 Kontakt

- **Email:** kontakt@pizzamaestro.pl
- **Twitter:** @pizzamaestro
- **Discord:** discord.gg/pizzamaestro

---

<div align="center">

**Made with ❤️ and 🍕 by PizzaMaestro Team**

© 2024 PizzaMaestro. Wszelkie prawa zastrzeżone.

</div>
