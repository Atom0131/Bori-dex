# 🚀 Release Notes - Bori-dex v1.1.0

**Release Date:** October 13, 2026  
**Platform:** Android (API 24 to API 36)  
**Binary:** `app-release.apk`

---

## What's New in v1.1.0

### 🌟 Key Enhancements

1. **Tabbed Settings Menu (Zero Scrolling Clutter)**:
   - Completely redesigned settings into 3 dedicated, organized tabs:
     - **`VOICE`**: Voice Category, Profile / Accents, 90s Hardware DSP Filter, Voice Pitch, and Speech Speed.
     - **`SOUND & SCAN`**: Scanning Preset, Commercial Fanfare, Voice Callout, Cry on Reveal, Auto-Cry, and Auto-Read.
     - **`GAME VERSION`**: Prioritized generation release selection.
   - Quick `✕` close button in header alongside bottom `CLOSE` and `TEST VOICE` action buttons.

2. **Grouped & Paired Game Releases**:
   - Replaced lengthy individual game version dropdowns with paired releases (`Red / Blue`, `Gold / Silver`, `Ruby / Sapphire`, `Diamond / Pearl`, `Black / White`, `X / Y`, `Sun / Moon`, `Sword / Shield`, `Legends: Arceus`, `Scarlet / Violet`).
   - Automatically prioritizes entries matching your chosen generation pair across all Pokémon.

3. **Directional Auto-Speech (Forward Only)**:
   - When advancing forward (`▶` next or picking a Pokémon from list/search), Dexter auto-reads the entry.
   - When navigating backward (`◀` previous or Back button), speech is automatically silenced so moving through history remains smooth and quiet.

4. **Dedicated `🗣 SPEAK` Replay Button**:
   - Added a prominent `🗣 SPEAK` action button right next to `🔊 CRY` on the Pokédex result screen to easily replay/hear Dexter's narration on demand anytime.

5. **Human-Friendly 54dp Page-Turn Controls**:
   - High-contrast 54dp page-turn arrow buttons (`◀` / `▶`) positioned at the screen edges for effortless one-handed browsing without blocking Pokémon art.

---

# 🚀 Release Notes - Bori-dex v1.0.0

**Release Date:** October 13, 2026  
**Platform:** Android (API 24 to API 36)  
**Binary:** `app-release.apk`

---

## What's New in v1.0.0

Welcome to the initial open-source release of **Bori-dex**, an authentic, offline-first AI Pokédex application for Android!

### 🌟 Highlights

1. **On-Device AI Computer Vision Scanner**:
   - Point your camera at Pokémon to identify them instantly using an embedded **TensorFlow Lite** classification model.
   - Works **100% offline** with CameraX, ensuring immediate responsiveness without any internet connection.

2. **Authentic 1997 Anime "Dexter" Voice**:
   - Tuned energetic robotic tenor voice matching the original 1997 animated series Pokédex (`pitch = 1.15X`, `speed = 1.12X`).
   - Integrated real-time **hardware DSP filter** simulating vintage 90s handheld hardware with a dual-tap metallic comb echo (12ms / 26ms) and 2kHz horn mid-EQ peak.
   - Multiple preset categories: **Retro 90's Anime**, **Modern High-End (HD)**, and **Custom Voices & Accents** with full support for international accents (American, British, Australian, Indian, Irish, etc.).

3. **"Who's That Pokémon?!" Commercial Transition**:
   - Signature 6-note anime commercial bumper arpeggio (`C5 -> E5 -> G5 -> C6 -> E6 -> G6`) in uncompressed 44.1kHz 16-bit PCM WAV.
   - Retro silhouette bouncing animation with drop shadows.

4. **Complete Multi-Generation Pokédex**:
   - Supports Gen 1 through Gen 9 with region filtering (Kanto, Johto, Hoenn, Sinnoh, Unova, Kalos, Alola, Galar, Paldea).
   - Normal and Shiny sprite toggles, alternate Mega/regional forms, evolution chains, and multi-version game flavor text selectors (Red, Blue, Gold, etc.).
   - Built-in phonetic pronunciation engine ensuring accurate names (e.g. *Venusaur*, *Rayquaza*, *Arceus*).

5. **Adaptive Edge-to-Edge UI & Scroll Memory**:
   - Full support for Android 15/16 status bar and navigation bar insets, ensuring camera punch-holes and status bar clocks never overlap UI buttons.
   - Scroll position persistence that remembers your exact position in the list when returning from viewing a Pokémon.
   - 16 KB page-aligned native builds ready for Android 15+.

---

## 📦 APK Details
- **Package Name**: `com.example.pokedex`
- **Application Name**: `Bori-dex`
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 16 (API 36)
- **License**: Apache 2.0
