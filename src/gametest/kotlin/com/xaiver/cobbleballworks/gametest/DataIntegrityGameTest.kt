package com.xaiver.cobbleballworks.gametest

import com.google.gson.JsonObject
import com.xaiver.cobbleballworks.content.ForgeBall
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.storage.loot.LootTable

/**
 * Every data file the mod ships must actually load. Minecraft only logs a broken file and moves on
 * (and a tag with one missing entry is dropped entirely, which would break, say, every pickaxe if
 * it were minecraft:mineable/pickaxe), so these tests turn those log lines into failures.
 * Also the static checks of section 8: every item has a model and every model texture exists.
 */
class DataIntegrityGameTest : FabricGameTest {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun everyTagLoads(helper: GameTestHelper) {
        val checks = Checks(helper)
        val tagFiles = modJsonResources("data").mapNotNull { res -> TAG_FILE.matchEntire(res.path)?.let { it to res } }
        checks.check(tagFiles.isNotEmpty()) { "the mod ships no tags" }
        for ((match, res) in tagFiles) {
            val (namespace, registryDir, path) = match.destructured
            val registry: Registry<*> = when (registryDir) {
                "item" -> BuiltInRegistries.ITEM
                "block" -> BuiltInRegistries.BLOCK
                else -> {
                    checks.fail("${res.path}: tests do not know the registry '$registryDir'")
                    continue
                }
            }
            checkTag(checks, registry, ResourceLocation.fromNamespaceAndPath(namespace, path), res)
        }
        checks.finish()
    }

    private fun <T> checkTag(checks: Checks, registry: Registry<T>, id: ResourceLocation, res: ModResource) {
        val tag = registry.getTag(TagKey.create(registry.key(), id)).orElse(null)
        if (tag == null) {
            checks.fail("tag #$id (${res.path}) did not load")
            return
        }
        for (value in res.json.asJsonObject.getAsJsonArray("values")) {
            val (entry, required) = if (value.isJsonObject) {
                value.asJsonObject["id"].asString to (value.asJsonObject["required"]?.asBoolean ?: true)
            } else {
                value.asString to true
            }
            if (!required) continue
            if (entry.startsWith("#")) {
                val nested = ResourceLocation.parse(entry.substring(1))
                checks.check(registry.getTag(TagKey.create(registry.key(), nested)).isPresent) { "#$id refers to missing tag $entry" }
            } else {
                val element = ResourceLocation.parse(entry)
                checks.check(tag.any { it.`is`(element) }) { "#$id does not contain $entry" }
            }
        }
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun everyRecipeLoads(helper: GameTestHelper) {
        val checks = Checks(helper)
        val recipes = helper.level.server.recipeManager
        var count = 0
        for (res in modJsonResources("data/$MOD_ID/recipe")) {
            val json = res.json.asJsonObject
            // Conditional recipes (hammers of materials the pack may not have) are tested on their own.
            if (json.has(LOAD_CONDITIONS)) continue
            val id = modId(res.path.removePrefix("data/$MOD_ID/recipe/").removeSuffix(".json"))
            checks.check(recipes.byKey(id).isPresent) { "recipe $id did not load" }
            count++
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun everyLootTableLoads(helper: GameTestHelper) {
        val checks = Checks(helper)
        val registries = helper.level.server.reloadableRegistries()
        for (res in modJsonResources("data/$MOD_ID/loot_table")) {
            val id = modId(res.path.removePrefix("data/$MOD_ID/loot_table/").removeSuffix(".json"))
            val table = registries.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id))
            checks.check(table !== LootTable.EMPTY) { "loot table $id did not load" }
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun everyItemHasModelAndTranslations(helper: GameTestHelper) {
        val checks = Checks(helper)
        val models = modFiles("assets/$MOD_ID/models/item").toSet()
        val langs = modJsonResources("assets/$MOD_ID/lang").associate { it.path.substringAfterLast('/') to it.json.asJsonObject }
        val enUs = langs["en_us.json"]
        val ptBr = langs["pt_br.json"]
        if (enUs == null || ptBr == null) {
            checks.fail("en_us.json and pt_br.json must both exist (found ${langs.keys})")
            checks.finish()
            return
        }
        for (id in BuiltInRegistries.ITEM.keySet().filter { it.namespace == MOD_ID }) {
            checks.check("assets/$MOD_ID/models/item/${id.path}.json" in models) { "item $id has no item model" }
            val key = BuiltInRegistries.ITEM.get(id).descriptionId
            checks.check(enUs.has(key)) { "en_us has no name for $id ($key)" }
            checks.check(ptBr.has(key)) { "pt_br has no name for $id ($key)" }
        }
        for (id in BuiltInRegistries.BLOCK.keySet().filter { it.namespace == MOD_ID }) {
            checks.check(modFiles("assets/$MOD_ID/blockstates").contains("assets/$MOD_ID/blockstates/${id.path}.json")) {
                "block $id has no blockstate"
            }
        }
        for (key in enUs.keySet() - ptBr.keySet()) checks.fail("pt_br is missing $key")
        for (key in ptBr.keySet() - enUs.keySet()) checks.fail("en_us is missing $key")
        checks.finish()
    }

    /**
     * The anvils draw each ball's piece with models/piece/<ball>_lid.json: the top half of
     * Cobblemon's 3D ball (the ancient one for the Hisuian balls) with the ball's own 3D texture.
     * [everyModelResolves] checks that parents and textures exist; this checks that none is missing.
     */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun everyBallHasAPieceModel(helper: GameTestHelper) {
        val checks = Checks(helper)
        val models = modJsonResources("assets/$MOD_ID/models/piece").associate { it.path.substringAfterLast('/') to it.json.asJsonObject }
        for (ball in ForgeBall.entries) {
            val model = models["${ball.id}_lid.json"]
            if (model == null) {
                checks.fail("no piece model for ${ball.id}")
                continue
            }
            val geometry = if (ball.id.startsWith("ancient_")) "ancient_lid" else "lid"
            checks.check(model["parent"]?.asString == "$MOD_ID:piece/$geometry") { "${ball.id}: piece parent should be $geometry" }
            val texture = model["textures"]?.asJsonObject?.get("layer0")?.asString
            checks.check(texture == "cobblemon:item/poke_balls/models/${ball.ballId.path}") { "${ball.id}: piece texture is $texture" }
        }
        for (geometry in listOf("lid", "ancient_lid", "iron_base")) {
            val elements = models["$geometry.json"]?.get("elements")?.asJsonArray
            checks.check(elements != null && elements.size() > 0) { "piece/$geometry.json has no elements" }
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun everyModelResolves(helper: GameTestHelper) {
        val checks = Checks(helper)
        val ourFiles = modFiles("assets/$MOD_ID").toSet()
        val cobblemon = FabricLoader.getInstance().getModContainer("cobblemon").orElse(null)

        fun exists(kind: String, ref: String): Boolean? {
            val loc = ResourceLocation.parse(ref)
            val file = "assets/${loc.namespace}/$kind/${loc.path}" + if (kind == "textures") ".png" else ".json"
            return when (loc.namespace) {
                MOD_ID -> file in ourFiles
                "cobblemon" -> cobblemon?.findPath(file)?.isPresent
                else -> null // vanilla assets are not on a dedicated server's classpath
            }
        }

        for (res in modJsonResources("assets/$MOD_ID/models")) {
            val json = res.json.asJsonObject
            json["parent"]?.asString?.let { parent ->
                if (!parent.startsWith("builtin/")) {
                    checks.check(exists("models", parent) != false) { "${res.path}: parent $parent does not exist" }
                }
            }
            val textures = json["textures"]?.asJsonObject ?: JsonObject()
            for ((slot, value) in textures.entrySet()) {
                val ref = value.asString
                if (ref.startsWith("#")) continue
                checks.check(exists("textures", ref) != false) { "${res.path}: texture $slot = $ref does not exist" }
            }
        }
        for (res in modJsonResources("assets/$MOD_ID/blockstates")) {
            val variants = res.json.asJsonObject["variants"]?.asJsonObject ?: continue
            for ((variant, value) in variants.entrySet()) {
                val entries = if (value.isJsonArray) value.asJsonArray.map { it.asJsonObject } else listOf(value.asJsonObject)
                for (entry in entries) {
                    val model = entry["model"].asString
                    checks.check(exists("models", model) != false) { "${res.path} [$variant]: model $model does not exist" }
                }
            }
        }
        checks.finish()
    }

    private companion object {
        const val LOAD_CONDITIONS = "fabric:load_conditions"
        val TAG_FILE = Regex("data/([a-z0-9_.-]+)/tags/([a-z_]+)/(.+)\\.json")
    }
}
