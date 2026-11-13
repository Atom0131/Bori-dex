## [2.0.4] - 2026-11-13
### Changed & Fixed
- **"Who's That Pokémon?!" Transition Pacing**: Increased `WHOS_THAT_FANFARE_DELAY_MS` (`2200ms` -> `3500ms`) and `REVEAL_HOLD_DELAY_MS` (`1300ms` -> `2200ms`) in `PokedexViewModel.kt`, and slowed `WhosThat` / `Reveal` pulse, float, and flash tweens in `PokedexScreen.kt`.
- **Silhouette & Reveal Circle Centering**: Anchored the ambient Pokéball watermark circle inside the silhouette/reveal stage `Box` in `PokedexScreen.kt` with `Alignment.Center` and `ContentScale.Fit` so the silhouette and revealed sprite remain geometrically centered in the circle on all screen sizes.

## [2.0.2] - 2026-11-11
### Fixed
- **Dexter Pronunciation Engine Letter-Spelling Bug**: Fixed case-preservation defect in `PokemonPronunciation.kt` where all-caps names (`BELDUM's`, `CHARIZARD's`, `EEVEE's`, `LUGIA's`, `DIALGA's`) were returned verbatim and spelled out letter-by-letter by Android TTS.
- **All-Caps Flavor Text Sanitization**: Sanitized 142 Pokémon entries in `pokemon_data.json` and added Step 3.1 all-caps normalization in `PokemonPronunciation.kt` and `PokemonMapHabitatRepository.kt`.
- **Hyphenated Pronunciation Stutters**: Sanitized 25 pronunciation entries in `pokemon_pronunciations.json` (including `Ho-Oh` -> `Ho Oh`, `deoxys-speed` -> `Deeoksis Speed Forme`) to prevent synthesizer pauses.
- **Room Database Sync Guard**: Bumped `pokemon_data_asset_version` from 13 to 14 in `PokedexDatabase.kt` and `PokedexViewModel.kt` to force upgraded devices to re-sync sanitized data into SQLite.

### Added
- **Universal Offline Sprite Bundling**: Bundled all 1,025 standard front sprites, 1,025 shiny sprites, and 317 in-game battle form sprites (#10001–#10326) in local assets (1,342 entities total, 99.3% coverage), eliminating remote network latency on cold starts.
- **Persistent Coil Disk Cache (`PokedexApplication.kt`)**: Implemented custom `Application` class configuring 100MB persistent disk cache and 25% memory cache.
- **Unit Test Coverage**: Added `testAllCapsPokemonNamesAndPossessivesDoNotSpellOut` in `PokemonPronunciationTest.kt`.

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.9.9] - 2026-11-02

### Added & Enhanced
- **Dexter's Ultimate Strategy Guide & 13 Strategy Domains**:
  - Implemented `"Dexter's Strategy Guide (${edition.uppercase()})"` welcome overview on initial Guide launch and query clearing with `master-ball.png` item icon.
  - Complete coverage across all 13 Strategy Domains: Healing Items, Key Items, Money Items, Event Items, Evolution Items, Battle & Held Items, Poké Balls & Capture, Missions & Roadblocks, Hidden Items & Dowsing, Braille & Secret Ciphers, Legendary Encounters, Event Pokémon & Myths, and Puzzles & Dungeons.
- **28 Poké Ball Guides & Per-Game Master Ball Target Engine**:
  - All 28 Poké Balls with precise catch multipliers, mechanics, and earliest unlock Poké Mart locations.
  - Game-by-game Master Ball target recommendations across Gens 1–9, including critical warnings (FireRed/LeafGreen Roaming Beast Roar deletion bug, guaranteed-catch ball conservation for Ogerpon/Terapagos).
  - Anti-mart guardrail excluding unique balls and key items from standard Poké Mart banners.
- **10 Regional Dungeon Encyclopedias**:
  - Integrated 10 landmark dungeon encyclopedias into the `Puzzles & Dungeons` domain and direct search (Great Crater of Paldea / Area Zero, Cerulean Cave, Mt. Silver, Sky Pillar, Spear Pillar & Distortion World, Giant Chasm, Terminus Cave, Ultra Space, Crown Tundra & Max Lair, Coronet Highlands & Temple of Sinnoh).
  - Added `CONNECTED ZONES & SUB-AREAS` progression banners displaying sub-areas and research stations.
- **100% 37-Cartridge Mainline UI Parity**:
  - Added `Brilliant Diamond` and `Shining Pearl` to `ALL_GAME_CARTRIDGES` in `PokedexScreen.kt`.

## [1.9.8] - 2026-11-02

### Added & Enhanced
- **Adaptive Layout Overhaul for Foldable Cover Screens (Android 17 / Material 3 WindowSizeClass)**:
  - Dynamically scaled `PokedexHeader` (50dp lens), monitor sprite stage (110dp box, 95dp sprite), audio actions (32dp), evolution bar (24dp), terminal header (30dp), toolbar (28dp), and matchup chips (20dp) on compact screens (`< 600dp`).
  - Reclaimed ~114dp (~300px) of vertical space on compact viewports, providing spacious, unclipped narrative lore text (>150dp height).
  - Maintained full 80dp lens, 152dp monitor stage, and fold-hinge awareness on unfolded displays (`>= 600dp`).
- **Canonical Area Zero & Paradox Encounter Encyclopedia**:
  - Ingested exhaustive official web guides and encounter data for all 16 Paradox Pokémon across Area Zero Research Stations 1–4 and the Zero Gate.
- **Physical Specifications Telemetry**:
  - Live metric/imperial height and weight telemetry chips with dynamic anatomical size category chips.

## [1.9.6] - 2026-11-01

### Added & Enhanced
- **Stage 10 Vision Model: Extreme Screen Scanning, Harsh Color Casts & Rear Battle Models**:
  - **Dataset Ingestion**: Enriched training dataset with official PokeAPI 2D back sprites, Showdown 3D rear models, and Scarlet & Violet / Legends: Arceus battle screenshots captured from behind with over-the-shoulder perspectives.
  - **Stage 10 Adversarial GPU Augmentation Graph**:
    - **CRT & LCD Subpixel Scanlines**: Alternating horizontal/vertical raster scanlines simulating handheld Nintendo Switch OLED, Switch Lite, 3DS, and TV displays.
    - **Moiré Optical Interference Fringes**: High-frequency dual-angle beating patterns simulating camera sensor aliasing against screen pixels.
    - **Night Shift / f.lux Amber-Yellow Cast**: Heavy blue light suppression and warm color temperature shifts altering base elemental colors.
    - **Crimson Battle Arena & Sunset Wash**: Intense scarlet/red illumination simulating dramatic battle arenas and red ambient lighting.
    - **Over-The-Shoulder Rear Perspective Warp**: Simulates camera angles looking up from behind the Pokémon on the battlefield.
    - **Display Glass Specular Hotspots**: Injects indoor ceiling lamp reflection flares on glossy screens.
    - Retains Stage 9 holographic rainbow diffraction, cosmos sparkle glitter, packaging crimps, and low-light sensor noise.
  - **Float32 TFLite Deployment**: Fine-tuned MobileNetV2 architecture resuming from Stage 9 weights, exported unquantized Float32 TFLite model (~14 MB), and deployed directly to `app/src/main/assets/model.tflite`.

## [1.9.5] - 2026-10-31

### Added & Enhanced
- **Stage 9 Vision Model: Booster Packs, Retail Packaging, Covers & Holographic Cards Retraining**:
  - **Dataset Ingestion**: Enriched training dataset across all Pokémon species with official card booster packs, booster boxes, tins, retail blister packaging, game box art covers, CoroCoro magazine covers, and Pokémon Adventures manga covers.
  - **Holographic & Specular Glare Resilience**: Specifically addressed failure modes on holographic/foil cards—targeting the **Pokémon Halloween Trick or Trade set (2022/2023)** with dedicated high-density coverage of **Mewtwo (#150)** and **Darkrai (#491)** alongside Gengar, Mimikyu, and Halloween holo rares.
  - **Stage 9 GPU Adversarial Holographic & Packaging Augmentation Graph**:
    - Synthesizes realistic prismatic rainbow diffraction grating (sinusoidal hue phase waves across diagonal angles) directly on the GPU during training.
    - Simulates high-intensity specular glare hotspots and reflection streaks (camera flash bouncing off glossy card foil and plastic booster wrappers).
    - Injects cosmos foil iridescent micro-sparkle glitter noise matching the Trick or Trade holofoil pattern.
    - Generates booster pack serrated crimp ridges and retail packaging cutoffs / partial card exposures.
    - Preserves deep low-light dimming, shadow gamma crush, and high-ISO sensor grain.
  - **Float32 TFLite Deployment**: Fine-tuned MobileNetV2 architecture with cosine decay learning rate resuming from Stage 8 checkpoint, exported unquantized Float32 TFLite model (~14 MB), and deployed directly to `app/src/main/assets/model.tflite`.

## [1.9.4] - 2026-10-31

### Added & Changed
- **Pokédex Entry Card Tactical Integration (STRONG TO & WEAK TO Relocation)**:
  - Migrated the defensive **`⚠️ WEAK TO`** badge strip down from the upper monitor stage into the spacious **`POKÉDEX DATA`** card, eliminating visual crowding between action buttons and the evolution lineage.
  - Added a dedicated **`⚡ STRONG TO`** row inside the Pokédex entry card displaying the Pokémon's offensive Same-Type Attack Bonus (STAB) super-effective coverage (`2×`) alongside the **`TYPE GUIDE ℹ️`** battle matrix launcher.
  - Provided over 800px of horizontal breathing room for tactical badges on wide/foldable screens (`maxWidth > 600.dp`), with horizontal scrolling on compact phone displays and dedicated `NONE (1×)` / `NONE` fallbacks for species without super-effective offensive or defensive matchups.
  - Preserved full scrollable narrative game lore text beneath the tactical strips with dynamic weight (`weight(1f)`).

## [1.9.3] - 2026-10-31

### Fixed
- **Canonical Map Habitat & Legendary Quarantine Audit**:
  - Grounded map encounters strictly in official Game Freak / Nintendo game guides, purging 523 fabricated `regionalHabitats` cross-region blocks, 179 invalid legendary region assignments, and 326 invalid pre-generation chronological assignments from `pokemon_encounters.json`.
  - Implemented compile-time `CANONICAL_LEGENDARY_MAP` in `PokemonMapHabitatRepository.kt`, strictly isolating all 100+ Legendaries, Mythicals, Ultra Beasts, and Paradox Legendaries across Gens 1–9 to their canonical regions and static story encounters.
  - Eliminated the bug where Kalos-exclusive Legendary Yveltal (#717) erroneously appeared roaming wild in Hoenn (Mt. Pyre / Route 101); querying Yveltal or Xerneas under Hoenn editions (Ruby, Sapphire, Emerald, Omega Ruby, Alpha Sapphire) now returns `isAreaUnknown = true` with 0 pins, while Kalos (X and Y) displays the authentic Geosenge Town Team Flare Secret HQ static story pin.
  - Hardened `PokemonRegion.matches()` in `PokemonRegion.kt` to prevent non-native species from matching foreign regions based on National Pokédex flavor texts.
  - Incremented Room database asset sync version to 13 in `PokedexDatabase.kt` and `PokedexViewModel.kt` to ensure seamless database re-synchronization.

## [1.9.2] - 2026-10-31

### Fixed
- **Pokédex Entry UI Layout Alignment & Overlap Prevention**:
  - Restructured the Pokédex Entry data card controls header into two dedicated, non-overlapping rows:
    - **Row 1**: `POKÉDEX DATA` title and `🔊` speech replay button aligned to the left; version dropdown (`VER: <EDITION> ▼` / `EXIT GO`) aligned to the right.
    - **Row 2**: Tactile `TOWN MAP 🗺️` launcher pill (with 20dp pixel map thumbnail and `#00FFCC` label) aligned to the left; `⚡ FORMS & EVOS` button aligned to the right.
  - Symmetrically distributed horizontal elements to provide over 400px of breathing room on foldable/tablet displays (`maxWidth > 600.dp`, Pixel Fold unfolded), completely eliminating the collision between the Town Map thumbnail and the Forms & Evolutions button.
  - Constrained Pokémon display names in the header with `weight(1f, fill = false)` and `TextOverflow.Ellipsis` to prevent long names from crowding adjacent `#NO` or elemental type badges.
  - Preserved authentic dual-screen Silph Co. chassis, central metallic fold hinge (`PokedexFoldHinge`), and configuration state without introducing destructive scaffold regressions.

## [1.9.1] - 2026-10-30

### Fixed
- **Pure Official Pokédex Entries**:
  - Stripped all 724 synthetic non-native prefixes (`"This Pokémon does not naturally inhabit the Paldea region... National Pokédex entry: "`) from `pokemon_data.json` across Pokémon Scarlet and Violet records.
  - Purified `PokemonMapHabitatRepository.resolvePokedexEntryForCartridge()` fallback logic: when a Pokémon is not natively documented in a selected regional game's Pokédex, it cleanly returns the Pokémon's canonical Pokédex entry without appending synthetic commentary ("This Pokémon is not documented in the...").
  - Hardened `PokemonMapHabitatRepository.cleanFlavorText()` and UI presentation layers (`displayFlavorText`, `currentCleanedFlavor`) to strip any synthetic non-native prefix strings.
  - Incremented Room SQLite asset sync version to 12 in `PokedexDatabase.kt` and `PokedexViewModel.kt` to guarantee fresh synchronization on first launch.

### Added
- **"ALL MAPS" National Pokédex Branding & Device Symbol**:
  - Rebranded the All-Regions town map entry to **"ALL MAPS"** (`eraSubtitle = "National Pokédex"`, `#001 - #1025`).
  - Featured the iconic red Pokédex device icon (`assets/maps/pokedex_icon.png`) in the master Town Map selector and main header thumbnail.
  - Updated main screen header banner to display `ALL MAPS • National Dex` in cyan (`#00FFCC`).

## [1.9.0] - 2026-10-26

### Added
- **Default Authentic Town & Road Labels on All Regional Maps**:
  - Implemented `RegionMapLandmarksRepository` and `RegionMapLandmarksLayer` composable rendering authentic town badges (`🏠`), city badges (`🏛️`), connecting route shields (`R1`, `R104`, `R29`), and key landmark pins (`📍`) across all 11 Pokémon world regions (Kanto GB, Kanto GBA + Sevii Islands, Johto, Hoenn, Sinnoh, Unova, Kalos, Alola, Galar, Hisui, Paldea).
  - Labels are **enabled by default** across all map views:
    - Pokédex Detail Town Map modal (`PokemonMapHabitatModal`).
    - Embedded Strategy Guide topic canvas (`GuideMapPinCanvas`).
    - Interactive full-screen pinch-to-zoom & pan modal (`ExpandedGuideMapModal`).
  - Added tactile interactive toggle button (`🏷️ LABELS: ON / OFF`) across all map header and canvas HUDs to allow toggling between labeled view and pristine raw artwork view.
  - In `ExpandedGuideMapModal`, labels scale and translate smoothly with pinch-to-zoom and pan gestures.
  - Added comprehensive unit test suite in `RegionMapLandmarksTest.kt` verifying non-empty landmark sets, towns & routes presence, normalized coordinate ranges (`[0.02, 0.98]`), and edition name lookups (100% pass rate).

## [1.8.1] - 2026-10-26

### Added
- **Comprehensive Strategy Guide Logic & Content Review**: Full audit and synchronization of Dexter's 12 Strategy Domains (`game_guides_index.json`), expanding total curated walkthrough topics from 57 to 84 (100% domain coverage).
  - Populated all 27 previously missing/mismatched topic IDs: `devon_scope`, `liberty_pass`, `dragon_scale`, `upgrade_dubious`, `protector`, `link_cable`, `life_orb`, `assault_vest`, `heavy_duty_boots`, `ability_patch`, `psyduck_headache`, `route_110_blockade`, `devon_goods_quest`, `silph_co_invasion`, `power_plant_part`, `dowsing_radar_spots`, `hidden_rare_candies`, `buried_fossils`, `daily_berry_trees`, `ruin_of_alph_puzzles`, `tanoby_ruins_cipher`, `abyssal_ruins_alphabet`, `ice_path_sliding_puzzles`, `lt_surge_trash_can_switches`, `silph_co_warp_pads`, `sabrina_gym_warp_maze`, and `sootopolis_gym_ice_tiles`.
  - Added full multi-region map pinpoint coordinate offsets in `PokemonGameGuideRepository.resolveMapCoordinates()` for Liberty Garden, Route 210 Psyduck Blockade, Rusturf Tunnel, Silph Co. HQ, Cerulean Gym Machine Part, and Tanoby Ruins.
  - Enriched Johto edition coverage for Snorlax (`Gold`, `Silver`, `Crystal`, `HeartGold`, `SoulSilver`) with Route 11 Vermilion City pin and EXPN Poké Flute radio broadcast instructions.
  - Added unit test suite in `PokemonGameGuideTest.kt` guaranteeing 100% topic ID resolution across all 12 categories and validating case-insensitive edition lookups.

### Fixed
- **Case-Insensitive Cartridge Candidate Key Resolution**: Fixed `PokemonGameGuideRepository.resolveVersionKey()` to sanitize spaces and perform case-insensitive lookups across `CARTRIDGE_CANDIDATE_KEYS`, preventing lowercased cartridge IDs from failing to resolve.
- **Category Topic ID Alignment & Alias Duplication**: Resolved dangling aliases in `healing_items`, `battle_items`, and puzzle categories, ensuring every declared category topic maps to an active topic record.

## [1.8.0] - 2026-10-26

### Added
- **Strategy Guide Quest Search Precedence & Pinpoint Radar**: Reordered search query evaluation in `PokemonGameGuideRepository.kt` so quest topics, items, and roadblocks are evaluated before competitive builds. Searching legendary or mythical Pokémon (e.g. "Mew", "Rayquaza", "Darkrai", "Arceus") now instantly routes to their canonical capture quests with active Town Map radar beacons and coordinates, reserving competitive builds for explicit queries ("build", "moveset", "nature", "ev").
- **Authentic Multi-Region Map Coordinate Mappings**: Expanded `resolveMapCoordinates()` across Kanto (GB & FRLG), Johto (GBC), Hoenn (GBA), and Sinnoh (DS) to support exact pinpoints for Faraway Island, Birth Island, Navel Rock, Southern Island, Newmoon Island, Flower Paradise, Spear Pillar, Seafoam Islands, Ilex Forest Shrine, Mossdeep Space Center White Rock, Desert Ruins, Island Cave, Ancient Tomb, Cave of Origin, Fullmoon Island, Snowpoint Temple, and Turnback Cave.
- **Synchronized Event Pokémon & Legendary Guide Index**: Populated and aligned all categories, topic IDs, and search aliases in `game_guides_index.json` for Old Sea Map (Mew), Aurora Ticket (Deoxys), Eon Ticket (Latias/Latios), Mystic Ticket (Lugia/Ho-Oh), Member Card (Darkrai), Oak's Letter (Shaymin), Azure Flute (Arceus), Ilex Forest Shrine (Celebi), and Mossdeep White Rock (Jirachi).
- **Foldable Dual-Pane Pokédex Hardware Hinge (`PokedexFoldHinge`)**: Added an authentic retro hardware hinge spine down the center fold crease on foldable devices (`maxWidth > 600.dp`, Pixel Fold unfolded), complete with metallic pivot cylinders and recessed chassis depth.
- **Unified Spanning Pokédex Header**: Elevated the Pokédex top bar (`PokedexHeader`) to span gracefully across both halves of the foldable chassis, featuring the Blue Scanner Lens and status lights on the left, a central `SILPH CO. DUAL-SCREEN POKÉDEX` badge, and quick Guide + Settings controls on the right.
- **Dual-Pane Active Selection Indicator**: Active Pokémon card in master index displays a vibrant cyan border (`2.5.dp`, `#00FFCC`) and an `ACTIVE ENTRY ▶` badge on foldable/tablet screens (`maxWidth > 600.dp`).
- **Authentic In-Game Pixel Town Maps (Native ROM Extracts)**:
  - **Pokémon Emerald (GBA)**: Extracted native 240x160 PokéNav Town Map from ROM decomp assets (`beater-studios/swg3`), integer-upscaled 8x nearest-neighbor to `1920x1280` with 27 hardware colors (`hoenn_rse.png`). Replaced blurry 3DS concept art.
  - **Pokémon Red & Blue (GB)**: Native 4-shade monochrome Game Boy Town Map (`1712x1440`). Default is authentic monochrome with 5 Game Boy hardware palette options (`Pocket (B&W)`, `DMG (Olive)`, `GBC Red`, `GBC Blue`, `Super GB`).
  - **Pokémon FireRed & LeafGreen (GBA)**: Extracted native 240x160 GBA Town Map from ROM decomp assets (`beater-studios/swg3`), integer-upscaled 8x nearest-neighbor to `1920x1280` with 27 hardware colors (`kanto_frlg.png`). Replaced 4:3 anime poster.
  - **Pokémon Gold, Silver & Crystal (GBC)**: Native 160x144 GBC Pokégear Town Map (`1976x1440`).
- **Game Boy Hardware Palette Switcher**: Interactive selector bar on Gen 1 maps allowing live hardware color switching between Pocket, DMG, GBC Red, GBC Blue, and Super Game Boy.
- **Retro 8-Bit Pixel-Art Item Sprites Engine**: 150+ authentic Gen 3/4/5 item sprites across Potions, Revives, Poké Balls, Key Items, and Evolution Stones, with nearest-neighbor crisp rendering.
- **Two-Tier Search Engine Overhaul**: Exact/phrase matching with keyword token overlap, stop word filtering, and numeric/accent normalization (`Flabébé`, `#025`, `golem`).
- **Nearest-Neighbor Scaling (`FilterQuality.None`)**: Applied nearest-neighbor rendering across all 2D pixel art maps in canvas, thumbnail, and modal zoom views.

### Fixed
- **Foldable Dual-Pane Vertical Asymmetry**: Resolved vertical misalignment where the left screen started ~96dp below the top header while the right detail pane started at the very top (`y=0`), and the right pane was truncated ~50dp at the bottom by a redundant `◀ INDEX / RESET` button. Master list and detail monitor now share identical top and bottom Y coordinates, identical height, and harmonized `8.dp` outer bezel padding.
- **Dual-Pane Master-Detail Desynchronization**: Resolved issue on foldable screens (`maxWidth > 600.dp`, Pixel Fold unfolded) where searching or filtering left the right detail pane stuck on a previously selected Pokémon (e.g. Rayquaza on left vs Bulbasaur on right). Injected real-time silent auto-synchronization (`LaunchedEffect`) keeping master and detail in sync without interrupting user input.
- **Edition & Map Desynchronization**: Fixed bug where selecting Red in the version picker still displayed Emerald's Hoenn map. Replaced early-exit on blank query with default helpful fallback search, ensuring immediate state and map updates when switching editions. Fixed `queryGym` fallback from picking Emerald's map on non-native item/gym queries.
- **Modal Persistence & Always-On Screen**: Injected `FLAG_KEEP_SCREEN_ON` and outside-touch protection (`dismissOnClickOutside = false`) to prevent unwanted dismissals.

## [Unreleased] - 2026-10-25

### Added
- **Comprehensive 1,025-Species Anime Pronunciation Engine**: Expanded `pokemon_pronunciations.json` to 1,834 phonetic mappings across Gen 1–9, including all 6 Legendary Titans (Regirock, Regice, Registeel, Regigigas, Regieleki, Regidrago), all 7 Hisui-native Pokémon (`#899–#905`), all 16 Hisuian regional forms, Paradox Pokémon, Treasures of Ruin, and Loyal Three.
- **Multi-Region Habitat Database (`pokemon_encounters.json` v3)**: Expanded wild habitat data to 4,729 regional habitat entries (`regionalHabitats` + `nativeRegions`) across Kanto, Johto, Hoenn, Sinnoh, Unova, Kalos, Alola, Galar, Hisui, and Paldea (including wild Paldea, Galar, and Alola habitats for returning species like Mudbray `#749` and Mudsdale `#750`).
- **Town Map ↔ Pokédex Entry & Voice Synchronization**: Switching regions or game cartridges in the Town Map modal or main index now synchronizes the active game edition, regional Pokédex entry drawer, and Dexter voice narration, with interactive `📍 <NATIVE REGION>` jump chips on `Area Unknown` overlays.

### Fixed
- **Multi-Word Phonetic Token Protection**: Added sentinel protection in `PokemonPronunciation.kt` to prevent single-word dictionary keys from corrupting already-replaced multi-word phrases, plus hyphenated sub-token resolution and default form suffix suppression in `PokemonFormDetector.kt`.
- **Cross-Region Habitat Pin Leakage**: Fixed `PokemonMapHabitatRepository` so primary debut-region pins never leak onto unrelated regional Town Maps when a Pokémon is not found in that region.
- **Flavor Text HTML Entity Cleanup**: Cleaned 242 raw `Pok&eacute;mon` HTML entities in `pokemon_data.json` and bumped Room database to v11.

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
