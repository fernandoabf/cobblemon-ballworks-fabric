package com.xaiver.cobbleballworks.gametest

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files

/**
 * Headless Fabric gametests ("fabric-gametest" entrypoint in src/gametest/resources/fabric.mod.json).
 *
 * Must be a regular class with a public no-arg constructor, NOT a Kotlin `object`:
 * fabric-gametest-api-v1 creates a new instance via `getConstructor().newInstance()`
 * for every test method.
 */
class CobbleBallworksGameTest : FabricGameTest {

    /** Trivial smoke test: the toolchain works and the mods we depend on are really loaded. */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun toolchainSmoke(helper: GameTestHelper) {
        val loader = FabricLoader.getInstance()
        if (!loader.isModLoaded("cobbleballworks")) {
            helper.fail("cobbleballworks is not loaded")
        }

        val cobblemon = loader.getModContainer("cobblemon").orElse(null)
        if (cobblemon == null) {
            LOGGER.warn("[ballworks-gametest] Cobblemon is NOT loaded in this run")
        } else {
            val expected = listOf("poke_ball", "great_ball", "ultra_ball", "master_ball", "red_apricorn")
            val missing = expected.filterNot {
                BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("cobblemon", it))
            }
            if (missing.isNotEmpty()) {
                helper.fail("Cobblemon is loaded but these items are missing from the registry: $missing")
            }
            val cobblemonItems = BuiltInRegistries.ITEM.keySet().count { it.namespace == "cobblemon" }
            LOGGER.info(
                "[ballworks-gametest] Cobblemon {} loaded; registered: {}; {} cobblemon:* items in the item registry",
                cobblemon.metadata.version.friendlyString,
                expected.joinToString { "cobblemon:$it" },
                cobblemonItems,
            )
        }

        helper.succeed()
    }

    /** The shipped metadata keeps the credits and the license (CLAUDE.md) and its icon exists. */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun metadataKeepsCreditsLicenseAndIcon(helper: GameTestHelper) {
        val checks = Checks(helper)
        val mod = FabricLoader.getInstance().getModContainer("cobbleballworks").orElseThrow()
        val meta = mod.metadata
        checks.check("MIT" in meta.license) { "the license should be MIT (${meta.license})" }
        checks.check(meta.authors.any { "xaiver" in it.name }) { "the original author must stay in authors (${meta.authors.map { it.name }})" }
        checks.check(meta.version.friendlyString.matches(Regex("""\d+\.\d+\.\d+\+1\.21\.1"""))) {
            "unexpected version ${meta.version.friendlyString}"
        }
        val icon = meta.getIconPath(128).flatMap { mod.findPath(it) }
        checks.check(icon.isPresent && Files.isRegularFile(icon.get())) { "icon ${meta.getIconPath(128)} not found" }
        checks.finish()
    }

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger("cobbleballworks-gametest")
    }
}
