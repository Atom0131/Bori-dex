$ErrorActionPreference = "Stop"

$body = @"
# 🎮 Bori-dex v1.5.0: The Pokémon GO Pokédex & Event Costumes Update

## 🌟 What's New in Version 1.5.0

### 1. 🔵 Authentic Pokémon GO Retro Game Cartridge
- **Custom Pixel-Art Cartridge Icon**: Distinctive 96x96 PokéStop blue & white radar icon (`pokemongo.png`).
- **Tactile Cartridge Selection**: Selectable from the green monitor cartridge picker and the Settings default edition selector.
- **Lore Integration**: Selecting Pokémon GO loads authentic GO encounter entries.

### 2. 📖 100% Pokédex Lore Coverage & Dexter Voice Narration
- Every single one of the **1,351 Pokémon** now possesses an authentic ``GO`` field entry in ``pokemon_data.json``.
- Full integration with Dexter TTS: when the Pokémon GO cartridge is active or set as preferred in settings, Dexter narrates authentic Pokémon GO field lore.

### 3. 🍬 Pokémon GO Evolution Guide
- Expanded ``evolution_guide.json`` with comprehensive Pokémon GO evolution mechanics:
  - Standard species Candy requirements (12, 25, 50, 100, 400 Candy).
  - 0-Candy Trade Evolution discounts for trade-evolution species.
  - Buddy walk distance requirements (e.g. Feebas 20km, Eevee day/night 10km).
  - Evolution Items & Lure Modules (Sinnoh Stone, Unova Stone, Mossy/Glacial/Magnetic/Rainy Lures).

### 4. 👒 72 Offline Pixel-Art Event Costumes (Normal & Shiny)
- **Squirtle Squad**: Squirtle (#007) with pointed anime sunglasses, Wartortle (#008), Blastoise (#009).
- **Latest Halloween**: Witch Hat Pikachu, Mimikyu Costume Pikachu, Spooky Festival Gengar, Pumpkaboo, Gourgeist, Pumpkin Hat Piplup, Halloween Mischief Drifblim.
- **Holiday & Christmas**: Santa Hat Pikachu & Raichu, Winter Beanie Pikachu & Raichu, Glaceon Undersea Cape, Holiday Scarf Spheal, Holiday Bells Stantler, Holiday Psyduck, Holiday Eevee.
- **New Year's & Party Celebrations**: New Year's Top Hat Pikachu, Golden Top Hat Hoothoot, 2020 Glasses Slowpoke, Party Hat Wurmple, Bulbasaur, Charmander, Eevee, Raticate, Wobbuffet.
- **Classics & Special Events**: Detective Pikachu, Ash Cap Pikachu, Straw Hat Pikachu, Flower Crown Pikachu & Eevee, Nightcap Snorlax (Pokémon Sleep), Cowboy Hat Snorlax.
- **100% Offline**: All 72 PNG assets bundled directly in the APK for instant loading without internet.

### 5. ⚡ Interactive Costume Equipping on Main Screen
- Tap ``[⚡ FORMS & COSTUMES]`` to view available costumes for any Pokémon.
- Tap **EQUIP** to immediately swap the viewport sprite on the green screen.
- Toggle **``[★ SHINY]``** to see the shiny pixel sprite variant.
- Tap the **``EQUIPPED: <NAME> ✖``** pill or the **``GO EVENT ✖``** tag to unequip anytime.

---

## 📦 Direct Downloads
- **Release APK**: [Bori-dex-v1.5.0.apk](https://github.com/Atom0131/Bori-dex/releases/download/v1.5.0/Bori-dex-v1.5.0.apk)
- **Package**: ``com.example.pokedex`` (Version ``1.5.0``, VersionCode ``5``)
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
    tag_name         = "v1.5.0"
    target_commitish = "main"
    name             = "Bori-dex v1.5.0 - Pokémon GO Pokédex, Retro Cartridge & Event Costumes"
    body             = $body
    draft            = $false
    prerelease       = $false
}

$jsonPayload = [System.Text.Encoding]::UTF8.GetBytes(($releaseData | ConvertTo-Json))

Write-Host "Creating GitHub Release v1.5.0..." -ForegroundColor Cyan
$res = Invoke-RestMethod -Uri "https://api.github.com/repos/Atom0131/Bori-dex/releases" `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $jsonPayload

Write-Host "Release created successfully: $($res.html_url)" -ForegroundColor Green

$apkPath = "app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apkPath)) {
    $apkPath = "Bori-dex-v1.5.0.apk"
}

$apkItem = Get-Item $apkPath
$assetName = "Bori-dex-v1.5.0.apk"
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
