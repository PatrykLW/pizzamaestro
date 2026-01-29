# ============================================================
# PizzaMaestro - Tworzenie skrotu na pulpicie
# ============================================================

$projectRoot = Split-Path -Parent $PSScriptRoot
$desktopPath = [Environment]::GetFolderPath("Desktop")

Write-Host "Tworzenie skrotu PizzaMaestro na pulpicie..." -ForegroundColor Cyan

# Sciezka do skryptu uruchomieniowego
$startScript = "$projectRoot\scripts\start.ps1"

# Tworz skrot
$WshShell = New-Object -ComObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut("$desktopPath\PizzaMaestro.lnk")
$Shortcut.TargetPath = "powershell.exe"
$Shortcut.Arguments = "-ExecutionPolicy Bypass -File `"$startScript`""
$Shortcut.WorkingDirectory = $projectRoot
$Shortcut.Description = "Uruchom PizzaMaestro - Kalkulator do pizzy"
$Shortcut.WindowStyle = 1

# Sprobuj ustawic ikone
$iconPath = "$projectRoot\frontend\public\favicon.ico"
if (Test-Path $iconPath) {
    $Shortcut.IconLocation = $iconPath
}

$Shortcut.Save()

Write-Host ""
Write-Host "Skrot 'PizzaMaestro' zostal utworzony na pulpicie!" -ForegroundColor Green
Write-Host "Kliknij dwukrotnie aby uruchomic aplikacje." -ForegroundColor Yellow
Write-Host ""
