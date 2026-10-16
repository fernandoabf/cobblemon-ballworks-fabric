package com.xaiver.cobbleballworks

import com.xaiver.cobbleballworks.config.BallworksConfig
import com.xaiver.cobbleballworks.registry.ModBlockEntities
import com.xaiver.cobbleballworks.registry.ModBlocks
import com.xaiver.cobbleballworks.registry.ModCreativeTab
import com.xaiver.cobbleballworks.registry.ModItems
import com.xaiver.cobbleballworks.registry.ModRecipes
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Fabric entrypoint (fabric.mod.json -> "main", adapter "kotlin").
 *
 * Fabric 1.21.1 port of Cobblemon Ballworks by xaiver (cxaiverb), MIT, reworked to match the
 * Poké Ball forge of classic Pixelmon (see docs/PLANO.md).
 */
object CobbleBallworks : ModInitializer {
    const val MOD_ID = "cobbleballworks"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)

    override fun onInitialize() {
        BallworksConfig.load()
        ModBlocks.init()
        ModBlockEntities.init()
        ModItems.init()
        ModRecipes.init()
        ModCreativeTab.init()
        LOGGER.info("Cobblemon Ballworks initialized ({} items).", ModItems.all().size + ModBlocks.items().size)
    }
}
