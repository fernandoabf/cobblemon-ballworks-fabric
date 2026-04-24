package com.xaiver.cobbleballworks.block

import com.xaiver.cobbleballworks.registry.BallRegistry
import com.xaiver.cobbleballworks.registry.ModItems
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * Lookup table the Ball Anvil uses to decide which blanks can be hammered into what.
 *
 * Source of truth is [BallRegistry] plus the shared base-blank entry — this keeps the anvil
 * in lock-step with the crafting-grid hammering recipes without any per-ball code. Adding a
 * ball to [BallRegistry] automatically teaches the anvil how to shape its lid.
 *
 * `hits` is the number of hammer right-clicks required to convert [input] -> [output].
 * Tweak the defaults here; nothing else needs to move.
 */
object AnvilRecipes {

    private const val DEFAULT_LID_HITS = 3
    private const val DEFAULT_BASE_HITS = 3

    data class Recipe(val output: Item, val hits: Int) {
        fun outputStack(): ItemStack = ItemStack(output)
    }

    private val byInput: Map<Item, Recipe> by lazy { buildTable() }

    private fun buildTable(): Map<Item, Recipe> {
        val map = LinkedHashMap<Item, Recipe>()
        map[ModItems.BASE_BLANK.get()] = Recipe(ModItems.POKEBALL_BASE.get(), DEFAULT_BASE_HITS)
        for (id in BallRegistry.ALL) {
            val blank = ModItems.LID_BLANKS[id]?.get() ?: continue
            val lid = ModItems.LIDS[id]?.get() ?: continue
            map[blank] = Recipe(lid, DEFAULT_LID_HITS)
        }
        return map
    }

    fun forInput(stack: ItemStack): Recipe? =
        if (stack.isEmpty) null else byInput[stack.item]

    fun isSupportedBlank(stack: ItemStack): Boolean = forInput(stack) != null
}
