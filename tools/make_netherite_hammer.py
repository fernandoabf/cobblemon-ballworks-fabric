#!/usr/bin/env python3
"""Builds textures/item/netherite_hammer.png from the diamond hammer sprite.

Like vanilla's netherite tools are its diamond tools recoloured, the head of diamond_hammer.png
(five shades of cyan) is mapped onto netherite's dark, slightly purple greys; the wooden handle is
kept. The diamond hammer sprite came from Pixelmon with the original mod, so this one stays under
the same terms as the other hammer sprites (see the README), not the MIT license.

Usage (from the repository root, needs Pillow):
    python3 tools/make_netherite_hammer.py
"""
from pathlib import Path

from PIL import Image

TEXTURES = Path("src/main/resources/assets/cobbleballworks/textures/item")

# Diamond head shades (darkest to lightest) -> netherite shades.
PALETTE = {
    (14, 63, 54): (0x1E, 0x19, 0x1D),
    (30, 138, 119): (0x42, 0x3A, 0x40),
    (39, 178, 154): (0x56, 0x4D, 0x53),
    (43, 199, 172): (0x6B, 0x61, 0x67),
    (51, 235, 203): (0x95, 0x89, 0x90),
}


def main():
    diamond = Image.open(TEXTURES / "diamond_hammer.png").convert("RGBA")
    out = diamond.copy()
    head = 0
    for y in range(diamond.height):
        for x in range(diamond.width):
            r, g, b, a = diamond.getpixel((x, y))
            if a and (r, g, b) in PALETTE:
                out.putpixel((x, y), PALETTE[(r, g, b)] + (a,))
                head += 1
    if head == 0:
        raise SystemExit("diamond_hammer.png has none of the expected head colours")
    out.save(TEXTURES / "netherite_hammer.png", optimize=True)
    print(f"wrote netherite_hammer.png ({head} head pixels recoloured)")


if __name__ == "__main__":
    main()
