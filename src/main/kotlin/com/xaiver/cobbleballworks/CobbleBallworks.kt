package com.xaiver.cobbleballworks

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Fabric entrypoint (fabric.mod.json -> "main", adapter "kotlin").
 *
 * Fabric 1.21.1 port of Cobblemon Ballworks by xaiver (cxaiverb), MIT.
 * Toolchain skeleton only: no content is registered yet.
 */
object CobbleBallworks : ModInitializer {
    const val MOD_ID = "cobbleballworks"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("Cobblemon Ballworks (Fabric) initialized - toolchain skeleton, no content yet.")
    }
}
