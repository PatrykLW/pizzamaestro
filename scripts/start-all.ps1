# ============================================================
# PizzaMaestro - Uruchomienie wszystkich serwisów
# ============================================================

$ErrorActionPreference = "Continue"
$projectRoot = Split-Path -Parent $PSScriptRoot

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "   PIZZAMAESTRO - URUCHAMIANIE" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Funkcja sprawdzająca port
function Test-Port {
    param([int]$Port)
    $connection = New-Object System.Net.Sockets.TcpClient
    try {
        $connection.Connect("localhost", $Port)
        $connection.Close()
        return $true
    } catch {
        return $false
    }
}

# 1. Sprawdź Docker
Write-Host "[1/5] Sprawdzanie Docker..." -ForegroundColor Yellow
try {
    $dockerStatus = docker info 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Uruchamiam Docker Desktop..." -ForegroundColor Yellow
        Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe" -ErrorAction SilentlyContinue
        Write-Host "Czekam na Docker (45s)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 45
    }
    Write-Host "Docker OK!" -ForegroundColor Green
} catch {
    Write-Host "BLAD: Docker nie jest zainstalowany!" -ForegroundColor Red
    Write-Host "Pobierz z: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# 2. Uruchom MongoDB
Write-Host "[2/5] Uruchamianie MongoDB..." -ForegroundColor Yellow
Set-Location $projectRoot

docker-compose up -d mongodb 2>&1 | Out-Null
Start-Sleep -Seconds 5

if (Test-Port 27017) {
    Write-Host "MongoDB OK! (port 27017)" -ForegroundColor Green
} else {
    Write-Host "Czekam na MongoDB..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

# 3. Opcjonalnie: Mongo Express
Write-Host "[3/5] Uruchamianie Mongo Express (GUI)..." -ForegroundColor Yellow
docker-compose up -d mongo-express 2>&1 | Out-Null
Write-Host "Mongo Express: http://localhost:8081 (admin/admin123)" -ForegroundColor Cyan

# 4. Uruchom Backend
Write-Host "[4/5] Uruchamianie Backend (Spring Boot)..." -ForegroundColor Yellow
$backendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$projectRoot`" && mvn spring-boot:run -Dspring-boot.run.profiles=dev" -PassThru -WindowStyle Minimized

Write-Host "Backend uruchomiony (PID: $($backendProcess.Id))" -ForegroundColor Green
Write-Host "Czekam na start backendu (60s)..." -ForegroundColor Yellow

$timeout = 60
$elapsed = 0
while (-not (Test-Port 8080) -and $elapsed -lt $timeout) {
    Start-Sleep -Seconds 5
    $elapsed += 5
    Write-Host "  Czekam... ($elapsed s)" -ForegroundColor Gray
}

if (Test-Port 8080) {
    Write-Host "Backend OK! (port 8080)" -ForegroundColor Green
} else {
    Write-Host "Backend nie odpowiada - sprawdz logi" -ForegroundColor Yellow
}

# 5. Uruchom Frontend
Write-Host "[5/5] Uruchamianie Frontend (React)..." -ForegroundColor Yellow
Set-Location "$projectRoot\frontend"

# Sprawdź czy node_modules istnieje
if (-not (Test-Path "node_modules")) {
    Write-Host "Instaluje zaleznosci npm..." -ForegroundColor Yellow
    npm install
}

Write-Host ""
Write-Host "=============================================" -ForegroundColor Green
Write-Host "   PIZZAMAESTRO URUCHAMIANY!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Aplikacja bedzie dostepna za chwile:" -ForegroundColor Cyan
Write-Host "  Frontend:      http://localhost:3000" -ForegroundColor White
Write-Host "  Backend API:   http://localhost:8080" -ForegroundColor White
Write-Host "  Swagger:       http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "  MongoDB GUI:   http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "Dane logowania:" -ForegroundColor Cyan
Write-Host "  Admin:   admin@pizzamaestro.pl / Admin123!@#" -ForegroundColor White
Write-Host "  User:    test@pizzamaestro.pl / Test123!@#" -ForegroundColor White
Write-Host "  Premium: premium@pizzamaestro.pl / Premium123!@#" -ForegroundColor White
Write-Host ""
Write-Host "Ctrl+C aby zatrzymac" -ForegroundColor Yellow
Write-Host ""

# Poczekaj chwilę i otwórz przeglądarkę
Start-Sleep -Seconds 3
Start-Process "http://localhost:3000"

# Uruchom frontend (blokuje terminal)
npm start
