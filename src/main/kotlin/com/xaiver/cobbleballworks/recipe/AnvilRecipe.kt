package com.xaiver.cobbleballworks.recipe

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.xaiver.cobbleballworks.content.AnvilRules
import com.xaiver.cobbleballworks.registry.ModBlocks
import com.xaiver.cobbleballworks.registry.ModRecipes
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level

/**
 * `cobbleballworks:anvil`: what a piece becomes on the Poké Ball anvil (hammered) and in the
 * Mechanical Anvil. Read by both anvils and by the EMI plugin.
 *
 * ```json
 * {"type": "cobbleballworks:anvil", "ingredient": {"item": "..."}, "result": {"id": "..."}, "force": 5}
 * ```
 * `force` (optional, default 5) is the force per stage: a piece needs 3 stages, so a hammer of
 * strength S strikes 3 x ceil(force / S) times, and the Mechanical Anvil takes 40 ticks per force.
 */
class AnvilRecipe(val ingredient: Ingredient, val result: ItemStack, val force: Int) : Recipe<SingleRecipeInput> {

    /** Ticks the Mechanical Anvil spends on one piece: 200 for a force-5 disc, like a furnace. */
    val processingTime: Int get() = force * TICKS_PER_FORCE

    override fun matches(input: SingleRecipeInput, level: Level): Boolean = ingredient.test(input.item())

    override fun assemble(input: SingleRecipeInput, registries: HolderLookup.Provider): ItemStack = result.copy()

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = true

    override fun getResultItem(registries: HolderLookup.Provider): ItemStack = result

    override fun getIngredients(): NonNullList<Ingredient> = NonNullList.of(Ingredient.EMPTY, ingredient)

    /** Kept out of the vanilla recipe book, which has no category for it (EMI shows it). */
    override fun isSpecial(): Boolean = true

    override fun getToastSymbol(): ItemStack = ItemStack(ModBlocks.BALL_ANVIL)

    override fun getSerializer(): RecipeSerializer<AnvilRecipe> = Serializer

    override fun getType(): RecipeType<AnvilRecipe> = ModRecipes.ANVIL

    object Serializer : RecipeSerializer<AnvilRecipe> {
        private val CODEC: MapCodec<AnvilRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(AnvilRecipe::ingredient),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(AnvilRecipe::result),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("force", AnvilRules.DEFAULT_FORCE_PER_STAGE).forGetter(AnvilRecipe::force),
            ).apply(instance, ::AnvilRecipe)
        }

        private val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AnvilRecipe> = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, AnvilRecipe::ingredient,
            ItemStack.STREAM_CODEC, AnvilRecipe::result,
            ByteBufCodecs.VAR_INT, AnvilRecipe::force,
            ::AnvilRecipe,
        )

        override fun codec(): MapCodec<AnvilRecipe> = CODEC

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, AnvilRecipe> = STREAM_CODEC
    }

    companion object {
        const val TICKS_PER_FORCE = 40

        /** The anvil recipe for [stack], or null if it cannot be worked on an anvil. */
        fun find(level: Level, stack: ItemStack): RecipeHolder<AnvilRecipe>? =
            if (stack.isEmpty) null else level.recipeManager.getRecipeFor(ModRecipes.ANVIL, SingleRecipeInput(stack), level).orElse(null)
    }
}
