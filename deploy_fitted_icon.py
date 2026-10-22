import os
from PIL import Image, ImageDraw, ImageFilter

def deploy():
    orig_cut = Image.open(r"C:\Users\pagan\.gemini\antigravity-cli\brain\179db89b-5c21-43b4-937b-b3a4b36653a5\pokedex_cutout_clean.png").convert("RGBA")
    pokedex = orig_cut.crop(orig_cut.getbbox())
    pw, ph = pokedex.size

    h = 320
    scale = h / ph
    w = int(pw * scale)
    scaled = pokedex.resize((w, h), Image.Resampling.LANCZOS)

    CANVAS = 512
    ox = (CANVAS - w) // 2
    oy = (CANVAS - h) // 2

    # Background color: Sleek dark slate #161A21 (matches original intended tone)
    bg_rgb = (22, 26, 33)
    bg = Image.new("RGBA", (CANVAS, CANVAS), (*bg_rgb, 255))

    # Foreground (transparent with shadow + pokedex)
    foreground = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    s_mask = Image.new("RGBA", (w, h), (0, 0, 0, 160))
    shadow = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    shadow.paste(s_mask, (ox, oy + 6), scaled.split()[3])
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=10))

    foreground = Image.alpha_composite(shadow, foreground)
    foreground.paste(scaled, (ox, oy), scaled)

    # Flattened composite for legacy launchers
    flattened = Image.alpha_composite(bg, foreground)

    base_res = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\res"

    # Save foreground drawable
    fg_path = os.path.join(base_res, "drawable", "ic_launcher_foreground.png")
    foreground.save(fg_path, "PNG")
    print(f"Saved {fg_path}")

    # Generate mipmaps for all densities
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, size in densities.items():
        folder_path = os.path.join(base_res, folder)
        os.makedirs(folder_path, exist_ok=True)
        # Flattened icon for legacy
        resized_flat = flattened.resize((size, size), Image.Resampling.LANCZOS)
        resized_flat.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
        resized_flat.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")
        print(f"Generated {folder}: {size}x{size}")

    # Update colors.xml
    colors_xml = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#161A21</color>
    <color name="pokedex_red">#DC2028</color>
</resources>
"""
    with open(os.path.join(base_res, "values", "colors.xml"), "w") as f:
        f.write(colors_xml)
    print("Updated colors.xml")

    # Update ic_launcher.xml & ic_launcher_round.xml
    adaptive_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"""
    anydpi_path = os.path.join(base_res, "mipmap-anydpi-v26")
    os.makedirs(anydpi_path, exist_ok=True)
    with open(os.path.join(anydpi_path, "ic_launcher.xml"), "w") as f:
        f.write(adaptive_xml)
    with open(os.path.join(anydpi_path, "ic_launcher_round.xml"), "w") as f:
        f.write(adaptive_xml)
    print("Updated adaptive icon XMLs")

if __name__ == "__main__":
    deploy()
