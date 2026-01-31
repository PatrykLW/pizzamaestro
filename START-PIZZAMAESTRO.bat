@echo off
chcp 65001 >nul
title PizzaMaestro - Uruchamianie...
color 0A

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    🍕 PizzaMaestro 🍕                        ║
echo ║              Uruchamianie aplikacji...                       ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

:: Ładowanie zmiennych z .env
if exist .env (
    echo [INFO] Ladowanie zmiennych srodowiskowych z .env...
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo [WARN] Plik .env nie znaleziony - uzywam wartosci domyslnych
)

:: Sprawdzenie czy MongoDB jest uruchomiony
echo.
echo [INFO] Sprawdzanie MongoDB...
docker ps 2>nul | findstr /i "mongo" >nul
if %errorlevel%==0 (
    echo [OK] MongoDB dziala w Docker
) else (
    echo [INFO] Uruchamianie MongoDB przez Docker Compose...
    docker-compose up -d mongodb 2>nul
    if %errorlevel% neq 0 (
        echo [WARN] Docker Compose nie znaleziony lub MongoDB nie w docker-compose
        echo [INFO] Sprawdz czy MongoDB jest uruchomiony na localhost:27017
    ) else (
        echo [OK] MongoDB uruchomione
        timeout /t 3 >nul
    )
)

:: Uruchomienie backendu Spring Boot
echo.
echo [INFO] Uruchamianie backendu Spring Boot (port 8080)...
start "PizzaMaestro Backend" cmd /c "cd /d "%~dp0" && mvnw.cmd spring-boot:run"

:: Czekaj na uruchomienie backendu
echo [INFO] Czekam na uruchomienie backendu...
:wait_backend
timeout /t 2 >nul
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% neq 0 (
    echo     ...backend jeszcze startuje
    goto wait_backend
)
echo [OK] Backend uruchomiony!

:: Uruchomienie frontendu React
echo.
echo [INFO] Uruchamianie frontendu React (port 3000)...
start "PizzaMaestro Frontend" cmd /c "cd /d "%~dp0%frontend" && npm start"

:: Czekaj na uruchomienie frontendu
echo [INFO] Czekam na uruchomienie frontendu...
timeout /t 10 >nul

:: Otwórz przeglądarkę
echo.
echo [INFO] Otwieram przegladarke...
start http://localhost:3000

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    🍕 PizzaMaestro 🍕                        ║
echo ║                                                              ║
echo ║   Backend:  http://localhost:8080                            ║
echo ║   Frontend: http://localhost:3000                            ║
echo ║   Swagger:  http://localhost:8080/swagger-ui.html            ║
echo ║                                                              ║
echo ║   Aby zatrzymac - zamknij okna terminali                     ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

pause
