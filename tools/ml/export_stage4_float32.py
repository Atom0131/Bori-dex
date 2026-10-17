r"""
Exports Stage 4 Fine-Tuned Model to high-precision Float32 TFLite.
- Matches TFLiteClassifier.kt native direct ByteBuffer input [1, 224, 224, 3] Float32 (602,112 bytes).
- Full Float32 softmax outputs across 1,025 classes (prevents uint8 quantization collapse).
- Automatically copies the exported model directly to PokedexApp/app/src/main/assets/model.tflite.
"""
import os
import shutil
import tensorflow as tf

try:
    import tf_keras as keras
except ImportError:
    from tensorflow import keras

BASE_DIR = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\Pokemon_ref_imgs"
EXPORT_DIR = os.path.join(BASE_DIR, "exported_model")
APP_ASSETS = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\assets"

CANDIDATE_CHECKPOINTS = [
    os.path.join(EXPORT_DIR, "checkpoint_stage4.h5"),
    os.path.join(EXPORT_DIR, "checkpoint_stage4.keras"),
    os.path.join(EXPORT_DIR, "checkpoint.h5"),
    os.path.join(EXPORT_DIR, "checkpoint.keras"),
]
CHECKPOINT_PATH = next((p for p in CANDIDATE_CHECKPOINTS if os.path.exists(p)), None)
if not CHECKPOINT_PATH:
    raise FileNotFoundError("Could not find any checkpoint in exported_model/ to export!")

EXPORT_TFLITE = os.path.join(EXPORT_DIR, "stage4_model_float32.tflite")
IMAGE_SIZE = (224, 224)

print(f"\n========================================================")
print(f" EXPORTING CLEAN INFERENCE TFLITE MODEL")
print(f" Source Checkpoint: {CHECKPOINT_PATH}")
print(f" Target TFLite: {EXPORT_TFLITE}")
print(f" App Destination: {APP_ASSETS}\\model.tflite")
print(f"========================================================\n")

def load_checkpoint_safely(path):
    try:
        return keras.models.load_model(path)
    except Exception as e:
        print(f"[*] Standard load_model failed ({e}), attempting json/weights fallback...")
        import h5py
        import json
        with h5py.File(path, 'r') as f:
            cfg = json.loads(f.attrs['model_config'])
        def clean_dict(d):
            if isinstance(d, dict):
                d.pop('optional', None)
                for v in d.values():
                    clean_dict(v)
            elif isinstance(d, list):
                for item in d:
                    clean_dict(item)
        clean_dict(cfg)
        m = keras.models.model_from_json(json.dumps(cfg))
        m.load_weights(path)
        return m

print(f"[+] Loading model from: {CHECKPOINT_PATH}")
model = load_checkpoint_safely(CHECKPOINT_PATH)
print("[+] Checkpoint loaded successfully!")

print("[+] Constructing clean inference architecture without training augmentation layers...")
inputs = keras.Input(shape=(*IMAGE_SIZE, 3), dtype=tf.float32, name="input_1")
x = keras.layers.Rescaling(1.0 / 127.5, offset=-1.0)(inputs)
base_model = keras.applications.MobileNetV2(
    input_shape=(*IMAGE_SIZE, 3),
    include_top=False,
    weights=None
)
x = base_model(x, training=False)
x = keras.layers.GlobalAveragePooling2D()(x)
x = keras.layers.Dropout(0.3)(x)
outputs = keras.layers.Dense(1025, activation='softmax', name="predictions")(x)
inference_model = keras.Model(inputs, outputs)

# Transfer weights from loaded checkpoint
# Layer alignment: [2: MobileNetV2, 5: Dense classifier]
inference_model.layers[2].set_weights(model.layers[2].get_weights())
inference_model.layers[5].set_weights(model.layers[5].get_weights())
print("[+] Weights transferred to clean inference graph!")

print("[+] Converting to full precision Float32 TFLite...")
converter = tf.lite.TFLiteConverter.from_keras_model(inference_model)
# Do NOT apply uint8 quantization to preserve decimal precision across 1,025 classes
tflite_model = converter.convert()

with open(EXPORT_TFLITE, "wb") as f:
    f.write(tflite_model)
size_mb = len(tflite_model) / (1024 * 1024)
print(f"[+] Exported Float32 TFLite model: {EXPORT_TFLITE} ({size_mb:.2f} MB)")

# Copy directly to Android app assets
dest_model = os.path.join(APP_ASSETS, "model.tflite")
os.makedirs(APP_ASSETS, exist_ok=True)
shutil.copy2(EXPORT_TFLITE, dest_model)
print(f"[+] Copied directly to Android app assets: {dest_model} ({os.path.getsize(dest_model):,} bytes)")

# Verify tensors
print("[+] Verifying TFLite model tensor contract...")
interp = tf.lite.Interpreter(model_path=dest_model)
interp.allocate_tensors()
in_det = interp.get_input_details()
out_det = interp.get_output_details()
print(f"    - Input: shape={in_det[0]['shape']}, type={in_det[0]['dtype']}")
print(f"    - Output: shape={out_det[0]['shape']}, type={out_det[0]['dtype']}")
print("\n[✓] ALL DONE! Model is ready for Android build and deployment.")


if __name__ == "__main__":
    pass
