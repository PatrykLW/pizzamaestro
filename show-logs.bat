@echo off
chcp 65001 >nul
title PizzaMaestro - Przeglądarka Logów

:menu
cls
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║     📋  PIZZAMAESTRO - LOGI APLIKACJI                    ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo Dostępne pliki logów:
echo.
echo   [1] 📄 Główne logi (pizzamaestro.log)
echo   [2] ⚠️  Błędy (pizzamaestro-errors.log)
echo   [3] 🌐 API requests (pizzamaestro-api.log)
echo   [4] 🧮 Kalkulacje (pizzamaestro-calculations.log)
echo   [5] 🔒 Security (pizzamaestro-security.log)
echo   [6] 📺 Podgląd na żywo głównych logów
echo   [7] 📺 Podgląd na żywo błędów
echo   [8] 🗑️  Wyczyść wszystkie logi
echo   [9] 📂 Otwórz folder logów
echo   [0] ❌ Wyjście
echo.
set /p choice=Wybierz opcję: 

if "%choice%"=="1" goto main_log
if "%choice%"=="2" goto error_log
if "%choice%"=="3" goto api_log
if "%choice%"=="4" goto calc_log
if "%choice%"=="5" goto security_log
if "%choice%"=="6" goto live_main
if "%choice%"=="7" goto live_error
if "%choice%"=="8" goto clear_logs
if "%choice%"=="9" goto open_folder
if "%choice%"=="0" goto exit
goto menu

:main_log
cls
echo === GŁÓWNE LOGI ===
if exist logs\pizzamaestro.log (
    type logs\pizzamaestro.log | more
) else (
    echo Plik nie istnieje.
)
pause
goto menu

:error_log
cls
echo === LOGI BŁĘDÓW ===
if exist logs\pizzamaestro-errors.log (
    type logs\pizzamaestro-errors.log | more
) else (
    echo Plik nie istnieje.
)
pause
goto menu

:api_log
cls
echo === LOGI API ===
if exist logs\pizzamaestro-api.log (
    type logs\pizzamaestro-api.log | more
) else (
    echo Plik nie istnieje.
)
pause
goto menu

:calc_log
cls
echo === LOGI KALKULACJI ===
if exist logs\pizzamaestro-calculations.log (
    type logs\pizzamaestro-calculations.log | more
) else (
    echo Plik nie istnieje.
)
pause
goto menu

:security_log
cls
echo === LOGI SECURITY ===
if exist logs\pizzamaestro-security.log (
    type logs\pizzamaestro-security.log | more
) else (
    echo Plik nie istnieje.
)
pause
goto menu

:live_main
cls
echo === PODGLĄD NA ŻYWO (Ctrl+C aby przerwać) ===
powershell -Command "Get-Content -Path 'logs\pizzamaestro.log' -Wait -Tail 50"
pause
goto menu

:live_error
cls
echo === PODGLĄD BŁĘDÓW NA ŻYWO (Ctrl+C aby przerwać) ===
powershell -Command "Get-Content -Path 'logs\pizzamaestro-errors.log' -Wait -Tail 50"
pause
goto menu

:clear_logs
echo Czyszczenie logów...
if exist logs (
    del /q logs\*.log 2>nul
    echo Logi wyczyszczone!
) else (
    echo Folder logów nie istnieje.
)
pause
goto menu

:open_folder
if not exist logs mkdir logs
explorer logs
goto menu

:exit
exit
