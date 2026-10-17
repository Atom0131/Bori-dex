# tools/deploy-and-backup.ps1
# Deploys main testing APK and maintains stable backups

$ErrorActionPreference = "Stop"

$appDir = Resolve-Path "$PSScriptRoot\.."
$apkSource = "$appDir\app\build\outputs\apk\debug\app-debug.apk"
$backupDir = "$appDir\..\APK_BACKUPS"

if (-not (Test-Path $apkSource)) {
    Write-Error "Source APK not found at $apkSource. Run assembleDebug first!"
    exit 1
}

if (-not (Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmm"

# 1. Main testing APK destinations (Always Bori-dex.apk)
$mainDestinations = @(
    "$appDir\Bori-dex.apk",
    "$appDir\..\Bori-dex.apk"
)
if (Test-Path "$HOME\OneDrive\Desktop") { $mainDestinations += "$HOME\OneDrive\Desktop\Bori-dex.apk" }
if (Test-Path "$HOME\Downloads") { $mainDestinations += "$HOME\Downloads\Bori-dex.apk" }

foreach ($dest in $mainDestinations) {
    Copy-Item $apkSource -Destination $dest -Force
    Write-Host "  [MAIN APK] -> $dest" -ForegroundColor Cyan
}

# 2. Permanent working backups
$backupDestinations = @(
    "$backupDir\Bori-dex-LATEST-STABLE-BACKUP.apk",
    "$backupDir\Bori-dex-backup-$timestamp.apk",
    "$appDir\..\Bori-dex-WORKING-BACKUP.apk"
)
if (Test-Path "$HOME\OneDrive\Desktop") { $backupDestinations += "$HOME\OneDrive\Desktop\Bori-dex-WORKING-BACKUP.apk" }
if (Test-Path "$HOME\Downloads") { $backupDestinations += "$HOME\Downloads\Bori-dex-WORKING-BACKUP.apk" }

foreach ($dest in $backupDestinations) {
    Copy-Item $apkSource -Destination $dest -Force
    Write-Host "  [BACKUP]   -> $dest" -ForegroundColor Green
}

Write-Host "`nAll main APKs and working backups are synchronized and safe!" -ForegroundColor Yellow
