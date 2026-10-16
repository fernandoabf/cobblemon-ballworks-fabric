package com.xaiver.cobbleballworks.client

import com.xaiver.cobbleballworks.client.render.BallAnvilRenderer
import com.xaiver.cobbleballworks.client.render.MechanicalAnvilRenderer
import com.xaiver.cobbleballworks.client.render.PieceRenderer
import com.xaiver.cobbleballworks.client.screen.MechanicalAnvilScreen
import com.xaiver.cobbleballworks.registry.ModBlockEntities
import com.xaiver.cobbleballworks.registry.ModItems
import com.xaiver.cobbleballworks.registry.ModMenus
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers

/**
 * Client-only entrypoint (fabric.mod.json -> "client", adapter "kotlin").
 * Lives in the `client` source set, so it can never be loaded on the dedicated server.
 */
object CobbleBallworksClient : ClientModInitializer {
    /**
     * Cooked apricorns reuse Cobblemon's apricorn sprite (layer0, referenced by the model, not
     * copied) tinted with this warm "roasted" colour, plus our own grill-mark overlay (layer1).
     */
    private const val COOKED_APRICORN_TINT = 0xFFEBC8A0.toInt()

    override fun onInitializeClient() {
        BlockEntityRenderers.register(ModBlockEntities.BALL_ANVIL, ::BallAnvilRenderer)
        BlockEntityRenderers.register(ModBlockEntities.MECHANICAL_ANVIL, ::MechanicalAnvilRenderer)
        // The pieces' 3D models (models/piece) are not item or block models, so ask for them.
        ModelLoadingPlugin.register { context -> context.addModels(PieceRenderer.modelIds) }
        MenuScreens.register(ModMenus.MECHANICAL_ANVIL, ::MechanicalAnvilScreen)
        ColorProviderRegistry.ITEM.register(
            ItemColor { _, tintIndex -> if (tintIndex == 0) COOKED_APRICORN_TINT else -1 },
            *ModItems.COOKED_APRICORNS.values.toTypedArray(),
        )
    }
}
