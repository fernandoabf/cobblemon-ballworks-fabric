package com.xaiver.cobbleballworks.client.screen

import com.xaiver.cobbleballworks.menu.MechanicalAnvilMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Inventory

/** Pixelmon drew the Mechanical Anvil with the vanilla furnace texture; so does this screen. */
class MechanicalAnvilScreen(menu: MechanicalAnvilMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<MechanicalAnvilMenu>(menu, inventory, title) {

    override fun init() {
        super.init()
        titleLabelX = (imageWidth - font.width(title)) / 2
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(graphics, mouseX, mouseY, partialTick)
        renderTooltip(graphics, mouseX, mouseY)
    }

    override fun renderBg(graphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val x = leftPos
        val y = topPos
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight)
        if (menu.isLit) {
            val flame = Mth.ceil(menu.litProgress * 13f) + 1
            graphics.blitSprite(LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - flame, x + 56, y + 36 + 14 - flame, 14, flame)
        }
        val arrow = Mth.ceil(menu.forgeProgress * 24f)
        graphics.blitSprite(BURN_PROGRESS_SPRITE, 24, 16, 0, 0, x + 79, y + 34, arrow, 16)
    }

    private companion object {
        val TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png")
        val LIT_PROGRESS_SPRITE: ResourceLocation = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress")
        val BURN_PROGRESS_SPRITE: ResourceLocation = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress")
    }
}
