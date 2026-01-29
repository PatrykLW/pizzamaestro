@echo off
chcp 65001 >nul
title 🍕 PizzaMaestro - Uruchamianie...
color 0F

echo.
echo   ╔═══════════════════════════════════════════════════════════╗
echo   ║                                                           ║
echo   ║     🍕  P I Z Z A M A E S T R O  🍕                      ║
echo   ║                                                           ║
echo   ║     Profesjonalny kalkulator ciasta na pizze              ║
echo   ║                                                           ║
echo   ╚═══════════════════════════════════════════════════════════╝
echo.
echo   Uruchamiam aplikacje...
echo.

cd /d "%~dp0"

powershell -ExecutionPolicy Bypass -File "%~dp0scripts\start-all.ps1"

echo.
echo   Aplikacja zostala zatrzymana.
pause
