package com.xaiver.cobbleballworks.gametest

import com.google.gson.JsonObject
import com.xaiver.cobbleballworks.block.BallAnvilBlock
import com.xaiver.cobbleballworks.block.MechanicalAnvilBlock
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

    /**
     * The Mechanical Anvil's press moves, so the renderer draws it (press + rod models) and the block
     * model must not have it too; the item keeps it. The heights the renderers use match the models.
     */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun anvilModelsMatchTheRenderers(helper: GameTestHelper) {
        val checks = Checks(helper)
        val models = modJsonResources("assets/$MOD_ID/models/block").associate { it.path.substringAfterLast('/') to it.json.asJsonObject }
        fun elements(file: String): Map<String, JsonObject> =
            models[file]?.getAsJsonArray("elements")?.map { it.asJsonObject }?.associateBy { it["name"]?.asString ?: "" } ?: emptyMap()
        fun top(element: JsonObject?): Double? = element?.getAsJsonArray("to")?.get(1)?.asDouble

        val machine = elements("mechanical_anvil.json")
        checks.check("press" !in machine) { "the block model still has the press: it would not move" }
        checks.check(top(machine["platform"]) == MechanicalAnvilBlock.PLATFORM_Y) {
            "MechanicalAnvilBlock.PLATFORM_Y is ${MechanicalAnvilBlock.PLATFORM_Y}, the platform's top is ${top(machine["platform"])}"
        }
        checks.check("press" in elements("mechanical_anvil_inventory.json")) { "the item model should show the press" }
        checks.check("press" in elements("mechanical_anvil_press.json")) { "missing the press model" }
        checks.check("rod" in elements("mechanical_anvil_rod.json")) { "missing the press rod model" }
        val anvil = elements("ball_anvil.json")
        checks.check(top(anvil["face"]) == BallAnvilBlock.TOP_Y) {
            "BallAnvilBlock.TOP_Y is ${BallAnvilBlock.TOP_Y}, the anvil's face top is ${top(anvil["face"])}"
        }
        checks.finish()
    }

    /**
     * Vanilla's anvil_top.png is only opaque in columns 3 to 12: a face that maps the outer columns
     * (as the automatic UV of the 12 px wide anvil face did) shows a see-through strip.
     */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun anvilTopTextureOnlyUsesItsOpaqueColumns(helper: GameTestHelper) {
        val checks = Checks(helper)
        for (res in modJsonResources("assets/$MOD_ID/models")) {
            val model = res.json.asJsonObject
            val textures = model.getAsJsonObject("textures")
            fun resolve(ref: String): String? {
                var value: String? = ref
                repeat(8) { if (value?.startsWith("#") == true) value = textures?.get(value!!.substring(1))?.asString }
                return value
            }
            for (element in model.getAsJsonArray("elements") ?: continue) {
                val e = element.asJsonObject
                val (x1, y1, z1) = e.getAsJsonArray("from").map { it.asDouble }
                val (x2, y2, z2) = e.getAsJsonArray("to").map { it.asDouble }
                for ((side, value) in e.getAsJsonObject("faces").entrySet()) {
                    val face = value.asJsonObject
                    if (resolve(face["texture"].asString)?.removePrefix("minecraft:") != "block/anvil_top") continue
                    // BlockElement's automatic UVs when a face has none.
                    val uv = face.getAsJsonArray("uv")?.map { it.asDouble } ?: when (side) {
                        "down" -> listOf(x1, 16 - z2, x2, 16 - z1)
                        "up" -> listOf(x1, z1, x2, z2)
                        "north" -> listOf(16 - x2, 16 - y2, 16 - x1, 16 - y1)
                        "south" -> listOf(x1, 16 - y2, x2, 16 - y1)
                        "west" -> listOf(z1, 16 - y2, z2, 16 - y1)
                        else -> listOf(16 - z2, 16 - y2, 16 - z1, 16 - y1)
                    }
                    checks.check(minOf(uv[0], uv[2]) >= 3.0 && maxOf(uv[0], uv[2]) <= 13.0) {
                        "${res.path}: ${e["name"]?.asString} $side maps anvil_top columns ${uv[0]}..${uv[2]}, outside the opaque 3..13"
                    }
                }
            }
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
