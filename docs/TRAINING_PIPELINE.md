# 🧠 Bori-dex Machine Learning & Training Pipeline

This document provides a complete technical deep-dive into the computer vision classification system powering the **Bori-dex** on-device scanner.

---

## 1. System Overview

Bori-dex uses a custom-trained **MobileNetV2** deep convolutional neural network deployed on-device via **TensorFlow Lite**. The model identifies **all 1,025 canonical Pokémon species** (Generation 1 through Generation 9) entirely offline with zero network latency.

```
                    ┌─────────────────────────┐
                    │ CameraX 1:1 Square Crop │
                    │     (224 x 224 x 3)     │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │   Native Direct JNI     │
                    │   ByteBuffer (Float32)  │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │   MobileNetV2 Float32   │
                    │    Inference Engine     │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │ 22-Frame Consensus Vote │
                    │   Rolling Averaging     │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │ Locked Pokémon #ID & UI │
                    │ Dexter Anime Narration  │
                    └─────────────────────────┘
```

---

## 2. Master Dataset Breakdown (494,768 Images)

The master training dataset was constructed to prevent overfitting to sterile 2D digital artwork and teach the network physical real-world object invariance:

| Dataset Category | Image Count | Real-World Purpose |
| :--- | :---: | :--- |
| **Ken Sugimori Official Art** | ~4,500 | Canonical anatomical baselines and color palettes for all 1,025 species. |
| **DreamWorld & Global Link** | ~3,200 | Clean vector silhouettes and high-contrast lines. |
| **High-Resolution Anime Frames** | ~35,000 | Dynamic battle stances, facial expressions, and varying aspect ratios. |
| **TCG Card Artwork** | ~45,000 | Foil gradients, angled backgrounds, and varied artistic interpretations. |
| **Physical 3D Prints (`varaiants_3d_prints`)** | ~180,000 | Raw resin and filament prints without paint, forcing the model to classify by **geometric silhouette and surface contours** rather than color alone. |
| **Sleep Series & Figurines (`varaiants_sleep`)** | ~120,000 | Curled-up poses, desk clutter, and physical toy textures. |
| **Lighting & Augmentation Variations** | ~107,000 | Room shadows, fluorescent overhead lighting, desk reflections, and camera glare. |
| **TOTAL** | **494,768** | **Master multi-variant dataset across 1,025 classes** |

### 🌐 Kaggle Dataset Transparency
The complete dataset is shared publicly for open-source verification on Kaggle Datasets:
- **Kaggle Dataset**: [`atom0131/boridex-pokemon-master-dataset-494k`](https://www.kaggle.com/datasets/atom0131/boridex-pokemon-master-dataset-494k)
- **Preparation & Upload Script**: Located in [`tools/ml/prepare_kaggle_dataset.py`](../tools/ml/prepare_kaggle_dataset.py).

---

## 3. Multi-Stage Fine-Tuning Journey

The model was developed through three progressive training phases:

### Phase Comparison

| Stage | Dataset Size | Training Strategy | Single-Frame Top-1 | Top-5 Accuracy | Real-World In-App Success |
| :--- | :---: | :--- | :---: | :---: | :---: |
| **Vanilla Baseline** | Generic ImageNet | Pre-trained feature extractor only | `< 10%` | `< 25%` | Fails completely on Pokémon |
| **Stage 1 (Head Tuning)** | ~30,000 images | Frozen backbone; trained Dense classification head | `41.2%` | `68.5%` | `~35% – 40%` (2D screens only) |
| **Stage 2 (Deep Backbone)** | ~120,000 images | Unfroze layers 100+; initial toys & sleep variants | `57.68%` | `84.2%` | `~75% – 80%` (clean merchandise) |
| **Stage 3 (Master Dataset)** | **494,768 images** | Deep fine-tuning on hardened 3D prints, dynamic lighting & cards | **`53.71%`** *(hardened)* | **`86.4%`** | **`~91% – 93%` (all physical items)** |

### Stage 3 Convergence Details
- **Platform**: Intel Core i7-10750H with oneDNN / AVX2 CPU acceleration.
- **Environment**: Python 3.10 with `tf_keras 2.21.0`.
- **Batch Size**: 32 (optimal memory locality and CPU cache utilization).
- **Cadence**: 2,500 steps/epoch (~80,000 images sampled per epoch) at ~0.48s/step (~20 min/epoch).
- **Validation**: 150 steps (~4,800 images evaluated per epoch).
- **Optimizer**: Adam with learning rate reduction (`ReduceLROnPlateau` from `1.5e-5` down to `1.875e-6`).
- **Early Stopping**: Triggered at **Epoch 13**, automatically restoring peak weights from **Epoch 8** (`val_accuracy: 53.71%`, `val_loss: 2.8309`).

---

## 4. Float32 vs. INT8 Quantization Guardrails

A critical finding in our testing:
1. **The Flaw of INT8 Post-Training Quantization on 1,025 Classes**:
   - In a 1,025-class classifier, baseline uniform probability is $1 / 1025 \approx 0.098\%$.
   - Quantizing weights and activations to 8-bit integers (`0..255`) caused an empirical **6x confidence collapse** (Pikachu dropped from 80.14% down to 13.28%, falling below the noise floor).
2. **The Float32 Solution**:
   - The final model is exported in **full-precision Float32** (`14.1 MB`).
   - Mobile devices easily cache and execute a 14.1 MB model in RAM with sub-25ms inference latency.
   - Preserves continuous floating-point probability distributions without rounding loss.
3. **Native Direct JNI Pipeline**:
   - Input: `[1, 224, 224, 3] Float32` via `ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)` (602,112 bytes).
   - Output: `[1, 1025] Float32` (4,100 bytes).

---

## 5. The 22-Frame Temporal Consensus Voter

Why is the real-world scanning success rate **~91%** when raw single-frame accuracy is 53.71%?

In [`PokedexViewModel.kt`](../app/src/main/java/com/example/pokedex/viewmodel/PokedexViewModel.kt):
1. **Rolling Temporal Accumulation**: The app collects prediction probability vectors across **22 consecutive frames** (~3 seconds at ~7.5 FPS).
2. **Noise Cancellation**: Random single-frame misclassifications due to hand tremble, glare, or motion blur cancel out mathematically ($\mathbb{E}[\text{noise}] \to 0$).
3. **Signal Amplification**: The true Pokémon features reinforce each other across frames into a dominant probability spike.
4. **Lock-In Criteria**:
   - Top averaged probability $\ge 0.20$ (20%, >200x above random chance).
   - Margin $\ge 1.25\times$ over the runner-up prediction.

---

## 6. How to Reproduce Training

All scripts are committed under [`tools/ml/`](../tools/ml/):

1. **Scrape Raw Data**:
   ```bash
   python tools/ml/scrape_dataset.py
   python tools/ml/scrape_dataset_3d_prints.py
   python tools/ml/scrape_dataset_pokemon_sleep.py
   python tools/ml/merge_all_datasets.py
   ```

2. **Execute Stage 3 Fine-Tuning**:
   ```bash
   python tools/ml/train_stage3_finetuning.py
   ```

3. **Export Float32 TFLite Model**:
   ```bash
   python tools/ml/export_stage3_float32.py
   ```
