$ErrorActionPreference = "Stop"

$body = @"
# 🎮 Bori-dex v1.5.1: Automatic SQLite Sync, List Filters & 1-Tap GO Quick Switch

## 🌟 What's Fixed & Added in Version 1.5.1

### 1. 🔄 Automatic Self-Healing SQLite Sync
- **The Issue**: Existing device installs cached database version 8, skipping reload of the newly bundled ``pokemon_data.json``.
- **The Fix**: Bumped Room database to ``version = 9`` and added an active check that verifies whether ``"GO"`` lore exists in SQLite. On app update/launch, the database automatically populates all 1,351 Pokémon with their Pokémon GO lore—**zero data loss, no uninstalling or cache clearing required**.

### 2. 🔵 1-Tap Pokémon GO Quick-Switch Pill
- Added a dedicated PokéStop Blue ``[GO]`` pill directly beside the ``VER: ... ▼`` cartridge selector in the Pokédex detail view.
- Single tap switches immediately to the Pokémon GO retro cartridge and lore.
- Tap again to return to default lore.

### 3. 🎯 Universal Cartridge Availability
- The ``POKÉMON GO`` cartridge is now **permanently guaranteed** to appear in the cartridge selection dialog for every single Pokémon, never filtered out.

### 4. 🏷️ Dedicated Main List Filter Tabs: ``[🔵 POKÉMON GO]`` & ``[👒 EVENT COSTUMES]``
- In the top filter chip bar on the main Pokédex list:
  - **``[🔵 POKÉMON GO]``**: Tap to filter the list instantly to all Pokémon with Pokémon GO integration.
  - **``[👒 EVENT COSTUMES]``**: Tap to filter the list to all 36+ Pokémon with event costumes (Squirtle, Pikachu, Slowpoke, Gengar, Snorlax, Spheal, Wurmple, etc.).

### 5. ⚡ Costume Badge Count Indicator
- The action button dynamically displays the exact number of costumes available for the viewed Pokémon (e.g. ``⚡ FORMS & COSTUMES (3)`` on Squirtle).

---

## 📦 Direct Downloads
- **Release APK**: [Bori-dex-v1.5.1.apk](https://github.com/Atom0131/Bori-dex/releases/download/v1.5.1/Bori-dex-v1.5.1.apk)
- **Package**: ``com.example.pokedex`` (Version ``1.5.1``, VersionCode ``6``)
"@

$credOutput = "protocol=https`nhost=github.com`n`n" | git credential fill | Out-String
$tokenLine = ($credOutput -split "`r?`n") | Where-Object { $_ -like "password=*" }
$token = $tokenLine.TrimStart("password=").Trim()

$headers = @{
    "Authorization" = "Bearer $token"
    "User-Agent"    = "BoriDex-Release"
    "Accept"        = "application/vnd.github+json"
}

$releaseData = @{
    tag_name         = "v1.5.1"
    target_commitish = "main"
    name             = "Bori-dex v1.5.1 - Pokémon GO Auto-Sync, List Filters & 1-Tap Quick Switch"
    body             = $body
    draft            = $false
    prerelease       = $false
}

$jsonPayload = [System.Text.Encoding]::UTF8.GetBytes(($releaseData | ConvertTo-Json))

Write-Host "Creating GitHub Release v1.5.1..." -ForegroundColor Cyan
$res = Invoke-RestMethod -Uri "https://api.github.com/repos/Atom0131/Bori-dex/releases" `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $jsonPayload

Write-Host "Release created successfully: $($res.html_url)" -ForegroundColor Green

$apkPath = "app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apkPath)) {
    $apkPath = "Bori-dex-v1.5.1.apk"
}

$apkItem = Get-Item $apkPath
$assetName = "Bori-dex-v1.5.1.apk"
$uploadUrl = "https://uploads.github.com/repos/Atom0131/Bori-dex/releases/$($res.id)/assets?name=$assetName"

Write-Host "Uploading $assetName ($([math]::Round($apkItem.Length / 1MB, 2)) MB)..." -ForegroundColor Cyan

$uploadHeaders = @{
    "Authorization" = "Bearer $token"
    "User-Agent"    = "BoriDex-Release"
    "Content-Type"  = "application/vnd.android.package-archive"
}

$uploadRes = Invoke-RestMethod -Uri $uploadUrl -Headers $uploadHeaders -Method Post -InFile $apkItem.FullName
Write-Host "Upload completed!" -ForegroundColor Green
Write-Host "Download URL: $($uploadRes.browser_download_url)" -ForegroundColor Yellow
