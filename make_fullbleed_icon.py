import numpy as np
from PIL import Image

# 1. Load clean cutout
cutout_path = r"C:\Users\pagan\.gemini\antigravity-cli\brain\179db89b-5c21-43b4-937b-b3a4b36653a5\pokedex_cutout_clean.png"
img = Image.open(cutout_path).convert("RGBA")

# Find non-transparent bounding box
bbox = img.getbbox()
print("Cutout bbox:", bbox)
cropped = img.crop(bbox)
cw, ch = cropped.size
print(f"Cropped size: {cw}x{ch}")

# Sample the dominant red color from the casing (around pixel 50, 50 in cropped)
# Find an opaque pixel that is predominantly red
np_img = np.array(cropped)
mask = np_img[:, :, 3] > 200
# Filter for red casing (R > 180, G < 80, B < 80)
red_pixels = np_img[mask & (np_img[:, :, 0] > 180) & (np_img[:, :, 1] < 80) & (np_img[:, :, 2] < 80)]
if len(red_pixels) > 0:
    median_red = np.median(red_pixels[:, :3], axis=0).astype(int)
    hex_red = f"#{median_red[0]:02X}{median_red[1]:02X}{median_red[2]:02X}"
    print(f"Dominant Pokédex Red: {hex_red} (RGB: {median_red})")
else:
    median_red = [220, 32, 45]
    hex_red = "#DC202D"

# In Android Adaptive Icons (108dp x 108dp canvas):
# - Full canvas: 108dp (e.g. 432x432 px or 512x512 px)
# - Visible squircle mask: inner 72dp diameter (66.6% of the canvas, or radius ~36dp)
# - To make the Pokédex "just fit" edge-to-edge inside the squircle:
# We scale the Pokédex so its red body fills ~80% of the canvas height/width, 
# with the red background extending to all edges so the squircle is 100% Pokédex Red.

CANVAS_SIZE = 512

# Scale cropped Pokédex to nicely fill the squircle without clipping the lens/d-pad
target_h = int(CANVAS_SIZE * 0.82)
scale = target_h / ch
target_w = int(cw * scale)
scaled = cropped.resize((target_w, target_h), Image.Resampling.LANCZOS)

# Create solid Pokédex red background
full_icon = Image.new("RGBA", (CANVAS_SIZE, CANVAS_SIZE), (median_red[0], median_red[1], median_red[2], 255))

# Paste scaled Pokédex centered
offset_x = (CANVAS_SIZE - target_w) // 2
offset_y = (CANVAS_SIZE - target_h) // 2
full_icon.paste(scaled, (offset_x, offset_y), scaled)

# Save master full-bleed icon
master_out = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\res\drawable\pokedex_fullbleed.png"
full_icon.save(master_out, "PNG")
print(f"Saved master fullbleed to {master_out}")

# Save into artifact dir too
artifact_out = r"C:\Users\pagan\.gemini\antigravity-cli\brain\179db89b-5c21-43b4-937b-b3a4b36653a5\pokedex_fullbleed.png"
full_icon.save(artifact_out, "PNG")

# Now generate density mipmaps
densities = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

base_res = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\res"
for folder, size in densities.items():
    resized = full_icon.resize((size, size), Image.Resampling.LANCZOS)
    # Save as ic_launcher.png and ic_launcher_round.png
    out_dir = f"{base_res}\\{folder}"
    resized.save(f"{out_dir}\\ic_launcher.png", "PNG")
    resized.save(f"{out_dir}\\ic_launcher_round.png", "PNG")
    print(f"Generated {folder}: {size}x{size}")

# Also update colors.xml with hex_red
colors_xml = f"""<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="pokedex_red">{hex_red}</color>
    <color name="ic_launcher_background">{hex_red}</color>
</resources>
"""
with open(f"{base_res}\\values\\colors.xml", "w") as f:
    f.write(colors_xml)
print("Updated colors.xml")
