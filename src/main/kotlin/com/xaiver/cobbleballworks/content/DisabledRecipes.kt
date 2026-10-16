package com.xaiver.cobbleballworks.content

import com.google.gson.JsonElement
import com.xaiver.cobbleballworks.CobbleBallworks
import net.minecraft.resources.ResourceLocation

/**
 * Cobblemon recipes the forge replaces (applied by RecipeManagerMixin on every data load):
 * - every forged ball: balls only come from lid + stone button + iron base, and the Master and
 *   Beast disc recipes use the same grid as Cobblemon's ball recipes;
 * - the apricorn -> dye smelting, which would fight the apricorn cooking over the furnace.
 */
object DisabledRecipes {
    val IDS: Set<ResourceLocation> = buildSet {
        ForgeBall.entries.forEach { add(it.ballId) }
        ApricornColor.entries.forEach { add(ResourceLocation.fromNamespaceAndPath("cobblemon", "${it.id}_apricorn_smelt_to_dye")) }
    }

    @JvmStatic
    fun filter(recipes: Map<ResourceLocation, JsonElement>): Map<ResourceLocation, JsonElement> {
        val removed = recipes.keys.count { it in IDS }
        if (removed == 0) return recipes
        CobbleBallworks.LOGGER.info("Disabled {} Cobblemon recipes that the Poké Ball forge replaces.", removed)
        return recipes.filterKeys { it !in IDS }
    }
}
