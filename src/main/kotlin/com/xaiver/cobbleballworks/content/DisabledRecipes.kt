package com.xaiver.cobbleballworks.content

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.config.BallworksConfig
import net.minecraft.resources.ResourceLocation

/**
 * Recipes the forge replaces (applied by RecipeManagerMixin on every data load, before parsing):
 * - Cobblemon's ball recipes, by id, so balls only come from lid + stone button + iron base and
 *   the Master and Beast disc recipes can use the same grid as Cobblemon's ball recipes;
 * - any other mod's recipe whose result is a forged ball (Cobblemon Parts Refabricated overrides
 *   the same ids and, for Cherish and Ancient Origin, adds recipes Cobblemon does not have), unless
 *   disableOtherModsBallRecipes is off;
 * - the apricorn -> dye smelting, which would fight the apricorn cooking over the furnace.
 */
object DisabledRecipes {
    val IDS: Set<ResourceLocation> = buildSet {
        ForgeBall.entries.forEach { add(it.ballId) }
        ApricornColor.entries.forEach { add(ResourceLocation.fromNamespaceAndPath("cobblemon", "${it.id}_apricorn_smelt_to_dye")) }
    }

    private val FORGED_BALLS: Set<ResourceLocation> = ForgeBall.entries.map { it.ballId }.toSet()

    @JvmStatic
    fun filter(recipes: Map<ResourceLocation, JsonElement>): Map<ResourceLocation, JsonElement> =
        filter(recipes, BallworksConfig.values.disableOtherModsBallRecipes)

    fun filter(recipes: Map<ResourceLocation, JsonElement>, otherMods: Boolean): Map<ResourceLocation, JsonElement> {
        val dropped = recipes.filter { (id, json) ->
            id in IDS || (otherMods && id.namespace != CobbleBallworks.MOD_ID && resultOf(json) in FORGED_BALLS)
        }.keys
        if (dropped.isEmpty()) return recipes
        val others = dropped.filter { it.namespace != "cobblemon" }
        CobbleBallworks.LOGGER.info("Disabled {} recipes that the Poké Ball forge replaces.", dropped.size)
        if (others.isNotEmpty()) CobbleBallworks.LOGGER.info("Other mods' Poké Ball recipes disabled: {}", others.sorted())
        return recipes.filterKeys { it !in dropped }
    }

    /** The item a recipe file makes: "result": {"id"} (1.21), {"item"} (older) or a plain id. */
    fun resultOf(json: JsonElement): ResourceLocation? {
        val result = (json as? JsonObject)?.get("result") ?: return null
        val id = when {
            result.isJsonPrimitive -> result.asString
            result.isJsonObject -> result.asJsonObject.let { it["id"] ?: it["item"] }?.takeIf { it.isJsonPrimitive }?.asString
            else -> null
        }
        return id?.let(ResourceLocation::tryParse)
    }
}
