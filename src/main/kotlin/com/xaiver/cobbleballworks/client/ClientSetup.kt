package com.xaiver.cobbleballworks.client

import com.xaiver.cobbleballworks.registry.ModBlockEntities
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.eventbus.api.IEventBus

/**
 * Client-only wiring for Ballworks. The entire object (and the client-only imports it
 * pulls in) is lazily class-loaded — [CobbleBallworks] references it behind an
 * `FMLEnvironment.dist == Dist.CLIENT` gate, so the dedicated server never touches
 * this file.
 */
@OnlyIn(Dist.CLIENT)
object ClientSetup {

    fun register(bus: IEventBus) {
        bus.addListener(::onRegisterRenderers)
    }

    private fun onRegisterRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(
            ModBlockEntities.BALL_ANVIL.get(),
            ::BallAnvilRenderer
        )
    }
}
