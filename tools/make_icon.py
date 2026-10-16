#!/usr/bin/env python3
"""Builds the mod icon (assets/cobbleballworks/icon.png) from two sprites the mod already ships.

An iron hammer comes down on a Poke Ball lid: the lid is derive_ball_parts.py's poke_lid.png
(derived from Cobblemon's texture, MPL-2.0) and the hammer is iron_hammer.png (imported from Pixelmon
by the original author). The icon stays under the terms of those two sprites (see the README).

Usage (from the repository root, needs Pillow; run derive_ball_parts.py first):
    python3 tools/make_icon.py
"""
from pathlib import Path

from PIL import Image, PngImagePlugin

TEXTURES = Path("src/main/resources/assets/cobbleballworks/textures/item")
OUT = Path("src/main/resources/assets/cobbleballworks/icon.png")

CANVAS = 24  # composition size in sprite pixels
SCALE = 5    # 24 * 5 = 120, centred in 128
SIZE = 128
NOTICE = (
    "Poke Ball lid derived from Cobblemon's texture (c) The Cobblemon Team, MPL-2.0 "
    "(https://mozilla.org/MPL/2.0/); hammer sprite imported from Pixelmon by the original author."
)


def sprite(name: str, box: tuple[int, int, int, int]) -> Image.Image:
    return Image.open(TEXTURES / f"{name}.png").convert("RGBA").crop(box)


def main() -> None:
    lid = sprite("poke_lid", (2, 6, 14, 13))         # 12x7, the cap without the empty rows
    hammer = sprite("iron_hammer", (1, 1, 16, 16))   # 15x15, head top-right
    striking = hammer.transpose(Image.Transpose.ROTATE_270)  # head bottom-right, handle up-left

    canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    canvas.alpha_composite(lid, (8, 16))
    canvas.alpha_composite(striking, (2, 2))

    icon = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    scaled = canvas.resize((CANVAS * SCALE, CANVAS * SCALE), Image.NEAREST)
    offset = (SIZE - CANVAS * SCALE) // 2
    icon.alpha_composite(scaled, (offset, offset))

    info = PngImagePlugin.PngInfo()
    info.add_text("Copyright", NOTICE)
    icon.save(OUT, pnginfo=info, optimize=True)
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()
