@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║     PIZZAMAESTRO - NAPRAWA I URUCHAMIANIE                 ║
echo ╠═══════════════════════════════════════════════════════════╣
echo ║  Ten skrypt naprawi problemy z OneDrive/node_modules      ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

cd /d "c:\Users\patry\OneDrive\Pulpit\pizzacalculatorproject"

echo [1/6] Zatrzymuję istniejące procesy...
taskkill /F /IM node.exe 2>nul
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak > nul

echo [2/6] Sprawdzanie Docker i MongoDB...
docker info > nul 2>&1
if errorlevel 1 (
    echo BLAD: Docker nie jest uruchomiony!
    echo Uruchom Docker Desktop i sprobuj ponownie.
    pause
    exit /b 1
)

docker ps | findstr "mongo" > nul 2>&1
if errorlevel 1 (
    echo Uruchamiam MongoDB...
    docker-compose up -d mongo mongo-express
    timeout /t 5 /nobreak > nul
)
echo MongoDB OK!

echo [3/6] Usuwam uszkodzone node_modules (OneDrive fix)...
cd frontend

:: Zamknij procesy które mogą blokować pliki
taskkill /F /IM node.exe 2>nul
timeout /t 2 /nobreak > nul

:: Wyłącz OneDrive sync dla node_modules
if exist "node_modules" (
    echo     Usuwam node_modules...
    cmd /c "rmdir /s /q node_modules" 2>nul
    
    :: Jeśli rmdir nie zadziałało, użyj robocopy trick
    if exist "node_modules" (
        echo     Uzywam alternatywnej metody usuwania...
        mkdir empty_dir_temp 2>nul
        robocopy empty_dir_temp node_modules /mir /r:0 /w:0 > nul 2>&1
        rmdir /s /q empty_dir_temp 2>nul
        rmdir /s /q node_modules 2>nul
    )
)

if exist "package-lock.json" (
    echo     Usuwam package-lock.json...
    del /f /q package-lock.json 2>nul
)

if exist ".cache" (
    echo     Usuwam .cache...
    rmdir /s /q .cache 2>nul
)

echo [4/6] Czyszcze npm cache...
call npm cache clean --force > nul 2>&1

echo [5/6] Instaluje zaleznosci (to moze potrwac)...
echo     npm install --legacy-peer-deps
call npm install --legacy-peer-deps

if errorlevel 1 (
    echo BLAD: npm install nie powiodl sie!
    echo.
    echo ROZWIAZANIE: Przenieś projekt POZA folder OneDrive!
    echo OneDrive powoduje problemy z synchronizacja node_modules.
    echo.
    echo Sugerowana lokalizacja: C:\projekty\pizzamaestro
    pause
    exit /b 1
)

echo [6/6] Uruchamiam aplikacje...
cd ..

:: Uruchom backend w tle
echo Uruchamiam Backend...
start "PizzaMaestro Backend" cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=dev"

:: Czekaj na backend
echo Czekam na start backendu (30s)...
set /a counter=0
:wait_backend
set /a counter+=5
timeout /t 5 /nobreak > nul
curl -s http://localhost:8080/api/health > nul 2>&1
if not errorlevel 1 goto backend_ok
if %counter% GEQ 60 (
    echo Backend startuje - sprawdz logi w osobnym oknie
    goto start_frontend
)
echo   Czekam... (%counter% s)
goto wait_backend

:backend_ok
echo Backend OK!

:start_frontend
echo Uruchamiam Frontend...
cd frontend
start "PizzaMaestro Frontend" cmd /c "npm start"
cd ..

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║           PIZZAMAESTRO URUCHOMIONY!                       ║
echo ╠═══════════════════════════════════════════════════════════╣
echo ║  Frontend:    http://localhost:3000                       ║
echo ║  Backend:     http://localhost:8080                       ║
echo ║  Swagger:     http://localhost:8080/swagger-ui.html       ║
echo ║  MongoDB:     http://localhost:8081 (admin/admin123)      ║
echo ╠═══════════════════════════════════════════════════════════╣
echo ║  LOGOWANIE:                                               ║
echo ║    Admin:   admin@pizzamaestro.pl / Admin123!@#           ║
echo ║    Premium: premium@pizzamaestro.pl / Premium123!@#       ║
echo ║    User:    test@pizzamaestro.pl / Test123!@#             ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo Logi backendu sa w oddzielnym oknie konsoli.
echo Ctrl+C w tym oknie aby zatrzymac.
echo.
pause
