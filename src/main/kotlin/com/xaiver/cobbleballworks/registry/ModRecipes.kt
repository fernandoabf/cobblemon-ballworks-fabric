package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.recipe.AnvilRecipe
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType

object ModRecipes {
    val ANVIL: RecipeType<AnvilRecipe> = Registry.register(
        BuiltInRegistries.RECIPE_TYPE,
        CobbleBallworks.id("anvil"),
        object : RecipeType<AnvilRecipe> {
            override fun toString(): String = "${CobbleBallworks.MOD_ID}:anvil"
        },
    )

    val ANVIL_SERIALIZER: RecipeSerializer<AnvilRecipe> =
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CobbleBallworks.id("anvil"), AnvilRecipe.Serializer)

    fun init() = Unit
}
