import os
import shutil
import argparse

BASE = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\Pokemon_ref_imgs"
TARGET_DIR = os.path.join(BASE, "varaiants")
SOURCE_DIRS = [
    os.path.join(BASE, "varaiants_round3"),
    os.path.join(BASE, "varaiants_3d_prints"),
    os.path.join(BASE, "varaiants_sleep")
]

def merge_source(source_dir, target_dir):
    source_name = os.path.basename(source_dir)
    if not os.path.exists(source_dir):
        print(f"[{source_name}] Directory not found, skipping.")
        return 0

    pokemon_folders = [f for f in os.listdir(source_dir) if os.path.isdir(os.path.join(source_dir, f))]
    total = len(pokemon_folders)
    total_copied = 0
    print(f"\n--- Merging {source_name} ({total} Pokemon folders) into master varaiants ---")

    for i, pokemon in enumerate(pokemon_folders, 1):
        src_pkmn_dir = os.path.join(source_dir, pokemon)
        dst_pkmn_dir = os.path.join(target_dir, pokemon)
        os.makedirs(dst_pkmn_dir, exist_ok=True)

        # Find max existing number in target
        max_num = -1
        for f in os.listdir(dst_pkmn_dir):
            if f.endswith(".jpg"):
                try:
                    num = int(f.split(".")[0])
                    if num > max_num:
                        max_num = num
                except ValueError:
                    pass

        start_num = max_num + 1
        copied = 0
        for f in sorted(os.listdir(src_pkmn_dir)):
            if f.endswith(".jpg"):
                src = os.path.join(src_pkmn_dir, f)
                dst = os.path.join(dst_pkmn_dir, f"{start_num:04d}.jpg")
                shutil.copy2(src, dst)
                start_num += 1
                copied += 1
                total_copied += 1

        if i % 100 == 0 or i == total:
            print(f"[{i}/{total}] Merged through {pokemon} (+{copied} images, total target: {start_num})")

    print(f"[{source_name}] Completed! Copied {total_copied} images.")
    return total_copied


def main():
    parser = argparse.ArgumentParser(description="Merge all auxiliary scraping datasets into master varaiants.")
    parser.add_argument("--sources", nargs="+", default=None, help="Specific source folder names (e.g. varaiants_round3)")
    args = parser.parse_args()

    sources_to_merge = SOURCE_DIRS
    if args.sources:
        sources_to_merge = [os.path.join(BASE, s) if not os.path.isabs(s) else s for s in args.sources]

    grand_total = 0
    for src in sources_to_merge:
        grand_total += merge_source(src, TARGET_DIR)

    print(f"\n==================================================")
    print(f" ALL DATASETS MERGED SUCCESSFULLY: +{grand_total} TOTAL IMAGES ADDED")
    print(f"==================================================")


if __name__ == "__main__":
    main()
