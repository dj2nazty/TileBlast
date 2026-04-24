"""
Install the user-provided Tile Blast icon into the Android project.

Strategy:
  - Crop the source PNG to a square containing just the squircle icon art
    (drop the "Tile Blast" wordmark — Android shows the app name under the icon).
  - Emit raster PNGs at all 5 mipmap densities as ic_launcher.png and
    ic_launcher_round.png (round is the same art; launcher applies its own mask).
  - Also emit a 432x432 foreground PNG for the adaptive icon (API 26+), scaled
    into the 66dp safe zone, so the installed icon looks right under any mask.
  - Keep background as a solid color (#12121F) for the adaptive-icon background.
"""

from PIL import Image
import os, shutil

SRC = r"C:/Users/Dj2nazty/Downloads/tileblast_icon.png"
APP_RES = r"C:/Users/Dj2nazty/AndroidStudioProjects/TileBlast/app/src/main/res"

# 1. Load and crop to the squircle icon (drop the text)
im = Image.open(SRC).convert("RGBA")
W, H = im.size  # expected 784 x 1168

# Empirical crop: icon squircle occupies roughly top portion, centered horizontally.
# Make the crop slightly larger than the visible squircle so the mask has room.
crop_side = int(W * 0.90)                       # 705-ish
cx = W // 2
cy = int(H * 0.46)                              # icon center ~540 in 1168-tall image
left   = cx - crop_side // 2
top    = cy - crop_side // 2
right  = left + crop_side
bottom = top + crop_side
icon_sq = im.crop((left, top, right, bottom))

# Mipmap raster sizes (px for each dpi bucket)
MIPMAPS = [
    ("mipmap-mdpi",    48),
    ("mipmap-hdpi",    72),
    ("mipmap-xhdpi",   96),
    ("mipmap-xxhdpi", 144),
    ("mipmap-xxxhdpi",192),
]

for folder, size in MIPMAPS:
    out_dir = os.path.join(APP_RES, folder)
    os.makedirs(out_dir, exist_ok=True)
    resized = icon_sq.resize((size, size), Image.LANCZOS)
    resized.save(os.path.join(out_dir, "ic_launcher.png"))
    resized.save(os.path.join(out_dir, "ic_launcher_round.png"))
    print("wrote", out_dir)

# 2. Adaptive-icon foreground: 432x432 transparent canvas with icon scaled to the
#    66dp safe zone (~264/432). Background layer is a solid color.
FG_CANVAS = 432
SAFE = int(FG_CANVAS * 264 / 432)   # 264
fg = Image.new("RGBA", (FG_CANVAS, FG_CANVAS), (0, 0, 0, 0))
safe_icon = icon_sq.resize((SAFE, SAFE), Image.LANCZOS)
off = (FG_CANVAS - SAFE) // 2
fg.paste(safe_icon, (off, off), safe_icon)
fg_dir = os.path.join(APP_RES, "drawable")
os.makedirs(fg_dir, exist_ok=True)
fg.save(os.path.join(fg_dir, "ic_launcher_foreground.png"))
print("wrote foreground png")

# 3. Delete stale vector drawables that previously defined the icon
stale = [
    os.path.join(APP_RES, "drawable", "ic_launcher_foreground.xml"),
    os.path.join(APP_RES, "drawable", "ic_launcher_background.xml"),
    os.path.join(APP_RES, "drawable", "ic_launcher_legacy.xml"),
    os.path.join(APP_RES, "mipmap-anydpi",     "ic_launcher.xml"),
    os.path.join(APP_RES, "mipmap-anydpi",     "ic_launcher_round.xml"),
]
for p in stale:
    if os.path.exists(p):
        os.remove(p)
        print("deleted", p)

# 4. Rewrite the adaptive icon XML (API 26+) to point at the new PNG foreground
anydpi_v26 = os.path.join(APP_RES, "mipmap-anydpi-v26")
os.makedirs(anydpi_v26, exist_ok=True)
adaptive_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
"""
for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
    with open(os.path.join(anydpi_v26, name), "w", encoding="utf-8") as f:
        f.write(adaptive_xml)
    print("wrote", name)

print("done")
