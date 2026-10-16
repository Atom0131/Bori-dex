import os
os.environ["CUDA_VISIBLE_DEVICES"] = "-1"
os.environ["TF_ENABLE_ONEDNN_OPTS"] = "0"

import tensorflow as tf
tf.config.set_visible_devices([], 'GPU')
import tf_keras as keras
import shutil

BASE_DIR = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\Pokemon_ref_imgs"
CHECKPOINT_PATH = os.path.join(BASE_DIR, "exported_model", "checkpoint.keras")
CHECKPOINT_H5 = os.path.join(BASE_DIR, "exported_model", "checkpoint.h5")
EXPORT_TFLITE = os.path.join(BASE_DIR, "exported_model", "stage3_model_float32.tflite")
APP_ASSETS = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\assets"

IMAGE_SIZE = (224, 224)

# Load checkpoint (prefer h5/keras via tf_keras)
if os.path.exists(CHECKPOINT_H5):
    print(f"Loading checkpoint from {CHECKPOINT_H5}...")
    model = keras.models.load_model(CHECKPOINT_H5)
else:
    print(f"Loading checkpoint from {CHECKPOINT_PATH}...")
    model = keras.models.load_model(CHECKPOINT_PATH)

print("Model loaded successfully!")

print("Building clean inference architecture...")
inputs = keras.Input(shape=(*IMAGE_SIZE, 3))
x = keras.layers.Rescaling(1./127.5, offset=-1)(inputs)
base_model = keras.applications.MobileNetV2(input_shape=(*IMAGE_SIZE, 3), include_top=False, weights=None)
x = base_model(x, training=False)
x = keras.layers.GlobalAveragePooling2D()(x)
x = keras.layers.Dropout(0.3)(x)
outputs = keras.layers.Dense(1025, activation='softmax')(x)
inference_model = keras.Model(inputs, outputs)

# Copy weights from checkpoint model
inference_model.layers[2].set_weights(model.layers[2].get_weights())
inference_model.layers[5].set_weights(model.layers[5].get_weights())
print("Weights transferred to clean inference model!")

print("Converting to full precision Float32 TFLite (matches TFLiteClassifier native buffer)...")
converter = tf.lite.TFLiteConverter.from_keras_model(inference_model)
# Do NOT quantize: Keep full Float32 precision for input, weights, and output!
tflite_model = converter.convert()

with open(EXPORT_TFLITE, "wb") as f:
    f.write(tflite_model)
print(f"Exported Float32 model to {EXPORT_TFLITE} ({len(tflite_model)} bytes)")

# Copy directly to Android app assets
dest_model = os.path.join(APP_ASSETS, "model.tflite")
shutil.copy2(EXPORT_TFLITE, dest_model)
print(f"Copied Float32 model to Android app: {dest_model} ({os.path.getsize(dest_model)} bytes)")

# Verify tensors
interp = tf.lite.Interpreter(model_path=dest_model)
interp.allocate_tensors()
in_det = interp.get_input_details()
out_det = interp.get_output_details()
print("Verified Input Tensor:", in_det[0]['shape'], in_det[0]['dtype'])
print("Verified Output Tensor:", out_det[0]['shape'], out_det[0]['dtype'])
print("ALL DONE! Stage 3 Float32 model is active and verified for the Android app!")
