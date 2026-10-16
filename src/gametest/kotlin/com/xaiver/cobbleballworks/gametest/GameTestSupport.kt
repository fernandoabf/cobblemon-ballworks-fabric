package com.xaiver.cobbleballworks.gametest

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.relativeTo

const val MOD_ID = "cobbleballworks"

fun modId(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)

fun modItem(path: String): Item? = BuiltInRegistries.ITEM.getOptional(modId(path)).orElse(null)

/** Collects every failed check of a test, so one run reports all of them instead of the first. */
class Checks(private val helper: GameTestHelper) {
    private val errors = mutableListOf<String>()

    fun check(condition: Boolean, message: () -> String) {
        if (!condition) errors += message()
    }

    fun fail(message: String) {
        errors += message
    }

    /** Fails the test with every collected error, or marks it as passed. */
    fun finish() {
        if (errors.isEmpty()) {
            helper.succeed()
        } else {
            helper.fail("${errors.size} check(s) failed:\n - " + errors.joinToString("\n - "))
        }
    }
}

/** A JSON file shipped in the mod jar (`assets/...` or `data/...`), with its path inside the jar. */
data class ModResource(val path: String, val json: JsonElement)

/** Every `.json` under [root] (for example `data` or `assets/cobbleballworks/models`) in the mod. */
fun modJsonResources(root: String): List<ModResource> {
    val container = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()
    return container.rootPaths.flatMap { rootPath ->
        val dir = rootPath.resolve(root)
        if (!Files.isDirectory(dir)) return@flatMap emptyList()
        Files.walk(dir).use { stream ->
            stream.filter { it.isRegularFile() && it.extension == "json" }
                .map { ModResource(it.relativeTo(rootPath).invariantSeparatorsPathString, JsonParser.parseString(it.readText())) }
                .toList()
        }
    }.distinctBy { it.path }
}

/** Every file under [root] in the mod, as paths relative to the mod root. */
fun modFiles(root: String): List<String> {
    val container = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()
    return container.rootPaths.flatMap { rootPath ->
        val dir: Path = rootPath.resolve(root)
        if (!Files.isDirectory(dir)) return@flatMap emptyList()
        Files.walk(dir).use { stream ->
            stream.filter { it.isRegularFile() }.map { it.relativeTo(rootPath).invariantSeparatorsPathString }.toList()
        }
    }.distinct()
}
