package com.xaiver.cobbleballworks.client

import com.xaiver.cobbleballworks.registry.ModItems
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.color.item.ItemColor

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
        ColorProviderRegistry.ITEM.register(
            ItemColor { _, tintIndex -> if (tintIndex == 0) COOKED_APRICORN_TINT else -1 },
            *ModItems.COOKED_APRICORNS.values.toTypedArray(),
        )
    }
}
