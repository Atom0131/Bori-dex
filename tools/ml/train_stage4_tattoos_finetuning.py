r"""
Stage 4 Universal Fine-Tuning Pipeline:
- Ingests all 578,000+ training images (Sprites, 3D prints, Sleep poses, Cutouts, Cards, and Tattoos).
- Introduces on-the-fly Synthetic Specular Glare (magazine reflections/lighting flares).
- Introduces on-the-fly Downward Perspective Keystoning & Tilt (off-axis smartphone camera angles).
- Resumes from Stage 2/3 checkpoint to preserve existing recognition capability.
- Auto-exports clean Float32 TFLite directly into Android app assets upon completion.
"""
import os
import math
import random
import tensorflow as tf

# Configure Keras import compatibility across TF versions
try:
    import tf_keras as keras
except ImportError:
    from tensorflow import keras

# Configure DirectML / GPU device visibility and memory growth
gpus = tf.config.list_physical_devices('GPU')
if gpus:
    try:
        tf.config.set_visible_devices(gpus[0], 'GPU')
        tf.config.experimental.set_memory_growth(gpus[0], True)
        print(f"[GPU] DirectML active on primary adapter: {gpus[0]}")
    except Exception as e:
        print(f"[GPU] Warning configuring GPU: {e}")

BASE_DIR = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\Pokemon_ref_imgs"
DATASET_DIR = os.path.join(BASE_DIR, "varaiants")
EXPORT_DIR = os.path.join(BASE_DIR, "exported_model")
os.makedirs(EXPORT_DIR, exist_ok=True)

IMAGE_SIZE = (224, 224)
BATCH_SIZE = 32

print(f"\n========================================================")
print(f" LOADING UNIVERSAL DATASET")
print(f" Source: {DATASET_DIR}")
print(f" Image Dimensions: {IMAGE_SIZE}")
print(f" Batch Size: {BATCH_SIZE}")
print(f"========================================================\n")

train_ds = keras.utils.image_dataset_from_directory(
    DATASET_DIR,
    validation_split=0.15,
    subset="training",
    seed=123,
    image_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE
)

val_ds = keras.utils.image_dataset_from_directory(
    DATASET_DIR,
    validation_split=0.15,
    subset="validation",
    seed=123,
    image_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE
)

class_names = train_ds.class_names
num_classes = len(class_names)
print(f"[+] Successfully loaded {num_classes} Pokemon classes.")


# ---------------------------------------------------------------------------
# SYNTHETIC AUGMENTATIONS: GLARE & DOWNWARD PERSPECTIVE KEYSTONING
# ---------------------------------------------------------------------------

@tf.function
def apply_specular_glare(images):
    """
    Simulates glossy magazine and skin glare by injecting an elliptical
    Gaussian hotspot with desaturating brightness blowout.
    """
    # 30% chance per batch
    if tf.random.uniform([]) > 0.30:
        return images

    cx = tf.random.uniform([], 0.15, 0.85)
    cy = tf.random.uniform([], 0.15, 0.85)
    sigma_x = tf.random.uniform([], 0.08, 0.25)
    sigma_y = tf.random.uniform([], 0.08, 0.25)
    intensity = tf.random.uniform([], 70.0, 180.0)

    x = tf.linspace(0.0, 1.0, IMAGE_SIZE[0])
    y = tf.linspace(0.0, 1.0, IMAGE_SIZE[1])
    xx, yy = tf.meshgrid(x, y)

    flare = tf.exp(-(((xx - cx) ** 2) / (2.0 * sigma_x ** 2) + ((yy - cy) ** 2) / (2.0 * sigma_y ** 2)))
    flare = tf.expand_dims(tf.expand_dims(flare, axis=0), axis=-1) * intensity

    # Apply additive glare flare
    return tf.clip_by_value(images + flare, 0.0, 255.0)


@tf.function
def apply_perspective_keystoning(images):
    """
    Simulates downward camera pitch (pointing smartphone downward at a magazine,
    desk, or arm) via 3D projective foreshortening transform.
    """
    # 35% chance per batch
    if tf.random.uniform([]) > 0.35:
        return images

    batch_len = tf.shape(images)[0]
    # Random keystoning compression factor (0.0004 to 0.0010 creates realistic 15-45 deg tilt)
    k_y = tf.random.uniform([], 0.0003, 0.0009)
    # Random sign: downward tilt (camera above) or upward tilt
    direction = tf.cond(tf.random.uniform([]) > 0.15, lambda: 1.0, lambda: -1.0)
    c1 = k_y * direction

    # Small random lateral shear for off-axis angle
    c0 = tf.random.uniform([], -0.0003, 0.0003)

    single_transform = tf.stack([1.0, 0.0, 0.0, 0.0, 1.0, 0.0, c0, c1])
    transforms = tf.repeat(tf.expand_dims(single_transform, axis=0), repeats=batch_len, axis=0)

    transformed = tf.raw_ops.ImageProjectiveTransformV2(
        images=images,
        transforms=transforms,
        output_shape=tf.constant(list(IMAGE_SIZE), dtype=tf.int32),
        interpolation="BILINEAR"
    )
    return transformed


@tf.function
def apply_dark_dim_lighting(images):
    """
    Simulates dim indoor night environments (20-100 lux), bedside lighting,
    underexposure, non-linear shadow gamma falloff, and high-ISO sensor grain.
    """
    # 45% chance per batch
    if tf.random.uniform([]) > 0.45:
        return images

    # 1. Photometric Dimming (scale down by 0.45 - 0.85)
    dim_factor = tf.random.uniform([], 0.45, 0.85)
    dimmed = images * dim_factor

    # 2. Non-linear Shadow Gamma Falloff (gamma 1.15 to 1.70 crushes deep room shadows)
    gamma = tf.random.uniform([], 1.15, 1.70)
    norm = dimmed / 255.0
    gamma_adjusted = tf.pow(tf.maximum(norm, 1e-4), gamma) * 255.0

    # 3. High-ISO Low-Light Sensor Grain / Noise
    noise_sigma = tf.random.uniform([], 1.5, 6.0)
    noise = tf.random.normal(shape=tf.shape(images), mean=0.0, stddev=noise_sigma)

    return tf.clip_by_value(gamma_adjusted + noise, 0.0, 255.0)


@tf.function
def apply_warm_yellow_lighting(images):
    """
    Simulates 2400K-3200K warm indoor incandescent, bedside lamp, and warm-white LED lighting.
    Amplifies Red and Green channels while attenuating Blue, shifting chroma towards yellow/amber.
    """
    # 45% chance per batch
    if tf.random.uniform([]) > 0.45:
        return images

    r_scale = tf.random.uniform([], 1.10, 1.35)
    g_scale = tf.random.uniform([], 1.02, 1.18)
    b_scale = tf.random.uniform([], 0.60, 0.85)

    scales = tf.stack([r_scale, g_scale, b_scale])
    scales = tf.reshape(scales, [1, 1, 1, 3])

    # Subtle ambient warm incandescent glow tint
    tint_bias = tf.random.uniform([], 0.0, 12.0)
    tint = tf.constant([1.2, 1.0, 0.4]) * tint_bias
    tint = tf.reshape(tint, [1, 1, 1, 3])

    return tf.clip_by_value(images * scales + tint, 0.0, 255.0)


@tf.function
def augment_batch(images, labels):
    """Combined pipeline: Perspective, Glare, Dim/Low-Light, Warm Yellow Cast, Flip, and Contrast."""
    images = tf.image.random_flip_left_right(images)
    images = apply_perspective_keystoning(images)
    images = apply_specular_glare(images)
    images = apply_dark_dim_lighting(images)
    images = apply_warm_yellow_lighting(images)
    images = tf.image.random_contrast(images, lower=0.85, upper=1.20)
    images = tf.image.random_brightness(images, max_delta=20.0)
    images = tf.clip_by_value(images, 0.0, 255.0)
    return images, labels


# Apply augmentation and autotune prefetch
train_ds = train_ds.map(augment_batch, num_parallel_calls=tf.data.AUTOTUNE)
train_ds = train_ds.repeat().shuffle(1024).prefetch(buffer_size=tf.data.AUTOTUNE)
val_ds = val_ds.repeat().prefetch(buffer_size=tf.data.AUTOTUNE)


# ---------------------------------------------------------------------------
# RESUME MODEL CHECKPOINT & COMPILE
# ---------------------------------------------------------------------------

checkpoint_stage3_keras = os.path.join(EXPORT_DIR, "checkpoint.keras")
checkpoint_stage3_h5 = os.path.join(EXPORT_DIR, "checkpoint.h5")

checkpoint_stage4_keras = os.path.join(EXPORT_DIR, "checkpoint_stage4.keras")
checkpoint_stage4_h5 = os.path.join(EXPORT_DIR, "checkpoint_stage4.h5")

if os.path.exists(checkpoint_stage3_h5):
    print(f"[+] Resuming from previous checkpoint: {checkpoint_stage3_h5}")
    model = keras.models.load_model(checkpoint_stage3_h5)
elif os.path.exists(checkpoint_stage3_keras):
    print(f"[+] Resuming from previous checkpoint: {checkpoint_stage3_keras}")
    model = keras.models.load_model(checkpoint_stage3_keras)
else:
    raise FileNotFoundError("Could not find previous stage checkpoint in exported_model/ to resume from!")

# Ensure base layers beyond index 80 are unfrozen for deep fine-tuning
base_model = model.layers[2]
base_model.trainable = True
fine_tune_at = 80
for layer in base_model.layers[:fine_tune_at]:
    layer.trainable = False

STAGE4_LR = 1.5e-5
print(f"[+] Configured Stage 4 Fine-Tuning Learning Rate: {STAGE4_LR}")
print(f"[+] Unfrozen layers from index {fine_tune_at} to {len(base_model.layers)} ({len(base_model.layers) - fine_tune_at} layers active)")

model.compile(
    optimizer=keras.optimizers.Adam(learning_rate=STAGE4_LR),
    loss=keras.losses.SparseCategoricalCrossentropy(),
    metrics=['accuracy']
)

# Callbacks: Save best weights, reduce learning rate on plateaus, early stopping
callbacks = [
    keras.callbacks.ModelCheckpoint(
        filepath=checkpoint_stage4_keras,
        monitor='val_accuracy',
        save_best_only=True,
        verbose=1
    ),
    keras.callbacks.ModelCheckpoint(
        filepath=checkpoint_stage4_h5,
        monitor='val_accuracy',
        save_best_only=True,
        verbose=1
    ),
    keras.callbacks.ReduceLROnPlateau(
        monitor='val_accuracy',
        factor=0.5,
        patience=3,
        min_lr=1e-7,
        verbose=1
    ),
    keras.callbacks.EarlyStopping(
        monitor='val_accuracy',
        patience=7,
        restore_best_weights=True,
        verbose=1
    )
]

# High-Quality Training Cadence: 4,000 steps/epoch = 128,000 images per epoch
STEPS_PER_EPOCH = 4000
VALIDATION_STEPS = 250
TOTAL_EPOCHS = 30

print(f"\n========================================================")
print(f" STARTING HIGH-QUALITY STAGE 4 FINE-TUNING")
print(f" - Steps per Epoch: {STEPS_PER_EPOCH} (128,000 samples/epoch)")
print(f" - Validation Steps: {VALIDATION_STEPS} (8,000 validation samples/epoch)")
print(f" - Total Epochs: {TOTAL_EPOCHS}")
print(f" - Early Stopping Patience: 7 epochs")
print(f" - Unfrozen Base Layers: {len(base_model.layers) - fine_tune_at}")
print(f" - Specular Glare Augmentation: ACTIVE")
print(f" - Downward Perspective Keystoning: ACTIVE")
print(f" - Dark / Low-Light Shadow Gamma: ACTIVE")
print(f" - Warm Yellow Tungsten Cast: ACTIVE")
print(f"========================================================\n")

model.fit(
    train_ds,
    validation_data=val_ds,
    steps_per_epoch=STEPS_PER_EPOCH,
    validation_steps=VALIDATION_STEPS,
    epochs=TOTAL_EPOCHS,
    callbacks=callbacks
)

# Save final full model
final_keras_path = os.path.join(EXPORT_DIR, "model_stage4.keras")
model.save(final_keras_path)
print(f"\n[+] Full Stage 4 Keras model saved to {final_keras_path}")

# Run automatic Float32 TFLite export
print("\n[+] Triggering automatic Float32 TFLite conversion & Android asset injection...")
import export_stage4_float32
print("\n[✓] Stage 4 training, TFLite conversion, and Android deployment completed successfully!")
