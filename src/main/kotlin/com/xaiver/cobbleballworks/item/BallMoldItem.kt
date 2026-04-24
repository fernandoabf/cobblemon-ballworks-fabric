package com.xaiver.cobbleballworks.item

import net.minecraft.world.item.Item

/**
 * Reusable ball mold. Produced only via a Tinkers' Construct casting-table recipe
 * (steel poured over a formed lid/base) — never through vanilla crafting — and used
 * as the "mold" input on Mekanism: More Machine's Stamper / Stamping Factory.
 *
 * Plain [Item] subclass. Source inspection of Mekanism-MoreMachine 1.20.1 shows
 * `TileEntityStamper` validates its mold slot only via `containsRecipeB(item)`
 * (i.e. "is there a loaded stamper recipe whose mold side accepts this?") — it
 * does NOT inspect `hasCraftingRemainingItem`, so overriding those methods on
 * the mold item does nothing useful for the MekMM Stamper. The mold's
 * reusability is governed by the Stamper's dedicated `mold` slot at the machine
 * level, not by vanilla crafting-remainder behavior.
 */
class BallMoldItem(props: Properties) : Item(props)
