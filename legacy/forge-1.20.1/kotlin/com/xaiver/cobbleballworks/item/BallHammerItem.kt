package com.xaiver.cobbleballworks.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * Used as a shapeless-crafting ingredient to hammer a blank into a formed Poké Ball part.
 *
 * It stays in the grid after a craft by overriding the Forge crafting-remainder hook:
 * the returned copy has +1 damage, so the player can keep crafting until the hammer breaks.
 *
 * No custom recipe type required — any vanilla shapeless recipe that lists this item as an
 * ingredient will get the damage-and-return behavior for free. That keeps the door open
 * for machine automation later (Create mechanical crafter, RS autocrafter, etc.), since
 * those all respect crafting remainders.
 */
class BallHammerItem(props: Properties) : Item(props) {

    override fun hasCraftingRemainingItem(stack: ItemStack): Boolean = true

    override fun getCraftingRemainingItem(itemStack: ItemStack): ItemStack {
        val copy = itemStack.copy()
        copy.count = 1
        val next = copy.damageValue + 1
        if (next >= copy.maxDamage) {
            return ItemStack.EMPTY
        }
        copy.damageValue = next
        return copy
    }
}
