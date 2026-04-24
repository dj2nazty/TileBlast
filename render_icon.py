from PIL import Image, ImageDraw
import os

# Render the TileBlast icon at multiple sizes/shapes for download.
OUT_DIR = os.path.expanduser("~/Downloads")

def hx(c):
    c = c.lstrip("#")
    return (int(c[0:2],16), int(c[2:4],16), int(c[4:6],16), 255)

BG = hx("#12121F")
TILES = [
    ("#5B8FFF", 0, 0),
    ("#22D9A0", 1, 0),
    ("#F06060", 2, 0),
    ("#4FA8F5", 0, 1),
    ("#F0A030", 1, 1),
    ("#9B80F0", 2, 1),
    ("#50D0E0", 0, 2),
    ("#F06060", 1, 2),
    ("#22D9A0", 2, 2),
]

def render(size, shape="square", fit="safe"):
    """
    shape: 'square' | 'round' | 'squircle'
    fit:   'safe' = icon tiles fit Android's adaptive-icon safe zone (small, like installed)
           'full' = tiles fill most of the canvas (good for store listings / previews)
    """
    img = Image.new("RGBA", (size, size), (0,0,0,0))
    draw = ImageDraw.Draw(img)

    # Background first (masked later for round/squircle)
    bg = Image.new("RGBA", (size, size), BG)

    # Tile layout
    if fit == "safe":
        # 108dp viewport, tiles occupy 36..76 -> 40dp wide centered. 40/108 = 0.37
        block_frac = 0.37
    else:
        # Fill ~82% of canvas for store listing
        block_frac = 0.82

    block = int(size * block_frac)
    start = (size - block) // 2
    cell = block // 3
    pad = max(2, size // 80)
    radius = max(4, cell // 6)

    td = ImageDraw.Draw(bg)
    for color, cx, cy in TILES:
        x0 = start + cx * cell + pad
        y0 = start + cy * cell + pad
        x1 = start + (cx+1) * cell - pad
        y1 = start + (cy+1) * cell - pad
        td.rounded_rectangle([x0, y0, x1, y1], radius=radius, fill=hx(color))

    # Apply mask
    if shape == "square":
        out = bg
    else:
        mask = Image.new("L", (size, size), 0)
        md = ImageDraw.Draw(mask)
        if shape == "round":
            md.ellipse([0, 0, size, size], fill=255)
        elif shape == "squircle":
            md.rounded_rectangle([0,0,size,size], radius=int(size*0.23), fill=255)
        out = Image.new("RGBA", (size,size), (0,0,0,0))
        out.paste(bg, (0,0), mask)
    return out

# Generate a set of useful downloads.
outputs = [
    # Play Store icon (required 512x512 PNG for store listing)
    ("TileBlast_icon_512_square.png", render(512, "square", "full")),
    ("TileBlast_icon_512_squircle.png", render(512, "squircle", "full")),
    ("TileBlast_icon_512_round.png", render(512, "round", "full")),
    # Preview of the REAL adaptive icon as-installed (tiles in the safe zone)
    ("TileBlast_icon_adaptive_preview_512.png", render(512, "squircle", "safe")),
    # Large version for viewing
    ("TileBlast_icon_1024.png", render(1024, "square", "full")),
]

for name, im in outputs:
    path = os.path.join(OUT_DIR, name)
    im.save(path)
    print("wrote", path)
