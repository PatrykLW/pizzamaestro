@echo off
title PizzaMaestro
cd /d "C:\Users\patry\OneDrive\Pulpit\pizzacalculatorproject"

echo.
echo === PizzaMaestro - Uruchamianie ===
echo.

echo [1/3] Uruchamiam MongoDB...
docker-compose up -d mongodb
timeout /t 5 /nobreak >nul

echo [2/3] Uruchamiam Backend (Spring Boot)...
start "Backend" cmd /k "cd /d C:\Users\patry\OneDrive\Pulpit\pizzacalculatorproject && mvn spring-boot:run"

echo Czekam na backend...
:czekaj
timeout /t 5 /nobreak >nul
curl -s http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 goto czekaj
echo Backend dziala!

echo [3/3] Uruchamiam Frontend (React)...
start "Frontend" cmd /k "cd /d C:\Users\patry\OneDrive\Pulpit\pizzacalculatorproject\frontend && npm start"

timeout /t 10 /nobreak >nul

echo Otwieram przegladarke...
start http://localhost:3000

echo.
echo === Aplikacja uruchomiona! ===
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000
echo.
pause
