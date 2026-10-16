package com.xaiver.cobbleballworks.client

import com.xaiver.cobbleballworks.CobbleBallworks
import net.fabricmc.api.ClientModInitializer

/**
 * Client-only entrypoint (fabric.mod.json -> "client", adapter "kotlin").
 * Lives in the `client` source set, so it can never be loaded on the dedicated server.
 */
object CobbleBallworksClient : ClientModInitializer {
    override fun onInitializeClient() {
        CobbleBallworks.LOGGER.info("Cobblemon Ballworks client initialized.")
    }
}
