package com.xaiver.cobbleballworks.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.xaiver.cobbleballworks.CobbleBallworks
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

/**
 * Server-side options in `config/cobbleballworks.json`. Missing keys get their default and the
 * file is rewritten with every option, so new options show up after an update.
 */
object BallworksConfig {
    data class Values(
        /** Durability a hammer loses each time a strike advances a stage (Pixelmon 8.x: 1, up to 7.2.0: 3). */
        val hammerDamagePerStage: Int = 1,
        /** After a finished piece is knocked off by hammering, the next matching piece is pulled from the inventory. */
        val allowAnvilAutoreloading: Boolean = false,
        /** The Mechanical Anvil clanks (iron golem death sound, as in Pixelmon 8.x) on every finished piece. */
        val mechanicalAnvilSound: Boolean = true,
    )

    /** Current values. Replaced on load; tests may set it directly. */
    @Volatile
    @JvmStatic
    var values: Values = Values()

    private val COMMENTS = mapOf(
        "hammerDamagePerStage" to "Durability a hammer loses when a strike advances a stage (3 stages per piece). Pixelmon 8.x: 1; up to 7.2.0: 3.",
        "allowAnvilAutoreloading" to "After hammering a finished piece off the anvil, pull the next matching disc from the inventory (Pixelmon option, off by default).",
        "mechanicalAnvilSound" to "Mechanical Anvil plays a heavy metal clank on every finished piece, as in Pixelmon 8.x.",
    )

    fun load(path: Path = FabricLoader.getInstance().configDir.resolve("${CobbleBallworks.MOD_ID}.json")) {
        val json = try {
            if (Files.isRegularFile(path)) JsonParser.parseString(Files.readString(path)).asJsonObject else JsonObject()
        } catch (e: Exception) {
            CobbleBallworks.LOGGER.error("Could not read {}, using the defaults: {}", path, e.toString())
            JsonObject()
        }
        val defaults = Values()
        values = Values(
            hammerDamagePerStage = json.int("hammerDamagePerStage", defaults.hammerDamagePerStage).coerceIn(0, 1000),
            allowAnvilAutoreloading = json.bool("allowAnvilAutoreloading", defaults.allowAnvilAutoreloading),
            mechanicalAnvilSound = json.bool("mechanicalAnvilSound", defaults.mechanicalAnvilSound),
        )
        save(path)
    }

    private fun save(path: Path) {
        val out = JsonObject()
        out.add("_comments", JsonObject().apply { COMMENTS.forEach { (key, text) -> addProperty(key, text) } })
        out.addProperty("hammerDamagePerStage", values.hammerDamagePerStage)
        out.addProperty("allowAnvilAutoreloading", values.allowAnvilAutoreloading)
        out.addProperty("mechanicalAnvilSound", values.mechanicalAnvilSound)
        try {
            Files.createDirectories(path.parent)
            Files.writeString(path, GsonBuilder().setPrettyPrinting().create().toJson(out) + "\n")
        } catch (e: Exception) {
            CobbleBallworks.LOGGER.error("Could not write {}: {}", path, e.toString())
        }
    }

    private fun JsonObject.int(key: String, default: Int): Int =
        runCatching { get(key)?.asInt }.getOrNull() ?: default

    private fun JsonObject.bool(key: String, default: Boolean): Boolean =
        runCatching { get(key)?.asBoolean }.getOrNull() ?: default
}
