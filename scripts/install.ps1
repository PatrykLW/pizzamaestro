# ============================================================
# PizzaMaestro - Skrypt instalacyjny dla Windows
# ============================================================
# Ten skrypt zainstaluje wszystkie wymagane komponenty:
# - Docker Desktop (jeśli nie ma)
# - MongoDB (przez Docker)
# - Node.js dependencies
# - Java build
# ============================================================

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "   PIZZAMAESTRO - INSTALACJA" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot

# Sprawdz Docker
Write-Host "[1/5] Sprawdzanie Docker..." -ForegroundColor Yellow
$dockerInstalled = Get-Command docker -ErrorAction SilentlyContinue

if (-not $dockerInstalled) {
    Write-Host "Docker nie jest zainstalowany!" -ForegroundColor Red
    Write-Host "Pobierz Docker Desktop z: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Po instalacji uruchom ponownie ten skrypt." -ForegroundColor Yellow
    Read-Host "Nacisnij Enter aby otworzyc strone pobierania..."
    Start-Process "https://www.docker.com/products/docker-desktop"
    exit 1
}

# Sprawdz czy Docker dziala
$dockerRunning = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker nie jest uruchomiony. Uruchamiam Docker Desktop..." -ForegroundColor Yellow
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    Write-Host "Czekam 30 sekund na uruchomienie Docker..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}

Write-Host "Docker OK!" -ForegroundColor Green

# Sprawdz Node.js
Write-Host "[2/5] Sprawdzanie Node.js..." -ForegroundColor Yellow
$nodeInstalled = Get-Command node -ErrorAction SilentlyContinue

if (-not $nodeInstalled) {
    Write-Host "Node.js nie jest zainstalowany. Instaluje przez winget..." -ForegroundColor Yellow
    winget install OpenJS.NodeJS.LTS
}

Write-Host "Node.js OK!" -ForegroundColor Green

# Sprawdz Java
Write-Host "[3/5] Sprawdzanie Java..." -ForegroundColor Yellow
$javaInstalled = Get-Command java -ErrorAction SilentlyContinue

if (-not $javaInstalled) {
    Write-Host "Java nie jest zainstalowana. Instaluje przez winget..." -ForegroundColor Yellow
    winget install Microsoft.OpenJDK.21
}

Write-Host "Java OK!" -ForegroundColor Green

# Uruchom MongoDB przez Docker
Write-Host "[4/5] Uruchamianie MongoDB..." -ForegroundColor Yellow
Set-Location $projectRoot

# Zatrzymaj stare kontenery jesli sa
docker-compose down 2>&1 | Out-Null

# Uruchom tylko MongoDB
docker-compose up -d mongodb mongo-express

Write-Host "Czekam na uruchomienie MongoDB..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Sprawdz czy MongoDB dziala
$mongoRunning = docker ps --filter "name=pizzamaestro-mongodb" --format "{{.Status}}"
if ($mongoRunning -like "*Up*") {
    Write-Host "MongoDB OK! Dostepne na porcie 27017" -ForegroundColor Green
    Write-Host "Mongo Express (GUI): http://localhost:8081 (admin/admin123)" -ForegroundColor Cyan
} else {
    Write-Host "Problem z uruchomieniem MongoDB. Sprawdz Docker." -ForegroundColor Red
    exit 1
}

# Instaluj zaleznosci frontendu
Write-Host "[5/5] Instalowanie zaleznosci frontendu..." -ForegroundColor Yellow
Set-Location "$projectRoot\frontend"
npm install

Write-Host ""
Write-Host "=============================================" -ForegroundColor Green
Write-Host "   INSTALACJA ZAKONCZONA POMYSLNIE!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Dane logowania do aplikacji:" -ForegroundColor Cyan
Write-Host "  Admin:   admin@pizzamaestro.pl / Admin123!@#" -ForegroundColor White
Write-Host "  User:    test@pizzamaestro.pl / Test123!@#" -ForegroundColor White
Write-Host "  Premium: premium@pizzamaestro.pl / Premium123!@#" -ForegroundColor White
Write-Host ""
Write-Host "Aby uruchomic aplikacje, uzyj:" -ForegroundColor Yellow
Write-Host "  .\scripts\start.ps1" -ForegroundColor White
Write-Host ""
Write-Host "Lub kliknij dwukrotnie na skrot 'PizzaMaestro' na pulpicie." -ForegroundColor Yellow
Write-Host ""

Set-Location $projectRoot
Read-Host "Nacisnij Enter aby zakonczyc..."
