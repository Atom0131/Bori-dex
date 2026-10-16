"""
Kaggle Dataset Preparation & Upload Helper
------------------------------------------
Packages the 494,768 images master dataset and creates dataset-metadata.json
ready for public upload to Kaggle Datasets via the Kaggle API/CLI or Web interface.

Prerequisites:
  1. pip install kaggle
  2. Place your kaggle.json API key in ~/.kaggle/kaggle.json

Usage:
  python prepare_kaggle_dataset.py --create-metadata
  python prepare_kaggle_dataset.py --upload
"""

import os
import json
import argparse

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "Pokemon_ref_imgs"))
DATASET_DIR = os.path.join(BASE_DIR, "varaiants")
KAGGLE_DIR = os.path.join(BASE_DIR, "kaggle_dataset")

METADATA = {
    "title": "Bori-dex: 1,025 Pokemon Master Vision Dataset (494k Images)",
    "id": "atom0131/boridex-pokemon-master-dataset-494k",
    "licenses": [{"name": "CC0-1.0"}],
    "description": (
        "Complete, hardened computer vision training dataset used for the Bori-dex Android application.\n\n"
        "### Dataset Highlights:\n"
        "- 494,768 images organized across 1,025 Pokémon species classes (Gen 1 through Gen 9).\n"
        "- Includes Ken Sugimori canonical 2D art, DreamWorld art, high-resolution anime frames, "
        "TCG card illustrations, 3D resin & filament prints, sleep series figures, and physical room lighting variations.\n"
        "- Used to train the production MobileNetV2 Float32 model powering the offline Bori-dex scanner.\n\n"
        "Project repository: https://github.com/Atom0131/Bori-dex"
    )
}

def generate_metadata():
    os.makedirs(KAGGLE_DIR, exist_ok=True)
    meta_path = os.path.join(KAGGLE_DIR, "dataset-metadata.json")
    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump(METADATA, f, indent=2)
    print(f"[+] Created Kaggle dataset metadata at: {meta_path}")

def print_instructions():
    print("\n" + "=" * 60)
    print(" KAGGLE DATASET UPLOAD INSTRUCTIONS")
    print("=" * 60)
    print("1. Install Kaggle CLI:")
    print("   pip install kaggle")
    print("\n2. Ensure your Kaggle API key is at ~/.kaggle/kaggle.json")
    print("\n3. To create the public dataset on Kaggle:")
    print(f"   kaggle datasets create -p \"{DATASET_DIR}\" -u")
    print("\n4. Or compress into zip parts and upload via the Kaggle Web UI at:")
    print("   https://www.kaggle.com/datasets/new")
    print("=" * 60 + "\n")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Prepare Bori-dex dataset for Kaggle")
    parser.add_argument("--create-metadata", action="store_true", help="Generate dataset-metadata.json")
    args = parser.parse_args()
    
    generate_metadata()
    print_instructions()
