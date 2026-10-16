package com.xaiver.cobbleballworks.client.compat.emi

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.content.AnvilRules
import com.xaiver.cobbleballworks.content.HammerMaterial
import com.xaiver.cobbleballworks.recipe.AnvilRecipe
import com.xaiver.cobbleballworks.registry.ModBlocks
import com.xaiver.cobbleballworks.registry.ModItems
import com.xaiver.cobbleballworks.registry.ModRecipes
import com.xaiver.cobbleballworks.registry.ModTags
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.BasicEmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.SlotWidget
import dev.emi.emi.api.widget.Widget
import dev.emi.emi.api.widget.WidgetHolder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeHolder

/**
 * EMI plugin ("emi" entrypoint, only loaded when EMI is installed). Every cobbleballworks:anvil
 * recipe shows up twice: hammered on the Poké Ball anvil and pressed by the Mechanical Anvil.
 */
class BallworksEmiPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        registry.addCategory(BALL_ANVIL)
        registry.addCategory(MECHANICAL_ANVIL)
        registry.addWorkstation(BALL_ANVIL, EmiStack.of(ModBlocks.BALL_ANVIL))
        registry.addWorkstation(BALL_ANVIL, EmiIngredient.of(ModTags.Items.HAMMERS))
        registry.addWorkstation(MECHANICAL_ANVIL, EmiStack.of(ModBlocks.MECHANICAL_ANVIL))
        for (holder in registry.recipeManager.getAllRecipesFor(ModRecipes.ANVIL)) {
            registry.addRecipe(HammeringRecipe(holder))
            registry.addRecipe(PressingRecipe(holder))
        }
    }

    /**
     * On the anvil: piece -> result, then a hammer slot that shows every hammer in turn next to the
     * strikes it needs for the whole piece (the arrow lists them all at once), and the force line.
     */
    private class HammeringRecipe(holder: RecipeHolder<AnvilRecipe>) :
        BasicEmiRecipe(BALL_ANVIL, holder.id(), 134, 34) {
        private val force = holder.value().force

        init {
            inputs.add(EmiIngredient.of(holder.value().ingredient))
            outputs.add(EmiStack.of(holder.value().result))
        }

        override fun addWidgets(widgets: WidgetHolder) {
            widgets.addSlot(inputs[0], 0, 0)
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 22, 1)
            widgets.addTooltipText(strikesTable(), 22, 1, 24, 17)
            widgets.addSlot(outputs[0], 50, 0).recipeContext(this)

            widgets.add(CyclingHammerSlot(92, 0)).catalyst(true)
            widgets.add(StrikesText(113, 5, force))
            widgets.addTooltipText(strikesTable(), 111, 0, width - 111, 18)

            val forceText = Component.translatable("emi.cobbleballworks.anvil.force", force, AnvilRules.STAGES)
            widgets.addText(forceText, 0, 25, TEXT_COLOR, false)
            widgets.addTooltipText(
                listOf(
                    Component.translatable("emi.cobbleballworks.anvil.force.line1"),
                    Component.translatable("emi.cobbleballworks.anvil.force.line2", force),
                ),
                0, 23, font().width(forceText), 11,
            )
        }

        private fun strikesTable(): List<Component> = buildList {
            add(Component.translatable("emi.cobbleballworks.anvil.strikes_title"))
            for (material in HammerMaterial.entries) {
                add(Component.translatable("emi.cobbleballworks.anvil.strikes", ModItems.hammer(material).description, strikes(material, force)))
            }
        }
    }

    /** The hammer [shownHammer] picks, so the slot and [StrikesText] always agree. */
    private class CyclingHammerSlot(x: Int, y: Int) : SlotWidget(EmiStack.EMPTY, x, y) {
        override fun getStack(): EmiIngredient = HAMMER_STACKS[shownHammer().ordinal]
    }

    /** "×N": strikes the hammer in [CyclingHammerSlot] needs for a piece of [force] per stage. */
    private class StrikesText(private val x: Int, private val y: Int, private val force: Int) : Widget() {
        override fun getBounds(): Bounds = Bounds(x, y, 20, 9)

        override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
            val text = Component.translatable("emi.cobbleballworks.anvil.hammer_strikes", strikes(shownHammer(), force))
            draw.drawString(font(), text, x, y, TEXT_COLOR, false)
        }
    }

    /** In the Mechanical Anvil: a furnace-like recipe with its time (40 ticks per force). */
    private class PressingRecipe(holder: RecipeHolder<AnvilRecipe>) :
        BasicEmiRecipe(MECHANICAL_ANVIL, synthetic("mechanical_anvil", holder.id()), 82, 38) {
        private val ticks = holder.value().processingTime

        init {
            inputs.add(EmiIngredient.of(holder.value().ingredient))
            outputs.add(EmiStack.of(holder.value().result))
        }

        override fun addWidgets(widgets: WidgetHolder) {
            widgets.addTexture(EmiTexture.EMPTY_FLAME, 1, 24)
            widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 1, 24, 4000, false, true, true)
            widgets.addFillingArrow(24, 5, 50 * ticks)
            widgets.addSlot(inputs[0], 0, 4)
            widgets.addSlot(outputs[0], 56, 0).large(true).recipeContext(this)
            val seconds = if (ticks % 20 == 0) "${ticks / 20}" else "%.1f".format(java.util.Locale.ROOT, ticks / 20f)
            val time = Component.translatable("emi.cobbleballworks.mechanical_anvil.time", seconds)
            widgets.addText(time, width - font().width(time), height - 9, TEXT_COLOR, false)
        }
    }

    companion object {
        private const val TEXT_COLOR = 0x404040

        /** How long each hammer stays in [CyclingHammerSlot]. */
        private const val HAMMER_CYCLE_MS = 1000L

        val BALL_ANVIL = EmiRecipeCategory(CobbleBallworks.id("ball_anvil"), EmiStack.of(ModBlocks.BALL_ANVIL))
        val MECHANICAL_ANVIL = EmiRecipeCategory(CobbleBallworks.id("mechanical_anvil"), EmiStack.of(ModBlocks.MECHANICAL_ANVIL))

        private val HAMMER_STACKS: List<EmiStack> = HammerMaterial.entries.map { EmiStack.of(ModItems.hammer(it)) }

        private fun shownHammer(): HammerMaterial {
            val hammers = HammerMaterial.entries
            return hammers[((System.currentTimeMillis() / HAMMER_CYCLE_MS) % hammers.size).toInt()]
        }

        private fun strikes(material: HammerMaterial, force: Int): Int = AnvilRules.strikesPerPiece(material.strength, force)

        private fun font() = Minecraft.getInstance().font

        /** EMI's convention for recipes that are not the recipe file itself: a path starting with "/". */
        private fun synthetic(prefix: String, id: ResourceLocation): ResourceLocation =
            CobbleBallworks.id("/$prefix/${id.namespace}/${id.path}")
    }
}
