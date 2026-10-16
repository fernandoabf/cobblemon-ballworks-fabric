#!/usr/bin/env python3
"""Derives the disc and lid sprites of every forged ball from Cobblemon's own ball sprites.

Cobblemon draws every Poke Ball item on the same 16x16 template, in a 3/4 view: the coloured top
half fills rows 0-7 and the band with the button starts at row 8. So for each ball:

  <ball>_lid.png        the coloured cap (rows 0-7) closed with a darker rim: the hammered lid
  <ball>_lid_blank.png  the same cap squashed into a flat lens: the disc before hammering

The source sprites are read straight from the Cobblemon jar in run-libs/ (downloaded by Gradle).
The results are derivative works of Cobblemon's textures (c) The Cobblemon Team, distributed under
Cobblemon's license (MPL-2.0); every generated PNG carries that notice in a tEXt chunk.

Usage (from the repository root, needs Pillow):
    python3 tools/derive_ball_parts.py
"""
import glob
import io
import sys
import zipfile
from pathlib import Path

from PIL import Image, PngImagePlugin

# Same list and order as com.xaiver.cobbleballworks.content.ForgeBall.
BALLS = [
    "poke", "great", "ultra", "level", "moon", "friend", "love", "safari", "heavy", "fast", "repeat",
    "timer", "nest", "net", "dive", "luxury", "heal", "dusk", "premier", "sport", "quick", "lure",
    "citrine", "verdant", "azure", "roseate", "slate",
    "park", "dream", "beast", "master", "cherish",
    "ancient_poke", "ancient_great", "ancient_ultra", "ancient_heavy", "ancient_leaden",
    "ancient_gigaton", "ancient_feather", "ancient_wing", "ancient_jet", "ancient_ivory",
    "ancient_citrine", "ancient_verdant", "ancient_azure", "ancient_roseate", "ancient_slate",
    "ancient_origin",
]

CUT = 8         # first row of the band in Cobblemon's ball template
DISC_ROWS = 3   # height of the flat disc, without its rim
OUT_DIR = Path("src/main/resources/assets/cobbleballworks/textures/item")
NOTICE = (
    "Derived from Cobblemon's item texture for this ball (c) The Cobblemon Team, MPL-2.0 "
    "(https://mozilla.org/MPL/2.0/). Modified by tools/derive_ball_parts.py in "
    "github.com/fernandoabf/cobblemon-ballworks-fabric."
)


def darker(pixel, factor=0.5):
    r, g, b, a = pixel
    return int(r * factor), int(g * factor), int(b * factor), a


def cap(ball):
    top = ball.crop((0, 0, 16, CUT))
    return top.crop(top.getbbox())


def with_rim(img):
    """Adds a darker row under img wherever its bottom row is opaque."""
    w, h = img.size
    out = Image.new("RGBA", (w, h + 1), (0, 0, 0, 0))
    out.alpha_composite(img, (0, 0))
    for x in range(w):
        pixel = img.getpixel((x, h - 1))
        if pixel[3] > 0:
            out.putpixel((x, h), darker(pixel))
    return out


def place(img, top_row):
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    out.alpha_composite(img, ((16 - img.size[0]) // 2, top_row))
    return out


def make_lid(ball):
    dome = with_rim(cap(ball))
    return place(dome, 13 - dome.size[1])  # resting on row 12


def make_disc(ball):
    c = cap(ball)
    lens = with_rim(c.resize((c.size[0], DISC_ROWS), Image.NEAREST))
    w, h = lens.size
    for xy in [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]:  # round the ends
        lens.putpixel(xy, (0, 0, 0, 0))
    return place(lens, 13 - h)


def save(img, path):
    info = PngImagePlugin.PngInfo()
    info.add_text("Copyright", NOTICE)
    img.save(path, pnginfo=info, optimize=True)


def main():
    jars = sorted(glob.glob("run-libs/Cobblemon-fabric-*.jar"))
    if not jars:
        sys.exit("Cobblemon jar not found in run-libs/ (run ./gradlew build once to download it)")
    with zipfile.ZipFile(jars[-1]) as jar:
        for ball in BALLS:
            data = jar.read(f"assets/cobblemon/textures/item/poke_balls/{ball}_ball.png")
            sprite = Image.open(io.BytesIO(data)).convert("RGBA")
            save(make_lid(sprite), OUT_DIR / f"{ball}_lid.png")
            save(make_disc(sprite), OUT_DIR / f"{ball}_lid_blank.png")
    print(f"wrote {2 * len(BALLS)} sprites from {jars[-1]}")


if __name__ == "__main__":
    main()
