@echo off
chcp 65001 >nul
title 🍕 PizzaMaestro - Instalacja
color 0E

echo.
echo   ╔═══════════════════════════════════════════════════════════╗
echo   ║                                                           ║
echo   ║     🍕  P I Z Z A M A E S T R O  -  I N S T A L A C J A  ║
echo   ║                                                           ║
echo   ╚═══════════════════════════════════════════════════════════╝
echo.
echo   Ten instalator:
echo.
echo     ✓ Sprawdzi wymagane oprogramowanie
echo     ✓ Uruchomi baze danych MongoDB (Docker)
echo     ✓ Zainstaluje zaleznosci projektu
echo     ✓ Utworzy skrot na pulpicie
echo.
echo   Wymagania:
echo     • Docker Desktop
echo     • Node.js 18+
echo     • Java 17+ (lub Maven zainstaluje automatycznie)
echo.
echo   ─────────────────────────────────────────────────────────────
echo.

pause

cd /d "%~dp0"

echo.
echo   [1/3] Instalacja zaleznosci...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0scripts\install.ps1"

echo.
echo   [2/3] Tworzenie skrotu na pulpicie...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0scripts\create-shortcut.ps1"

echo.
echo   [3/3] Gotowe!
echo.
echo   ╔═══════════════════════════════════════════════════════════╗
echo   ║                                                           ║
echo   ║     ✅  INSTALACJA ZAKONCZONA POMYSLNIE!                  ║
echo   ║                                                           ║
echo   ╚═══════════════════════════════════════════════════════════╝
echo.
echo   Aby uruchomic aplikacje:
echo.
echo     • Kliknij dwukrotnie plik: START-PIZZAMAESTRO.bat
echo     • Lub uzyj skrotu "PizzaMaestro" na pulpicie
echo.
echo   Dane logowania:
echo     Admin:   admin@pizzamaestro.pl / Admin123!@#
echo     User:    test@pizzamaestro.pl / Test123!@#
echo     Premium: premium@pizzamaestro.pl / Premium123!@#
echo.

pause
