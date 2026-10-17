# How Bori-dex Was Trained: The Machine Learning & Vision Pipeline

> **App Version**: Bori-dex v1.3.0  
> **Model Checkpoint**: Stage 3 Master Model (`stage3_model_float32.tflite`)  
> **Target Classes**: 1,025 Pokémon Species (Generations 1 through 9)  
> **Dataset Size**: 494,768 Curated Images  
> **Inference Speed**: ~18–25 ms per frame on-device (Android GPU/NNAPI)

---

## 1. Overview & Architecture

Bori-dex uses an on-device computer vision classifier powered by a fine-tuned **MobileNet** backbone running through TensorFlow Lite. Unlike typical cloud-based AI that sends camera frames to a remote server, Bori-dex executes **100% locally on your smartphone** using native JNI C++ bindings and direct memory buffers.

```
┌─────────────────┐       ┌────────────────────────┐       ┌────────────────────────┐
│  CameraX View   │ ────► │  Bitmap Preprocessing  │ ────► │  Float32 Direct Buffer │
│  (Real-Time)    │       │  (224x224 Bilinear)    │       │  (602,112 bytes)       │
└─────────────────┘       └────────────────────────┘       └───────────┬────────────┘
                                                                       │
┌─────────────────────────┐       ┌────────────────────────┐           ▼
│ Top-5 Smooth Confidence │ ◄──── │ Softmax Output Buffer  │ ◄──── ┌────────────────┐
│ (~91% in-app accuracy)  │       │ (1,025 Float32 Prob.)  │       │ TFLite Runtime │
└─────────────────────────┘       └────────────────────────┘       └────────────────┘
```

---

## 2. The Foundation: Transfer Learning

Training a neural network from scratch to recognize 1,025 fine-grained Pokémon species would typically require millions of training images and months of compute. Instead, we used **Transfer Learning**:

1. **Pre-trained Feature Extractor**: We started with MobileNet pre-trained on ImageNet (a massive dataset of millions of everyday objects like cars, animals, and furniture).
2. **Universal Low-Level Features**: At the base levels, the neural network already understood:
   * Line orientations and high-contrast edges
   * Color gradients and textures
   * Basic organic shapes (eyes, ears, snouts, paws, wings)
3. **Custom Pokémon Head**: We replaced ImageNet's 1,000 generic classes with a specialized Dense classifier head outputting probabilities across all **1,025 Pokémon species**.

---

## 3. The Dataset: 494,768 Master Images

A vision model is only as smart as the variety of situations it has encountered. If an AI is only trained on flat 2D anime illustrations, it will fail when pointed at a physical plastic toy on a wooden table.

To make Bori-dex work in the real world, we synthesized and merged a **494,768-image master dataset** across four key domains:

| Data Category | Source / Description | Why It Was Needed |
| :--- | :--- | :--- |
| **Official Canonical Assets** | Sugimori art, Pokémon HOME 3D renders, and animated sprites. | Establishes the ground-truth anatomical proportions and official color palettes. |
| **Real-World Photos** | Pokémon plushies, action figures, trading cards, and vinyl figures. | Teaches the model to recognize real-world physical materials (fabric, plastic, glossy cardstock). |
| **Unpainted 3D Prints** | Raw resin, matte PLA, grey primer, and unfinished desktop prints. | **Crucial Breakthrough**: Eliminates color dependency, forcing the network to identify Pokémon purely by **silhouette, geometry, and anatomical features**. |
| **Pokémon Sleep Poses** | Figures lying down, curled up, snoozing, or photographed from above. | Prevents model confusion when a physical toy or figurine is not standing in a standard fighting stance. |

---

## 4. Data Augmentation: Real-Time Camera Simulation

During training, no image was fed into the network in its raw form. The data loader applied randomized transforms on-the-fly:

* **Photometric Jitter**: Random shifts in brightness, contrast, saturation, and hue (simulating dark bedrooms, fluorescent office lights, and outdoor sunshine).
* **Geometric Transforms**: Random rotations (±25°), scaling (0.8x to 1.2x), horizontal flips, and perspective shearing.
* **Synthetic Occlusion (Cutout)**: Random rectangular patches of the image were erased to teach the model how to identify a Pokémon even if the user's fingers or hands are partially blocking it.
* **Gaussian Blur & Noise**: Simulating camera motion blur and low-light sensor grain.

---

## 5. The 3-Stage Training Pipeline

To adapt the model without causing "catastrophic forgetting" (where the model loses its general vision understanding), training was executed in three distinct stages:

```
┌────────────────────────────────────────────────────────────────────────┐
│ STAGE 1: Head Warmup                                                   │
│ • Base model frozen; only top Dense layer trains.                      │
│ • Learning Rate: 1e-3                                                  │
│ • Goal: Align random classifier weights with existing visual features. │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│ STAGE 2: Domain Adaptation                                             │
│ • Upper 40% of backbone unfrozen.                                      │
│ • Trained on initial toy/figure photos and 3D renders.                 │
│ • Learning Rate: 1e-4                                                  │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│ STAGE 3: Deep Fine-Tuning (The 494k Overnight Run)                     │
│ • Entire network unfrozen with Cosine Decay learning rate.             │
│ • 494,768 images including 3D prints and Pokémon Sleep poses.          │
│ • Loss: Categorical Cross-Entropy with Label Smoothing (0.1).          │
│ • Early Stopping: Checkpoint saved at peak validation (Epoch 8).       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 6. Training Dynamics & Convergence

During Stage 3, the training script evaluated validation loss and accuracy after every epoch:

* **Optimizer**: Adam with Cosine Decay learning rate schedule.
* **Label Smoothing (0.1)**: Instead of forcing the model to be 100% confident in one class, label smoothing distributes 10% of the probability mass across other classes. This prevents overconfidence on visually ambiguous species.
* **Early Stopping**: The run converged and was automatically stopped at **Epoch 13**, with the model restoring its best checkpoint from **Epoch 8**:
  * **Validation Loss**: `2.8309` (all-time low)
  * **Top-1 Accuracy**: `53.71%`
  * **Top-5 Accuracy**: `86.40%`

---

## 7. Understanding the Accuracy Numbers

In machine learning, accuracy across **1,025 fine-grained classes** has very different characteristics than typical 10-class benchmarks:

* **Random Guessing**: $1 / 1,025 = 0.098\%$
* **Top-1 Accuracy (53.71%)**: More than 1 out of every 2 frames, the model's single highest guess is 100% correct across all 1,025 possibilities.
* **Top-5 Accuracy (86.40%)**: In over 86% of frames, the true Pokémon is in the top 5 candidates.
* **In-App Real-World Performance (~91%)**: Inside the Android app, `CameraX` captures 30 frames per second. Bori-dex applies **Temporal Confidence Smoothing** across consecutive frames. Transient noise and ambiguous frames are averaged out, resulting in a smooth, confident identification in ~91% of real-world scans.

---

## 8. Exporting to Android: The Float32 JNI Solution

Once trained in Python/TensorFlow, the model had to be converted into a format executable on mobile devices.

### The INT8 Quantization Trap
Many mobile tutorials recommend 8-bit integer quantization (`INT8`) to compress models down to 3–4 MB. When applied to 1,025 classes, INT8 causes a **6x confidence collapse**:
* Subtle numerical differences between closely related species get rounded away.
* Top predictions dropped from 85–95% confidence down to 10–15%, making the scanner hesitant and jittery.

### The Float32 Direct JNI Architecture
We exported the model as **full 32-bit floating point (`Float32`)**:
* **File Size**: 14.1 MB (`stage3_model_float32.tflite`).
* **Memory Buffer**: Exactly $1 \times 224 \times 224 \times 3 \times 4 \text{ bytes} = 602,112 \text{ bytes}$.
* **Direct JNI Buffer Allocation**: In `TFLiteClassifier.kt`, Bori-dex allocates a native, direct `ByteBuffer` in C-space memory with `ByteOrder.nativeOrder()`. The CameraX frame is copied directly into this buffer without JVM garbage collection overhead.
* **Performance**: Sub-25ms inference time, running seamlessly on Android NNAPI and GPU delegates.

---

## 9. Reproducibility & Open Source Tools

All scripts used to create, merge, train, and export this model are fully versioned in the project repository under `tools/ml/`:

* [`scrape_dataset.py`](file:///C:/Users/pagan/OneDrive/Documents/Code/Pokedex/PokedexApp/tools/ml/scrape_dataset.py): Fetches official canonical artwork and sprites.
* [`scrape_dataset_3d_prints.py`](file:///C:/Users/pagan/OneDrive/Documents/Code/Pokedex/PokedexApp/tools/ml/scrape_dataset_3d_prints.py): Curates unpainted resin and PLA 3D model renders.
* [`scrape_dataset_pokemon_sleep.py`](file:///C:/Users/pagan/OneDrive/Documents/Code/Pokedex/PokedexApp/tools/ml/scrape_dataset_pokemon_sleep.py): Downloads sleeping/curled poses.
* [`merge_all_datasets.py`](file:///C:/Users/pagan/OneDrive/Documents/Code/Pokedex/PokedexApp/tools/ml/merge_all_datasets.py): Deduplicates and balances the 494k dataset into standardized class directories.
* [`train_stage3_finetuning.py`](file:///C:/Users/pagan/OneDrive/Documents/Code/Pokedex/PokedexApp/tools/ml/train_stage3_finetuning.py): Runs the full Stage 3 deep fine-tuning pipeline.
* [`export_stage3_float32.py`](file:///C:/Users/pagan/OneDrive/Documents/Code/Pokedex/PokedexApp/tools/ml/export_stage3_float32.py): Converts the best trained Keras model into the production Float32 `.tflite` file.
* [`prepare_kaggle_dataset.py`](file:///C:/Users/pagan/OneDrive/Documents/Code/Pokedex/PokedexApp/tools/ml/prepare_kaggle_dataset.py): Packages metadata and directory manifests for public Kaggle dataset hosting (`atom0131/boridex-pokemon-master-dataset-494k`).
