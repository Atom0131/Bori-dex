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
