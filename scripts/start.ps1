# ============================================================
# PizzaMaestro - Skrypt uruchomieniowy
# ============================================================

$ErrorActionPreference = "SilentlyContinue"

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "   PIZZAMAESTRO - URUCHAMIANIE" -ForegroundColor Cyan  
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot

# Sprawdz czy Docker dziala
Write-Host "[1/4] Sprawdzanie Docker..." -ForegroundColor Yellow
$dockerRunning = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Uruchamiam Docker Desktop..." -ForegroundColor Yellow
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe" -ErrorAction SilentlyContinue
    Write-Host "Czekam na uruchomienie Docker (30s)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}
Write-Host "Docker OK!" -ForegroundColor Green

# Uruchom MongoDB
Write-Host "[2/4] Uruchamianie MongoDB..." -ForegroundColor Yellow
Set-Location $projectRoot
docker-compose up -d mongodb mongo-express

Start-Sleep -Seconds 5

$mongoRunning = docker ps --filter "name=pizzamaestro-mongodb" --format "{{.Status}}"
if ($mongoRunning -like "*Up*") {
    Write-Host "MongoDB OK!" -ForegroundColor Green
} else {
    Write-Host "Uruchamiam MongoDB ponownie..." -ForegroundColor Yellow
    docker-compose up -d mongodb mongo-express
    Start-Sleep -Seconds 10
}

# Uruchom Backend w tle
Write-Host "[3/4] Uruchamianie Backend (Spring Boot)..." -ForegroundColor Yellow
$backendJob = Start-Job -ScriptBlock {
    param($root)
    Set-Location $root
    $env:SPRING_PROFILES_ACTIVE = "dev"
    $env:MONGODB_URI = "mongodb://localhost:27017/pizzamaestro"
    & mvn spring-boot:run 2>&1
} -ArgumentList $projectRoot

Write-Host "Backend uruchomiony w tle (PID: $($backendJob.Id))" -ForegroundColor Green
Write-Host "Czekam na start backendu (30s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Uruchom Frontend
Write-Host "[4/4] Uruchamianie Frontend (React)..." -ForegroundColor Yellow
Set-Location "$projectRoot\frontend"

# Otworz przegladarke
Write-Host ""
Write-Host "=============================================" -ForegroundColor Green
Write-Host "   PIZZAMAESTRO URUCHOMIONY!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Aplikacja:" -ForegroundColor Cyan
Write-Host "  Frontend:     http://localhost:3000" -ForegroundColor White
Write-Host "  Backend API:  http://localhost:8080" -ForegroundColor White
Write-Host "  Swagger:      http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "  Mongo Express: http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "Dane logowania:" -ForegroundColor Cyan
Write-Host "  Admin:   admin@pizzamaestro.pl / Admin123!@#" -ForegroundColor White
Write-Host "  User:    test@pizzamaestro.pl / Test123!@#" -ForegroundColor White
Write-Host ""
Write-Host "Nacisnij Ctrl+C aby zatrzymac aplikacje." -ForegroundColor Yellow
Write-Host ""

# Otworz przegladarke po chwili
Start-Sleep -Seconds 5
Start-Process "http://localhost:3000"

# Uruchom frontend (to zablokuje terminal)
npm start
