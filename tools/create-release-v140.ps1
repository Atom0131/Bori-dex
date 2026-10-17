$ErrorActionPreference = "Stop"

$body = @"
# ⚡ Bori-dex v1.4.0: The Active Scanner HUD Update

## 🎮 What's New in Version 1.4.0

### 1. ⚡ Chaotic Animated Series Laser Sweep
- **Dynamic Oscillating Scanline**: High-intensity vertical laser scanline sweeps continuously up and down across the center viewfinder with a luminous cyber-cyan gradient trail.
- **Precision Viewport Alignment**: Constrained to the 280dp center crop, maintaining 1:1 parity with the camera's 70% center ML analysis bounds.
- **Sci-Fi Sensor Telemetry**: Floating corner telemetry readouts displaying live scanning frequency (``SYS: SCAN 894MHz``), radar status (``RADAR: ACTIVE``), and FOV bounds (``FOV: 70% CROP``).

### 2. 🌀 Invizimals Radar Rings & Concentric Lock-On Progression
- **Segmented Outer Radar Ring**: Slowly rotating circular cyber-tick ring that accelerates in real-time as confidence accumulates.
- **Concentric Contracting Targeting Ring**: An inner targeting reticle that pulses and contracts toward the center as lock-on streak increases from 1 to 18 frames.
- **3-Pip Countdown Tracker**: Real-time visual progress pips (``● ○ ○`` -> ``● ● ○`` -> ``● ● ●``) corresponding to the ~2.5s acquisition countdown (``3... 2... 1...``).

### 3. 🔴 Holographic Poké Ball Viewfinder & Creature Manifestation
- **Authentic Dual-Hemisphere Glow**: Translucent red upper hemisphere and cool white lower hemisphere framed by bold cyan equator guidelines.
- **Pulsating Center Core Button**: Dynamic breathing pulse that accelerates to a rapid heartbeat during lock-on, snapping to radiant neon green (``#00FF66``) at consensus.
- **Translucent Holographic Pokémon Preview**: When a candidate Pokémon is identified with high confidence, its glowing translucent sprite materializes in the center of the Poké Ball reticle during the lock countdown before the official fanfare reveal.

### 4. 🛌 Real-World Merchandise & Sleep Band Calibration
- Fully calibrated INT8 MobileNetV2 vision pipeline capable of recognizing physical figures, plushies, and custom embroidered fabric line-art (such as the Pokémon Sleep GO Plus+ wristband).
- Zero-lag 60 FPS hardware-accelerated Canvas rendering.

---

## 📦 Release Artifacts
- **Commit**: ``df4dff8``
- **Package**: ``com.example.pokedex`` (Version ``1.4.0``, VersionCode ``4``)
- **APK**: Standalone installable release APK attached below (``Bori-dex-v1.4.0.apk``)
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
    tag_name         = "v1.4.0"
    target_commitish = "main"
    name             = "Bori-dex v1.4.0 - Active Scanner HUD & Animated Hologram"
    body             = $body
    draft            = $false
    prerelease       = $false
}

$jsonPayload = [System.Text.Encoding]::UTF8.GetBytes(($releaseData | ConvertTo-Json))

$res = Invoke-RestMethod -Uri "https://api.github.com/repos/Atom0131/Bori-dex/releases" `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $jsonPayload

Write-Host "Created Release ID: $($res.id) -> $($res.html_url)"
