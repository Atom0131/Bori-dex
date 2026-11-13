# 🚀 Release Notes - Bori-dex v2.0.4 (Build 21)
**Release Date**: November 13, 2026  
**Platform**: Android (API 24 to API 36 / 37 Adaptive Ready)  
**Target Hardware**: Samsung Galaxy Z Fold, Pixel 9 Pro Fold & Standard Android Phones  
**Binary**: `Bori-dex-v2.0.4.apk`  

---

### What's New in v2.0.4
1. **"Who's That Pokémon?!" Pacing & Suspense Calibration**:
   - Increased the `"WHO'S THAT POKÉMON?"` silhouette intermission (`WHOS_THAT_FANFARE_DELAY_MS`) from `2200ms` to `3500ms` and the `"IT'S [POKÉMON]!"` reveal hold (`REVEAL_HOLD_DELAY_MS`) from `1300ms` to `2200ms` so the transition feels deliberate and easy to process without dragging.
   - Slowed the pulse, float, scale, and reveal-flash tweens in `PokedexScreen.kt` for smoother, less jarring motion.
2. **Centered Silhouette & Reveal Circle Alignment**:
   - Anchored the ambient Pokéball watermark circle directly inside the silhouette and reveal stage `Box` (`200.dp` compact / `240.dp` expanded) with `Alignment.Center` and `ContentScale.Fit`, ensuring the Pokémon silhouette and revealed sprite are geometrically centered inside the circle across compact cover screens, phones, and unfolded foldables.
3. **Includes v2.0.3 Enhancements**:
   - Two-stage leap-frog / snap-and-glide scroll engine, soft dissolve list transition, animated `#00FFCC` neon selection border, and unfolded dual-pane detail layout (`detailListState` decoupled).

---

# Release Notes - Bori-dex v2.0.2 (Build 19)
**Release Date**: November 11, 2026  
**Target Hardware**: Samsung Galaxy Z Fold & Pixel 9 Pro Fold Form Factors  
**Vision Model**: MobileNetV2 Float32 Unquantized (14.1 MB, 1,025 classes)  

---

### Key Highlights
1. **Fluid Dexter Speech & Zero Letter-by-Letter Spelling**:
   - Fixed a dictionary case-preservation defect in `PokemonPronunciation.kt` where matching all-caps names (`BELDUM's`, `CHARIZARD's`, `EEVEE's`) returned all-caps text, causing Android TextToSpeech to treat them as acronyms and spell out letters.
   - Enforced TitleCase return invariant and added all-caps word normalization preserving genuine acronyms (`DNA`, `HP`, `TM`, etc.).
   - Sanitized 142 entries in `pokemon_data.json` and 25 hyphenated pronunciation entries in `pokemon_pronunciations.json`.
   - Bumped Room DB sync to version 14 so upgraded devices immediately re-populate their SQLite tables with sanitized entries.

2. **Universal Offline Sprite Caching (1,342 Entities, 99.3% Coverage)**:
   - Bundled all 1,025 National Pokédex sprites, 1,025 shiny variants, and 317 battle form sprites locally in assets (~2.84 MB total).
   - Sprites load instantaneously (0ms) on cold start without network requests or GitHub raw timeouts.
   - Configured persistent 100MB Coil disk cache via custom `PokedexApplication.kt`.

---

# 🚀 Release Notes - Bori-dex v2.0.1

**Release Date:** November 10, 2026  
**Platform:** Android (API 24 to API 36 / 37 Adaptive Ready)  
**Binary:** `Bori-dex-v2.0.1.apk` / `app-debug.apk`  
**Commit:** `bfb0399` | **Tag:** `v2.0.1`  

---

## What's New in v2.0.1

### 📸 1. Calibrated Auto-Lock Temporal Runway (~1.5s Countdown)
- Dialed back the consensus locking runway from 6 frames (0.36s) to a deliberate **18–22 frames (~1.5s–1.8s)** for confidence $\ge 50\%$.
- Restores the authentic anime Pokédex countdown (`"ACQUIRING: POKEMON..."` ➔ `"LOCKING IN 3..."` ➔ `"2..."` ➔ `"1..."` ➔ `"LOCKED!"`), giving users visual confirmation and time to center the camera.
- Gives CameraX 3A algorithms (auto-exposure, auto-focus, auto-white-balance) ample frames to stabilize lighting before locking.
- Scaled background rejection confidence floor down to 10% (100× above random baseline for 1,025 classes), eliminating false `"No Pokemon detected"` lockouts on real 3D figurines.

### 🎨 2. Physical Color Disambiguation Guardrails (Green Rayquaza Fix)
- Required strict green absence (`greenPct < 0.06f`) for Shiny Rayquaza to prevent physical green statues under desk shadows from being falsely flagged as Shiny Black Rayquaza.
- Added explicit emerald green Rayquaza body detection (`greenPct >= 0.08f` or green hue band) returning `isShiny = false, confidence = 0.90f`.
- Enabled dynamic candidate re-evaluation every 3 frames without being blocked by stale confidence thresholds, ensuring camera exposure improvements update colors in real time.

### 📱 3. Dual-Screen Foldable List Synchronization
- `selectPokemon()` automatically clears stale search queries (e.g. `"999"`) and synchronizes the active region filter so the scanned Pokémon is present in the left index list.
- Guarded wide-screen cold boot effect so it never hijacks the selection to `filteredPokemonList.first()` during `Scanning`, `WhosThat`, or `Reveal`.
- The left panel `LazyColumn` now automatically animates scroll directly to the scanned Pokémon on `PokedexState.Result`.
- Rendered a glowing neon teal selection border (`BorderStroke(2.dp, Color(0xFF00FFCC))`) around the active Pokémon's card in the left list.

---

# 🚀 Release Notes - Bori-dex v2.0.0

**Release Date:** November 8, 2026  
**Platform:** Android (API 24 to API 36 / 37 Adaptive Ready)  
**Binary:** `Bori-dex-v2.0.0.apk` / `app-debug.apk`  
**Tag:** `v2.0.0`  

---

## What's New in v2.0.0

### ❓ 1. Interactive 5-Step Trainer Field Manual
- Companion-guided interactive onboarding modal led by Dexter (authentic 1997 anime Pokédex AI).
- Step 1: AI Vision Scanner & Floating Battle Overlay.
- Step 2: Ultimate Strategy Guide & Regional Encyclopedias.
- Step 3: Dexter Voice Engine & Live DSP Voice Test.
- Step 4: 37 Retro Game Cartridges & Dynamic Lore.
- Step 5: Settings, Customization & Replay.
- Silent-on-open gate suppresses automatic speech on initial launch and during Step 1.

### 🎯 2. Floating Battle Overlay Scanner
- System-level accessibility overlay (`PokedexOverlayAccessibilityService`) floating over Android emulators and web browsers with live 4×/2× battle weakness readouts.

---

# 🚀 Release Notes - Bori-dex v1.9.9

**Release Date:** November 2, 2026  
**Platform:** Android (API 24 to API 36 / 37 Adaptive Ready)  
**Binary:** `Bori-dex-v1.9.9.apk` / `app-debug.apk`  
**Master Dataset:** [Bori-dex 1,025 Pokemon Master Vision Dataset](https://www.kaggle.com/datasets/atom0131/boridex-pokemon-master-dataset-494k)  

---

## What's New in v1.9.9

### 🧭 1. Dexter's Ultimate Strategy Guide & 13 Strategy Domains
- **Comprehensive Welcome Hub**: Opening `🧭 GUIDE` or clearing queries presents `"Dexter's Strategy Guide (${edition.uppercase()})"`, featuring the authentic `master-ball.png` item icon and structured quick-starts for:
  - **13 Specialized Strategy Domains**: Healing Items, Key Items, Money Items, Event Items, Evolution Items, Battle & Held Items, Poké Balls & Capture, Missions & Roadblocks, Hidden Items & Dowsing, Braille & Secret Ciphers, Legendary Encounters, Event Pokémon & Myths, and Puzzles & Dungeons.
  - **28 Poké Ball & Master Ball Guides**: Detailed catch multipliers, earliest mart unlock locations, and full per-game Master Ball target strategies across Generations 1–9.
  - **10 Regional Dungeon Encyclopedias**: Deep multi-section guides for the greatest landmarks and dungeons in Pokémon history.

### 🔴 2. Complete 28-Ball Catch Mechanics & Per-Game Master Ball Target Engine
- **Per-Game Master Ball Targets**: Game-specific strategic advice pinpointing the optimal use of the Master Ball:
  - *FireRed / LeafGreen*: Urgent warning for Roaming Raikou/Entei due to the infamous Gen 3 Roar deletion glitch (Roar deletes the beast permanently from save memory); Mewtwo in Cerulean Cave B1F as runner-up.
  - *HeartGold / SoulSilver*: Roaming Latias/Latios or Raikou/Entei to avoid tedious Mean Look tracking.
  - *Ruby / Sapphire / Emerald*: Roaming Latios/Latias or Rayquaza at Sky Pillar.
  - *Diamond / Pearl / Platinum / BDSP*: Roaming Mesprit or Cresselia.
  - *Scarlet / Violet*: Second Koraidon/Miraidon (Lv. 72, catch rate 3) at Area Zero Zero Lab, or the Four Treasures of Ruin (Chien-Pao, Chi-Yu, Ting-Lu, Wo-Chien); critical warning *not* to waste Master Balls on Ogerpon or Terapagos, which are 100% guaranteed catches in any ball.
- **Strict Anti-Mart Guardrail**: Master Ball, Apricorn Balls, Safari/Sport Balls, and ancient items are strictly excluded from standard Poké Mart progression banners, preventing false mart availability tags.

### 🏛️ 3. 10 Canonical Regional Dungeon Encyclopedias
- **Direct Browsing & Search Integration**: Surfaced all 10 Regional Dungeon Encyclopedias directly within the 13th domain (`Puzzles & Dungeons`) and via natural search:
  1. **Great Crater of Paldea (Area Zero)**: Impact formation, Zero Gate lift, Research Stations 1–4, Zero Lab, and Underdepths.
  2. **Cerulean Cave (Unknown Dungeon)**: Post-game Celio network quest unlock, Mewtwo B1F chambers, and fossil relics.
  3. **Mt. Silver (Silver Cave)**: 16-badge unlock, Red championship climax, and high-level wild Tyranitar cavern.
  4. **Sky Pillar**: Mach Bike cracked-floor acrobatics, Rayquaza summit ascent, and Weather Trio awakening.
  5. **Spear Pillar & Distortion World**: Mt. Coronet ascent, Cyrus climax, Distortion World anti-gravity platforming, and Giratina capture.
  6. **Giant Chasm**: Kyurem crater forest, Plasma Frigate invasion, and DNA Splicers cavern.
  7. **Terminus Cave**: Historic coal mine maze, Zygarde 50% chamber, and hidden Orbs.
  8. **Ultra Space & Resolution Cave**: Ultra Wormhole warp mechanics, Poni Island depths, and Ultra Beast encounters.
  9. **The Crown Tundra & Max Lair**: Peony's expedition, Legendary Clues 1–3, Dynamax Adventures 100% catch rate strategy, and Calyrex steed bonding.
  10. **Coronet Highlands & Temple of Sinnoh**: Hisui ancient ruins, Mount Coronet climb, Dialga/Palkia Origin Forme battles, and the Origin Ball.
- **Connected Zones & Sub-Areas**: Dedicated UI progression banner displaying all internal chambers, sub-levels, and research outposts.

### 🎮 4. 100% 37-Cartridge Parity in UI Switcher
- Added **Brilliant Diamond** (`0xFF4FC3F7`) and **Shining Pearl** (`0xFFF48FB1`) to `ALL_GAME_CARTRIDGES` in `PokedexScreen.kt`, completing all 37 mainline editions in the UI cartridge switcher with tailored palette chips and mascot styling.

---

# 🚀 Release Notes - Bori-dex v1.9.8

**Release Date:** November 2, 2026  
**Platform:** Android (API 24 to API 36 / 37 Adaptive Ready)  
**Binary:** `Bori-dex-v1.9.8.apk` / `Bori-dex.apk`  
**Master Dataset:** [Bori-dex 1,025 Pokemon Master Vision Dataset](https://www.kaggle.com/datasets/atom0131/boridex-pokemon-master-dataset-494k)  

---

## What's New in v1.9.8

### 📐 1. Google Adaptive Apps & Foldable Layout Overhaul (Android 17 Standard)
- **Material 3 WindowSizeClass Breakpoints**: Dynamic reflow based on `WindowWidthSizeClass` (Compact `< 600dp` vs Medium/Expanded `\ge 600dp`).
- **Vertical Space Optimization on Folded Devices**: Resolved vertical layout cramping on compact cover screens (e.g., Samsung Galaxy Z Fold closed mode, 475dp viewport):
  - **Dynamic Top Lens**: Scaled main scanner lens from 80dp to 50dp and action buttons from 40dp to 34dp on compact displays, reclaiming ~30dp.
  - **Monitor Sprite Stage**: Dynamically scaled from 152dp to 110dp (sprite icon 95dp, edge pagination arrows 34dp), reclaiming ~42dp.
  - **Action & Evolution Strip**: Compact 32dp action buttons and 24dp monospace evolution line, reclaiming ~14dp.
  - **Terminal Header & Quick Toolbar**: Tightened toolbar from 36dp to 28dp and matchup chips from 26dp to 20dp, reclaiming ~28dp.
  - **Spacious Lore Description**: Reclaimed **~114dp (~300px)** of net vertical space, allowing the narrative flavor text box to render with >150dp (>400px) of fluid, unclipped breathing room.
- **Unfolded Two-Pane Experience**: Retained full 80dp lens, 152dp monitor stage, and fold-hinge awareness across dual-pane tablets and unfolded foldables.

### 🧭 2. Canonical Area Zero & Paradox Encounter Encyclopedia
- **Ingested Official Web Guides & Walkthroughs**: Ingested complete Bulbapedia, IGN, Game8, Serebii, and strategy guide documentation for the Great Crater of Paldea (Area Zero) and the Zero Gate.
- **Complete Paradox Coverage**: Comprehensive encounter data for all Scarlet & Violet Paradox Pokémon (Great Tusk, Scream Tail, Brute Bonnet, Flutter Mane, Slither Wing, Sandy Shocks, Roaring Moon, Koraidon, Iron Treads, Iron Bundle, Iron Hands, Iron Jugulis, Iron Moth, Iron Thorns, Iron Valiant, Miraidon).
- **Research Stations 1–4 & Depths**: Detailed spawn breakdowns across Upper Surface Biome, Research Station 2 cavern entrances, Research Station 3 waterfall cliffs, Research Station 4 crystal depths, and the Hidden Roaring Moon / Iron Valiant secret cave.

### ⚖️ 3. Physical Specifications & Telemetry Display
- **Metric & Imperial Height/Weight Chips**: Dual-unit display formatted as `HT: X.Xm • F'IN"` and `WT: XX.Xkg • XXX.Xlbs`.
- **Anatomical Size Classification**: Automatic categorisation into `TINY`, `SMALL`, `MEDIUM`, `LARGE`, or `GIGANTIC` with dedicated cybernetic badge styling.

---

# 🚀 Release Notes - Bori-dex v1.7.0

**Release Date:** October 24, 2026  
**Platform:** Android (API 24 to API 36)  
**Binary:** `Bori-dex-v1.7.0.apk` / `app-release.apk`  
**Master Dataset:** [Bori-dex 1,025 Pokemon Master Vision Dataset (680k+ Images)](https://www.kaggle.com/datasets/atom0131/boridex-pokemon-master-dataset-494k)  

---

## What's New in v1.7.0

### 🌙 1. Stage 8 Low-Light & Official Guidebook Vision Backbone
- **Official Strategy Guide Extraction**: Extracted and ingested **83,086 official illustrations** across 756+ species from 28 strategy guides (Sun & Moon, Ultra Sun & Moon, HeartGold & SoulSilver, Diamond & Pearl, Scarlet & Violet, etc.).
- **Physical Real-World Dataset**: Ingested **562 physical samples**, including retail store photos, test video crops (Lunala & Solgaleo 3DS box art), Fossil Museum Pikachu plushies, and statues.
- **6 Adversarial Low-Light GPU Operations**: DirectML GPU augmentations simulating dim evening lighting (deep dimming down to $0.18\times$, shadow gamma $\gamma \le 2.6$, high-ISO sensor noise $\sigma \le 25$, $2700\text{K}$ incandescent tint, dynamic range flattening, flashlight hotspot).
- **High-Precision Float32 TFLite Architecture**: Deployed as Float32 (`14.1 MB`) in `assets/model.tflite` to preserve full classification precision across all 1,025 classes.

### 🗺️ 2. Interactive Town Map & Gen 1–9 Habitat Tracker
- **Complete Encounter Engine**: Shipped `pokemon_encounters.json` covering encounter tables, locations, spawn percentages, and encounter methods (Walking, Surfing, Fishing, Rock Smash, Headbutt) across Gen 1 to Gen 9.
- **Cartridge-Style Region Selector**: Interactive cartridge carousel with custom retro shells for Kanto, Johto, Hoenn, Sinnoh, Unova, Kalos, Alola, Galar, and Paldea.
- **Dynamic Spawn Popouts**: Clean, responsive map dialogs with version-specific badges and habitat markers.

### ⚡ 3. Consensus Latency Fine-Tuning & Snappy Transitions
- **5-Tier Calibrated Auto-Lock**:
  - **Tier 1** ($\ge 65\%$): Auto-locks in **8 frames (~0.5s)**.
  - **Tier 2** ($\ge 40\%$): Auto-locks in **10 frames (~0.6s)**.
  - **Tier 3** ($\ge 26\%$ & $\ge 1.4\times$ ratio): Auto-locks in **12 frames (~0.8s)**.
  - **Tier 4** ($\ge 22\%$ & $\ge 1.8\times$ ratio): Auto-locks in **14 frames (~0.9s)**.
  - **Tier 5 (Dominant Low-Light)**: $\ge 17\%$ with $\ge 2.5\times$ ratio auto-locks in **16 frames (~1.0s)**, eliminating the previous 2.6s advisory hold on dark packaging.
- **Advisory Progress Bar Alignment**: Fixed progress bar calculation so it strictly reflects active locking progress, eliminating false 100% full-bar freezes.
- **Initial Camera Settling**: Shaved ~130ms from lens activation by reducing settling frames from 8 to 4.
- **Post-Lock Voice Reveal**: Tightened delay from 2400ms to 1600ms for immediate, fluid transition to the full Dex entry.

### 👁️ 4. Scanner Viewport & Camera Optical Centering
- **Expanded $0.85\times$ Optical Crop**: Widened camera analyzer crop from $0.70\times$ to **$0.85\times$** at optical center ($44.1\%$), capturing wide anatomical wingspan (Lunala), perimeter mane (Solgaleo), and physical 3DS box borders without edge clipping.
- **Calibrated 250dp Reticle**: Perfectly balanced across folded cover and unfolded tablet displays on Samsung Galaxy Z Fold.
- **0 ms Offline Sprites**: Instant offline loading from local assets with dynamic shiny sprite previews.

### 🛡️ 5. Specular Glare Attenuation Guardrail
- **Reflection Suppression**: Automatically applies $0.25\times$ attenuation to Volbeat/Illumise candidates below $60\%$ confidence to suppress false reflections on glossy packaging and retail glass.
- **Automated Verification**: 14 JVM unit tests in `PokedexConsensusTest` passing 100%.

---

# 🚀 Release Notes - Bori-dex v1.6.4

**Release Date:** October 22, 2026  
**Platform:** Android (API 24 to API 36)  
**Binary:** `Bori-dex-v1.6.4.apk` / `app-debug.apk`  
**Master Dataset:** [Bori-dex 1,025 Pokemon Master Vision Dataset (679k Images)](https://www.kaggle.com/datasets/atom0131/boridex-pokemon-master-dataset-494k)

---

## What's New in v1.6.4

### 🎯 1. Scanner HUD Overhaul & 240dp Viewport
- **Enlarged 240.dp Viewport**: Enlarged target viewfinder box from 125.dp to **`240.dp` × `240.dp`** with `RoundedCornerShape(14.dp)`, providing natural, comfortable physical framing for plushies, toys, 3D prints, and cards.
- **Balanced Vertical Centering**: Positioned at `Alignment.Center` with `offset(y = (-32).dp)`, perfectly calibrated between the top display bezel (~120.dp clearance) and quick-zoom dock (~75.dp clearance).
- **Concentric Rounded Corner Brackets**: Replaced sharp 90° right angles with smooth 9.dp circular arcs matching the outer 14.dp curvature (`14.dp - 5.dp = 9.dp`). Rendered with `StrokeCap.Round` and `StrokeJoin.Round` for soft, organic cyber curves with zero sharp edges.
- **Zero-Overlap Telemetry & Reticle Rescale**:
  - Rescaled Pokéball reticle: `pokeRadius = 84.dp`, `outerRadius = 98.4.dp`, central core = `16.dp`.
  - Holographic creature preview enlarged to `140.dp` container with `125.dp` sprite.
  - Telemetry readouts (`SYS: SCAN 894MHz`, `RADAR: ACTIVE`, `TARGET: --`, `FOV: 70% CROP`) sit cleanly with zero overlap.

### 📐 2. Pixel-Perfect 0px Flush Bezel Fit
- **Unified Screen Padding**: Applied `isCompactBezel` so that `PokedexState.Scanning` uses the exact same `6.dp` bezel padding as the Pokémon Entry screen (`PokedexState.Result`).
- **Viewfinder Bezel Clipping**: Added `.clip(RoundedCornerShape(8.dp))` to both the camera container `Box` and `CameraPreview`, eliminating the 55px bleed into the red chassis and unclipped corners. Native camera feed now aligns with 0 px deviation ($y = 383\text{ px}$, $x = 30\text{ px}$).

### 🦖 3. Stage 6 Fossil Museum Pikachu Model Calibration
- **Real-World Multi-Crop Ingestion**: Ingested 18 high-resolution real-world photos of the authentic Japanese Pokémon Fossil Museum (ポケモン化石博物館) Excavator Pikachu plush across multiple lighting conditions and angles.
- **DirectML Targeted Calibration**: Fine-tuned Stage 6 backbone layers on NVIDIA GTX 1660 Ti with balanced dual-stream batches, eliminating misclassification with Volbeat (#313).
- **Full-Precision Float32 TFLite Engine**: Preserves Float32 weights (`14.1 MB`) in `assets/model.tflite` to prevent post-training quantization collapse on 1,025 classes.

### 🌐 4. Pokemon GO Evolution Items & Anime Natural Speech
- **Comprehensive Evolution Guide**: Dynamic evolution guide supporting Pokemon GO items (XL Candy, Mega Energy, Sinnoh/Unova Stones, Solar/Lunar Fusion Energy).
- **Phonetic Pronunciation Engine**: Expanded dictionary to 1,071 title-cased entries across all 1,025 species, eliminating screaming all-caps in favor of authentic anime sentence casing.

---

# 🚀 Release Notes - Bori-dex v1.3.0

**Release Date:** October 16, 2026  
**Platform:** Android (API 24 to API 36)  
**Binary:** `app-release.apk` / `app-debug.apk`  
**Kaggle Dataset:** [Bori-dex 1,025 Pokemon Master Vision Dataset (494k Images)](https://www.kaggle.com/datasets/atom0131/boridex-pokemon-master-dataset-494k)

---

## What's New in v1.3.0

### 🧠 1. Stage 3 Machine Learning Master Model (494k Images)
- **Massive Real-World Dataset Expansion**: Fine-tuned across **494,768 images** belonging to all **1,025 Pokémon species classes** (Gen 1 through Gen 9).
- **Physical Invariance**: Incorporates physical 3D resin & filament prints (`varaiants_3d_prints`), sleep series figurines (`varaiants_sleep`), TCG card artwork, anime stills, and natural room lighting/shadow variations.
- **Silhouette & Morphology Awareness**: The network recognizes Pokémon by true geometric structure and contours rather than flat 2D color textures alone.
- **Convergence Metrics**:
  - **Validation Accuracy**: Peaked at **`53.71%` Top-1** and **`86.4%` Top-5** across 1,025 classes on a hardened 98,953-image holdout split.
  - **Validation Loss**: Reached a record project low of **`2.8309`**.
  - **Real-World In-App Accuracy**: Delivers **`~91% – 93%` scanning success** on physical items when combined with the 22-frame consensus voter.
- **Kaggle Transparency**: Published the entire 4.75 GB raw dataset to [Kaggle Datasets](https://www.kaggle.com/datasets/atom0131/boridex-pokemon-master-dataset-494k) and committed all scraping and training scripts to [`tools/ml/`](tools/ml/).

---

### 🛡️ 2. Full-Precision Float32 TFLite Architecture
- **Quantization Confidence Guardrail**: Preserves full **Float32 precision** (`14.1 MB`) in `assets/model.tflite` to prevent the catastrophic 6x confidence collapse caused by INT8 post-training quantization on 1,025 classes.
- **Native JNI Direct Buffer Matching**:
  - Input: `[1, 224, 224, 3] Float32` directly mapped to a 602,112-byte native `ByteBuffer` with zero memory copying.
  - Output: `[1, 1025] Float32` feeding continuous floating-point probability distributions into the temporal voter.

---

### 🔄 3. Dynamic Game Edition Evolution Engine
- **Canonical Game Evolution Rules**: Pokémon evolutions now dynamically adapt to the active game cartridge selected.
  - **Feebas $\rightarrow$ Milotic**:
    - *Hoenn (Ruby / Sapphire / Emerald)*: Level up with Beauty $\ge 170$ via Pokéblocks (`pokeblock.png`).
    - *Sinnoh (Diamond / Pearl / Platinum / BDSP)*: Level up with Beauty $\ge 170$ via Poffins (`poffin.png`).
    - *Gen 5+ & Scarlet / Violet*: Trade holding Prism Scale (`prism-scale.png`).
- **In-Modal Cartridge Quick-Switcher**:
  - Tapping the `VER: [EDITION] ▾` chip directly inside the **HOW TO EVOLVE** section opens the retro cartridge selector.
  - Switching games immediately updates the required method, pixel item sprite, and location notes live without closing the modal.
- **Trade Evolutions & Missing Pixel Art**:
  - Attached `link-cable.png` / `linking-cord.png` to trade-only Pokémon (Kadabra, Haunter, Machoke, Graveler) with *Legends: Arceus* cord instructions.
  - Added `upgrade.png` for Porygon $\rightarrow$ Porygon2.
  - Added destination elemental type badges (`[WATER]`, `[GHOST][POISON]`) to all evolution destination cards.

---

### 🎙️ 4. Dexter Voice Engine DSP Sibilance & Glitch Fix
- **Smooth Hyperbolic Tangent Limiter**: Replaced the flawed limiter (`1.0 - 1/(x+1)`) with $C^\infty$ continuous saturation: `tanh(x * 1.15)`. Eliminates the severe 50% amplitude drop on signal peaks and harsh popping/clipping on consecutive 'S' sounds.
- **Comb Echo Flutter Damping**: Added high-frequency damping on feedback paths and placed a 4,000 Hz low-pass filter downstream to suppress high-frequency resonance.
- **Phonetics Polish**:
  - Added possessive `'s` resolution in `PokemonPronunciation.kt`.
  - Added explicit phonetic respelling for Feebas: `"FEE-bus"`.
  - Utterance-specific temporary WAV files (`${utteranceId}_raw.wav`) prevent audio buffer collisions.

---

# 🎮 Release Notes - Bori-dex v1.2.0

**Release Date:** October 15, 2026  
**Platform:** Android (API 24 to API 36)  

### Highlights
1. **Tactile Retro Game Cartridges**: 37 authentic tactile retro game cartridges with realistic molded plastic shell colors, top grip ridges, recessed metallic foil stickers, and mascot pixel art.
2. **Pokédex Data Full-Height Expansion**: Expanded the lore text box vertically to fill the display with smooth internal vertical scrolling.
3. **Refined Shiny Toggle**: Integrated a subtle `✨` shiny glyph badge into the sprite viewport with balanced `NORMAL` / `★ SHINY` button states.
4. **Centered & Dynamic Evolution Modal**: Replaced rigid 85% height modal with a dynamic, content-fitted dialog centered on the display.
5. **Canonical #001–#1025 Range Enforcement**: Strictly bounds the index to canonical species (ending at Pecharunt #1025), mapping alternate forms to their base canonical ID.

---

# 🚀 Release Notes - Bori-dex v1.1.0

**Release Date:** October 13, 2026  
**Platform:** Android (API 24 to API 36)  

### Highlights
1. **Tabbed Settings Menu**: Dedicated tabs for `VOICE`, `SOUND & SCAN`, and `GAME VERSION` eliminating scrolling.
2. **Grouped & Paired Game Releases**: Generation pairs prioritizing era-appropriate entry data.
3. **Directional Auto-Speech**: Speaks when moving forward, automatically silences when navigating backward.
4. **Dedicated `🗣 SPEAK` Replay Button**: Replay Dexter's narration on demand.
5. **Human-Friendly 54dp Page-Turn Controls**: High-contrast navigation arrows at screen edges.

---

# 🚀 Release Notes - Bori-dex v1.0.0

**Release Date:** October 13, 2026  
**Platform:** Android (API 24 to API 36)  

### Highlights
1. **On-Device AI Computer Vision Scanner**: Real-time TensorFlow Lite classification running 100% offline with CameraX.
2. **Authentic 1997 Anime "Dexter" Voice**: Tuned robotic tenor voice with real-time hardware DSP comb filter (dual-tap metallic echo + 2kHz horn mid-EQ).
3. **"Who's That Pokémon?!" Transition**: Signature 6-note anime commercial bumper arpeggio in 44.1kHz PCM WAV.
4. **Gen 1–9 Pokédex Index**: Regional filters, search, normal/shiny sprites, and alternate form chips.
5. **Phonetic Pronunciation Engine**: Resolves tricky Pokémon names naturally.
