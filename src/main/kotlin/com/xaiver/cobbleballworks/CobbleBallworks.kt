package com.xaiver.cobbleballworks

import com.xaiver.cobbleballworks.registry.ModBlockEntities
import com.xaiver.cobbleballworks.registry.ModBlocks
import com.xaiver.cobbleballworks.registry.ModCreativeTabs
import com.xaiver.cobbleballworks.registry.ModItems
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.loading.FMLEnvironment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Mod(CobbleBallworks.MOD_ID)
class CobbleBallworks {
    companion object {
        const val MOD_ID = "cobbleballworks"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }

    init {
        val modBus: IEventBus = FMLJavaModLoadingContext.get().modEventBus

        // Order matters: ModItems first so the shared ITEMS register is ready before
        // ModBlocks adds the BallAnvil BlockItem into it.
        ModItems.register(modBus)
        ModBlocks.register(modBus)
        ModBlockEntities.register(modBus)
        ModCreativeTabs.register(modBus)

        // Client-only wiring (BlockEntityRenderer registration). The
        // `com.xaiver.cobbleballworks.client` package imports `net.minecraft.client.**`
        // classes that don't exist on a dedicated server, so ClientSetup must only be
        // referenced when we're actually on the client. Lazy class-loading means the
        // JVM never resolves ClientSetup on the server side — the INVOKESTATIC below
        // is reached only when the `if` is true, and the class is loaded at that
        // moment, not at CobbleBallworks load time.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.xaiver.cobbleballworks.client.ClientSetup.register(modBus)
        }

        LOGGER.info("Cobblemon Ballworks loaded - parts crafting online.")
    }
}
