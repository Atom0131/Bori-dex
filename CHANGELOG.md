# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.7.0] - 2026-10-24

### Added
- **Stage 8 Low-Light & Official Guidebook Vision Backbone**: 83,086 strategy guide illustrations and 562 physical samples trained with 6 adversarial GPU low-light operations (shadow gamma, sensor noise, deep dimming, tungsten tint, flashlight hotspot).
- **Interactive Town Map & Gen 1–9 Habitat Tracker**: Encounter database (`pokemon_encounters.json`) with interactive cartridge-style region selector, spawn rates, and encounter popouts across 9 generations.
- **5-Tier Calibrated Consensus Auto-Lock Engine**: Responsive auto-lock tiers (Tier 1 @ 8 frames down to Tier 5 @ 16 frames for dominant low-light targets).
- **Expanded $0.85\times$ Optical Centered Crop**: Wide camera crop preventing edge clipping on broad subjects like Lunala's wings and Solgaleo's mane.
- **250dp HUD Reticle Viewport**: Calibrated viewfinder framing with instant 0 ms offline sprite rendering and dynamic shiny sprite preview.

### Fixed
- **Advisory Mode Progress Bar Freeze**: Fixed progress calculation so the reticle no longer falsely reaches 100% and green while awaiting decisive consensus.
- **Initial Camera Settling Latency**: Shaved ~130ms from camera startup by reducing settling threshold from 8 to 4 frames.
- **Post-Lock Hold Delay**: Reduced voice announcement hold delay from 2400ms to 1600ms for crisp, fluid transition to the Dex entry.
- **Volbeat & Illumise Specular Glare False Positives**: Applied $0.25\times$ attenuation to protect against packaging reflections.

## [1.3.0] - 2026-10-16

### Added
- **Stage 3 Machine Learning Master Model**: 494,768 images master dataset across all 1,025 classes incorporating 3D prints, sleep series figures, TCG card art, and varied room lighting.
- **Kaggle Dataset Transparency**: Public dataset published to Kaggle Datasets (`atom0131/boridex-pokemon-master-dataset-494k`) with preparation scripts in `tools/ml/`.
- **Dynamic Version-Specific Evolution System**: Canonical game-dependent evolution requirements (e.g. Feebas with Pokéblocks in Sapphire vs. Poffins in Diamond vs. Prism Scale trade in Scarlet/Violet).
- **In-Modal Cartridge Quick-Switcher**: Interactive `VER: [EDITION] ▾` chip allowing real-time edition switching inside the evolution modal.
- **Trade Evolutions & Pixel Art**: Pixel items (`link-cable.png`, `linking-cord.png`, `upgrade.png`, `poffin.png`, `pokeblock.png`, `prism-scale.png`) and target elemental type badges (`[WATER]`, etc.).
- **Reproducible ML Pipeline**: Scrapers, merge scripts, training pipeline, and Float32 inference exporter committed under `tools/ml/`.

### Fixed
- **Dexter Voice Sibilance & Glitch Fix**: Replaced discontinuous limiter with smooth hyperbolic tangent saturation (`tanh(x * 1.15)`), eliminating harsh 50% amplitude drops and audio popping on 'S' sounds.
- **Echo Resonance**: Added high-frequency damping on comb echo feedback and downstream 4,000 Hz low-pass filter.
- **Pronunciation & Buffer Safety**: Added possessive `'s` resolution, `"FEE-bus"` phonetic spelling, and isolated utterance WAV files.

## [1.2.0] - 2026-10-15

### Added
- **Tactile Retro Game Cartridges**: 37 authentic retro game cartridges with molded plastic shells, grip ridges, foil stickers, and mascot pixel art.
- **Pokédex Lore Full-Height Expansion**: Vertical expansion of lore text box with internal vertical scrolling.
- **Refined Shiny Toggle**: Subtle `✨` shiny glyph badge in sprite viewport with `NORMAL` / `★ SHINY` button states.
- **Centered Evolution Modal**: Dynamic, content-fitted dialog centered on the display.
- **Canonical Range**: Strictly bounds index to canonical species (#001–#1025).

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
