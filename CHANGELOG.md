# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-10-13

### Added
- Tabbed settings modal layout separating options into `VOICE`, `SOUND & SCAN`, and `GAME VERSION` to eliminate scrolling.
- Paired game version preference (`Red / Blue`, `Ruby / Sapphire`, `Sword / Shield`, etc.) prioritizing era-appropriate entry data.
- Dedicated `🗣 SPEAK` replay button on entry detail screen.
- Edge-placed 54dp page-turn arrow buttons (`◀` / `▶`) for human-friendly navigation.
- Directional auto-speech control: plays when moving forward, automatically silences when navigating backward.
- Quick `✕` close button on settings dialog.

## [1.0.0] - 2026-10-13

### Added
- Real-time on-device TensorFlow Lite computer vision scanner with CameraX.
- Authentic 1997 anime Dexter male voice calibration (`pitch = 1.15X`, `speed = 1.12X`).
- Real-time hardware DSP comb filter (dual-tap 12ms/26ms metallic echo + 2kHz horn mid-EQ).
- Signature 6-note "Who's That Pokémon?!" anime arpeggio commercial bumper in 44.1kHz PCM WAV.
- Clean 2-tier voice settings menu with dropdowns for Retro, Modern HD, and Custom Voices with international accents.
- Pokédex index with Gen 1-9 support, regional filters, and search.
- Normal and Shiny pixel sprite toggles, alternate form chips (Mega evolutions, regional variants).
- Multi-game Pokédex entry selector with game version dropdowns (Red, Blue, Gold, Silver, etc.).
- Phonetic pronunciation dictionary resolving tricky names (Venusaur, Rayquaza, Arceus, etc.).
- List scroll position persistence retaining exact list position upon returning from detail views.
- Safe area system bar insets (`statusBarsPadding()`, `navigationBarsPadding()`) preventing camera punch-hole and clock clipping.
- Apache 2.0 open-source licensing.
