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
def augment_batch(images, labels):
    """Combined pipeline: Glare, Perspective, Flip, and Contrast."""
    images = tf.image.random_flip_left_right(images)
    images = apply_perspective_keystoning(images)
    images = apply_specular_glare(images)
    images = tf.image.random_contrast(images, lower=0.85, upper=1.20)
    images = tf.image.random_brightness(images, max_delta=25.0)
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

# Ensure base layers beyond index 100 are unfrozen for fine-tuning
base_model = model.layers[2]
base_model.trainable = True
fine_tune_at = 100
for layer in base_model.layers[:fine_tune_at]:
    layer.trainable = False

STAGE4_LR = 1.5e-5
print(f"[+] Configured Stage 4 Fine-Tuning Learning Rate: {STAGE4_LR}")

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
        patience=2,
        min_lr=1e-7,
        verbose=1
    ),
    keras.callbacks.EarlyStopping(
        monitor='val_accuracy',
        patience=5,
        restore_best_weights=True,
        verbose=1
    )
]

# Optimal training cadence: 2,500 steps/epoch = 80,000 images/epoch
STEPS_PER_EPOCH = 2500
VALIDATION_STEPS = 150
TOTAL_EPOCHS = 20

print(f"\n========================================================")
print(f" STARTING STAGE 4 FINE-TUNING")
print(f" - Steps per Epoch: {STEPS_PER_EPOCH} (80,000 samples/epoch)")
print(f" - Validation Steps: {VALIDATION_STEPS} (4,800 validation samples/epoch)")
print(f" - Total Epochs: {TOTAL_EPOCHS}")
print(f" - Specular Glare Augmentation: ACTIVE")
print(f" - Downward Perspective Keystoning: ACTIVE")
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
