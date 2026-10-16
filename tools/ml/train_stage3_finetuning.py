import os
import tensorflow as tf
import tf_keras as keras

# Target the discrete NVIDIA GPU (GTX 1660 Ti via DirectML) and enable memory growth
gpus = tf.config.list_physical_devices('GPU')
if gpus:
    try:
        # Use GPU 0 (NVIDIA GTX 1660 Ti)
        tf.config.set_visible_devices(gpus[0], 'GPU')
        tf.config.experimental.set_memory_growth(gpus[0], True)
        print(f"DirectML active on: {gpus[0]}")
    except RuntimeError as e:
        print(e)

base_dir = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\Pokemon_ref_imgs"
dataset_dir = os.path.join(base_dir, "varaiants")
export_dir = os.path.join(base_dir, "exported_model")
os.makedirs(export_dir, exist_ok=True)

IMAGE_SIZE = (224, 224)
BATCH_SIZE = 32  # Optimal CPU memory and cache locality on i7-10750H with oneDNN/AVX2

print(f"Loading master dataset from: {dataset_dir}")

train_ds = keras.utils.image_dataset_from_directory(
    dataset_dir,
    validation_split=0.2,
    subset="training",
    seed=123,
    image_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE
)

val_ds = keras.utils.image_dataset_from_directory(
    dataset_dir,
    validation_split=0.2,
    subset="validation",
    seed=123,
    image_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE
)

class_names = train_ds.class_names
num_classes = len(class_names)
print(f"Found {num_classes} classes.")

# Performance: AUTOTUNE prefetching keeps compute pipe 100% saturated
train_ds = train_ds.repeat().shuffle(1024).prefetch(buffer_size=tf.data.AUTOTUNE)
val_ds = val_ds.repeat().prefetch(buffer_size=tf.data.AUTOTUNE)

checkpoint_path = os.path.join(export_dir, "checkpoint.keras")
checkpoint_h5_path = os.path.join(export_dir, "checkpoint.h5")

# 1. Load the model from best Stage 2 checkpoint via tf_keras
if os.path.exists(checkpoint_h5_path):
    print(f"Resuming Stage 3 Fine-Tuning from H5: {checkpoint_h5_path}")
    model = keras.models.load_model(checkpoint_h5_path)
elif os.path.exists(checkpoint_path):
    print(f"Resuming Stage 3 Fine-Tuning directly from: {checkpoint_path}")
    model = keras.models.load_model(checkpoint_path)
else:
    raise FileNotFoundError("Could not find previous stage checkpoint to resume from!")

# Ensure base model unfreezing is active (layers beyond 100)
base_model = model.layers[2]
base_model.trainable = True
fine_tune_at = 100
for layer in base_model.layers[:fine_tune_at]:
    layer.trainable = False

# Calibrate learning rate for fine-tuning
STAGE3_LR = 1.5e-5
print(f"Setting active Stage 3 learning rate to: {STAGE3_LR}")

model.compile(
    optimizer=keras.optimizers.Adam(learning_rate=STAGE3_LR),
    loss=keras.losses.SparseCategoricalCrossentropy(),
    metrics=['accuracy']
)

# Callbacks: Save best checkpoint to both .keras and .h5 formats
checkpoint_callback_keras = keras.callbacks.ModelCheckpoint(
    filepath=checkpoint_path,
    monitor='val_accuracy',
    save_best_only=True,
    verbose=1
)

checkpoint_callback_h5 = keras.callbacks.ModelCheckpoint(
    filepath=checkpoint_h5_path,
    monitor='val_accuracy',
    save_best_only=True,
    verbose=1
)

reduce_lr = keras.callbacks.ReduceLROnPlateau(
    monitor='val_accuracy',
    factor=0.5,
    patience=2,
    min_lr=1e-7,
    verbose=1
)

early_stopping = keras.callbacks.EarlyStopping(
    monitor='val_accuracy',
    patience=5,
    restore_best_weights=True,
    verbose=1
)

# Optimized cadence: 2,500 steps/epoch = 80,000 images per epoch (~20-22 min)
# Validation capped at 150 steps = 4,800 images evaluated in ~1 min
STEPS_PER_EPOCH = 2500
VALIDATION_STEPS = 150
TOTAL_EPOCHS = 25

print(f"\n========================================================")
print(f" STARTING OPTIMIZED STAGE 3 FINE-TUNING")
print(f" - Batch Size: {BATCH_SIZE}")
print(f" - Steps per Epoch: {STEPS_PER_EPOCH} (80,000 images/epoch)")
print(f" - Validation Steps: {VALIDATION_STEPS} (4,800 samples/epoch)")
print(f" - Total Epochs: {TOTAL_EPOCHS}")
print(f" - Pipeline: tf.data.AUTOTUNE prefetch active")
print(f"========================================================\n")

model.fit(
    train_ds,
    validation_data=val_ds,
    steps_per_epoch=STEPS_PER_EPOCH,
    validation_steps=VALIDATION_STEPS,
    epochs=TOTAL_EPOCHS,
    callbacks=[checkpoint_callback_keras, checkpoint_callback_h5, reduce_lr, early_stopping]
)

# Save final full model
keras_path = os.path.join(export_dir, "model.keras")
model.save(keras_path)
print(f"\nRaw Keras model saved to {keras_path}")

# Run automatic INT8 export
print("\nExporting final quantized INT8 TFLite model...")
import export_checkpoint_quantized
print("\nStage 3 training and TFLite export completed successfully!")
