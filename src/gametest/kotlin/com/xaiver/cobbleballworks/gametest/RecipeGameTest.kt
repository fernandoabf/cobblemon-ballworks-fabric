package com.xaiver.cobbleballworks.gametest

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.xaiver.cobbleballworks.content.CatchClass
import com.xaiver.cobbleballworks.content.DisabledRecipes
import com.xaiver.cobbleballworks.content.ForgeBall
import com.mojang.serialization.JsonOps
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.crafting.SmithingRecipeInput
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import kotlin.io.path.readText

/** Section 3 of docs/PLANO.md, checked by actually crafting with the server's recipe manager. */
class RecipeGameTest : FabricGameTest {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun apricornsCookIntoCookedApricorns(helper: GameTestHelper) {
        val checks = Checks(helper)
        val level = helper.level
        for (color in RegistryGameTest.COLORS) {
            val raw = ItemStack(cobblemonItem("${color}_apricorn"))
            val matches = level.recipeManager.getRecipesFor(RecipeType.SMELTING, SingleRecipeInput(raw), level)
            // Cobblemon's apricorn -> dye smelting would fight over the furnace; it must be gone.
            checks.check(matches.size == 1) { "$color apricorn has ${matches.size} smelting recipes: ${matches.map { it.id() }}" }
            val recipe = matches.firstOrNull()?.value() ?: continue
            val result = recipe.getResultItem(level.registryAccess())
            checks.check(result.item == modItem("cooked_${color}_apricorn") && result.count == 1) {
                "$color apricorn smelts into $result"
            }
            checks.check(recipe.experience == 0.1f && recipe.cookingTime == 200) {
                "cooking $color apricorn: ${recipe.experience} XP, ${recipe.cookingTime} ticks (expected 0.1, 200)"
            }
        }
        checks.finish()
    }

    /**
     * Every disc recipe is Cobblemon's recipe for that ball with cooked apricorns: rebuilds the grid
     * from the original file inside the Cobblemon jar and crafts it.
     */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun discsUseCobblemonsBallRecipeWithCookedApricorns(helper: GameTestHelper) {
        val checks = Checks(helper)
        val level = helper.level
        val cobblemon = FabricLoader.getInstance().getModContainer("cobblemon").orElseThrow()
        for (ball in RegistryGameTest.BALLS) {
            val path = cobblemon.findPath("data/cobblemon/recipe/${ball}_ball.json").orElse(null)
            val ownCount = RegistryGameTest.OWN_DISC_RECIPES[ball]
            if (ownCount != null) {
                // No Cobblemon recipe to follow: the forge's own grid makes the discs.
                checks.check(path == null) { "Cobblemon now has a recipe for ${ball}_ball: follow it" }
                val own = modJsonResources("data/$MOD_ID/recipe").firstOrNull { it.path.endsWith("/${ball}_lid_blank.json") }
                val crafted = own?.let { craft(level, grid(it.json.asJsonObject, cook = false)) } ?: ItemStack.EMPTY
                checks.check(crafted.item == modItem("${ball}_lid_blank") && crafted.count == ownCount) {
                    "$ball: the forge's own disc grid gives $crafted, expected ${ownCount}x ${ball}_lid_blank"
                }
                continue
            }
            if (path == null) {
                checks.fail("Cobblemon has no recipe file for ${ball}_ball")
                continue
            }
            val original = JsonParser.parseString(path.readText()).asJsonObject
            val catchClass = ForgeBall.entries.first { it.id == ball }.catchClass
            val disc = modItem("${ball}_lid_blank")
            // Cobblemon's grid with cooked apricorns and the middle metal of the ball's catch class.
            val cooked = grid(original, cook = true, tier = catchClass.tier)
            val crafted = craft(level, cooked)
            checks.check(crafted.item == disc && crafted.count == catchClass.discs) {
                "$ball: Cobblemon's grid with cooked apricorns and tier ${catchClass.tier} gives $crafted, " +
                    "expected ${catchClass.discs}x ${ball}_lid_blank"
            }
            // With raw apricorns there must be nothing: the ball now only comes from the forge. Master
            // and Beast use no apricorns, so their raw grid is the disc grid itself.
            val raw = craft(level, grid(original, cook = false, tier = catchClass.tier))
            val usesApricorns = cooked.items().map { it.item } != grid(original, cook = false, tier = catchClass.tier).items().map { it.item }
            checks.check(if (usesApricorns) raw.isEmpty else raw.item == disc) {
                "$ball: Cobblemon's original grid gives $raw"
            }
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun cobblemonsNativeBallRecipesAreDisabled(helper: GameTestHelper) {
        val checks = Checks(helper)
        val recipes = helper.level.recipeManager
        for (ball in RegistryGameTest.BALLS) {
            checks.check(recipes.byKey(cobblemonId("${ball}_ball")).isEmpty) { "cobblemon:${ball}_ball is still loaded" }
        }
        for (color in RegistryGameTest.COLORS) {
            checks.check(recipes.byKey(cobblemonId("${color}_apricorn_smelt_to_dye")).isEmpty) {
                "cobblemon:${color}_apricorn_smelt_to_dye is still loaded"
            }
        }
        // Only the overridden files are gone: the rest of Cobblemon's recipes still load.
        for (kept in listOf("campfire_pot_red", "apricorn_planks", "gilded_chest")) {
            checks.check(recipes.byKey(cobblemonId(kept)).isPresent) { "cobblemon:$kept should still exist" }
        }
        // No crafting recipe other than ours makes a forged ball.
        val forged = RegistryGameTest.BALLS.map { cobblemonId("${it}_ball") }.toSet()
        val registries = helper.level.registryAccess()
        for (holder in recipes.getAllRecipesFor(RecipeType.CRAFTING)) {
            val result = BuiltInRegistries.ITEM.getKey(holder.value().getResultItem(registries).item)
            checks.check(result !in forged || holder.id().namespace == MOD_ID) { "${holder.id()} also crafts $result" }
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun ballIsLidPlusStoneButtonPlusIronBase(helper: GameTestHelper) {
        val checks = Checks(helper)
        val level = helper.level
        val base = ItemStack(modItem("pokeball_base")!!)
        for (ball in RegistryGameTest.BALLS) {
            val lid = ItemStack(modItem("${ball}_lid")!!)
            // Shapeless: any arrangement works, even in the 2x2 inventory grid.
            val ok = craft(level, CraftingInput.of(2, 2, listOf(ItemStack.EMPTY, base, lid, ItemStack(Items.STONE_BUTTON))))
            checks.check(ok.item == cobblemonItem("${ball}_ball") && ok.count == 1) { "$ball assembly gives $ok" }
            val woodenButton = craft(level, CraftingInput.of(2, 2, listOf(lid, ItemStack(Items.OAK_BUTTON), base, ItemStack.EMPTY)))
            checks.check(woodenButton.isEmpty) { "$ball assembly accepts a wooden button: $woodenButton" }
            val noBase = craft(level, CraftingInput.of(2, 2, listOf(lid, ItemStack(Items.STONE_BUTTON), ItemStack.EMPTY, ItemStack.EMPTY)))
            checks.check(noBase.isEmpty) { "$ball assembly works without a base: $noBase" }
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun hammersAndIronDiscs(helper: GameTestHelper) {
        val checks = Checks(helper)
        val level = helper.level
        val stick = ItemStack(Items.STICK)
        for ((hammer, material) in HAMMER_MATERIALS) {
            val id = modId("${hammer}_hammer")
            val stack = when {
                material.startsWith("#") -> {
                    val tag = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, ResourceLocation.parse(material.substring(1))))
                    val first = tag.orElse(null)?.firstOrNull()
                    if (first == null) {
                        // Ruby, sapphire and aluminium are not in every pack: no material, no recipe.
                        checks.check(level.recipeManager.byKey(id).isEmpty) { "$id loaded although $material is empty" }
                        continue
                    }
                    ItemStack(first)
                }
                else -> ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(material)))
            }
            val m = stack
            val result = craft(level, CraftingInput.of(3, 3, listOf(m, m, m, m, stick, m, ItemStack.EMPTY, stick, ItemStack.EMPTY)))
            checks.check(result.item == modItem("${hammer}_hammer")) { "$hammer hammer grid with $material gives $result" }
        }
        val iron = ItemStack(Items.IRON_INGOT)
        for (row in 0..2) {
            val items = MutableList(9) { ItemStack.EMPTY }
            for (col in 0..2) items[row * 3 + col] = iron
            val result = craft(level, CraftingInput.of(3, 3, items))
            checks.check(result.item == modItem("base_blank") && result.count == 5) { "3 iron ingots in row $row give $result" }
        }
        checks.finish()
    }

    /**
     * A disc costs what its ball helps a capture (CatchClass, from Cobblemon's catch multipliers): the
     * metal in the middle, the discs per craft and the force on the anvil all follow the class, and a
     * stronger class never costs less.
     */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun discCostFollowsTheCatchClass(helper: GameTestHelper) {
        val checks = Checks(helper)
        val recipes = modJsonResources("data/$MOD_ID/recipe").associate { it.path.removePrefix("data/$MOD_ID/recipe/") to it.json.asJsonObject }
        for (ball in ForgeBall.entries) {
            val catchClass = ball.catchClass
            val disc = recipes["${ball.id}_lid_blank.json"]
            if (disc == null) {
                checks.fail("${ball.id}: no disc recipe")
                continue
            }
            val tiers = disc.getAsJsonObject("key").entrySet().mapNotNull { (_, value) ->
                value.asJsonObject["tag"]?.asString?.let { TIER_TAG.matchEntire(it)?.groupValues?.get(1)?.toInt() }
            }
            val expectedTiers = if (catchClass.tier > 0) listOf(catchClass.tier) else emptyList()
            checks.check(tiers == expectedTiers) { "${ball.id} (${catchClass.name}): middle tier $tiers, expected $expectedTiers" }
            checks.check(disc.getAsJsonObject("result")["count"].asInt == catchClass.discs) {
                "${ball.id} (${catchClass.name}): ${disc.getAsJsonObject("result")["count"]} discs, expected ${catchClass.discs}"
            }
            val force = recipes["anvil/${ball.id}_lid.json"]?.get("force")?.asInt ?: 5
            checks.check(force == catchClass.force) { "${ball.id} (${catchClass.name}): force $force, expected ${catchClass.force}" }
        }
        // Classes are listed from the weakest to the strongest ball: none may be cheaper than the one before.
        CatchClass.entries.zipWithNext().forEach { (weaker, stronger) ->
            val cheaper = stronger.discs > weaker.discs || stronger.force < weaker.force ||
                (stronger.tier in 1 until weaker.tier)
            checks.check(!cheaper) { "${stronger.name} costs less than ${weaker.name}" }
        }
        checks.finish()
    }

    /**
     * Other mods' recipes for a forged ball go too, whatever their id: Cobblemon Parts Refabricated
     * overrides Cobblemon's ids and adds Cherish and Ancient Origin ones Cobblemon does not have.
     */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun otherModsBallRecipesAreDropped(helper: GameTestHelper) {
        val checks = Checks(helper)
        fun recipe(result: String) = JsonParser.parseString(
            """{"type": "minecraft:crafting_shapeless", "ingredients": [{"item": "minecraft:stick"}], "result": {"id": "$result", "count": 1}}""",
        )
        val recipes = mapOf(
            cobblemonId("cherish_ball") to recipe("cobblemon:cherish_ball"),
            ResourceLocation.fromNamespaceAndPath("othermod", "parts/cherish_ball") to recipe("cobblemon:cherish_ball"),
            ResourceLocation.fromNamespaceAndPath("othermod", "parts/poke_ball_old_format") to
                JsonParser.parseString("""{"type": "minecraft:crafting_shapeless", "result": {"item": "cobblemon:poke_ball"}}"""),
            ResourceLocation.fromNamespaceAndPath("othermod", "stick") to recipe("minecraft:stick"),
            ResourceLocation.fromNamespaceAndPath("othermod", "red_apricorn") to recipe("cobblemon:red_apricorn"),
            modId("cherish_ball") to recipe("cobblemon:cherish_ball"),
        )
        val kept = DisabledRecipes.filter(recipes, otherMods = true).keys.map { it.toString() }.toSet()
        checks.check(kept == setOf("othermod:stick", "othermod:red_apricorn", "$MOD_ID:cherish_ball")) { "kept $kept" }
        val lenient = DisabledRecipes.filter(recipes, otherMods = false).keys.map { it.toString() }.toSet()
        checks.check("cobblemon:cherish_ball" !in lenient && "othermod:parts/cherish_ball" in lenient) {
            "with disableOtherModsBallRecipes off only Cobblemon's ids should go: $lenient"
        }
        checks.finish()
    }

    /** Like vanilla netherite tools: the diamond hammer upgraded at a smithing table, keeping what it had. */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun netheriteHammerIsUpgradedAtTheSmithingTable(helper: GameTestHelper) {
        val checks = Checks(helper)
        val level = helper.level
        val efficiency = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.EFFICIENCY)
        val template = ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
        val ingot = ItemStack(Items.NETHERITE_INGOT)

        val diamond = ItemStack(modItem("diamond_hammer")!!)
        diamond.enchant(efficiency, 3)
        diamond.damageValue = 100
        val result = smith(level, SmithingRecipeInput(template, diamond, ingot))
        checks.check(result.item == modItem("netherite_hammer")) { "smithing a diamond hammer gives $result" }
        checks.check(EnchantmentHelper.getItemEnchantmentLevel(efficiency, result) == 3) { "the upgrade lost Efficiency III" }
        checks.check(result.damageValue == 100 && result.maxDamage == 2031) {
            "the upgrade should keep the wear: ${result.damageValue}/${result.maxDamage}"
        }

        val iron = smith(level, SmithingRecipeInput(template, ItemStack(modItem("iron_hammer")!!), ingot))
        checks.check(iron.isEmpty) { "an iron hammer should not become netherite ($iron)" }
        val noTemplate = smith(level, SmithingRecipeInput(ItemStack.EMPTY, ItemStack(modItem("diamond_hammer")!!), ingot))
        checks.check(noTemplate.isEmpty) { "the upgrade needs the netherite upgrade template ($noTemplate)" }
        checks.finish()
    }

    private fun smith(level: Level, input: SmithingRecipeInput): ItemStack =
        level.recipeManager.getRecipeFor(RecipeType.SMITHING, input, level)
            .map { it.value().assemble(input, level.registryAccess()) }
            .orElse(ItemStack.EMPTY)

    private fun craft(level: Level, input: CraftingInput): ItemStack =
        level.recipeManager.getRecipeFor(RecipeType.CRAFTING, input, level)
            .map { it.value().assemble(input, level.registryAccess()) }
            .orElse(ItemStack.EMPTY)

    /**
     * The 3x3 grid of a shaped recipe file, with one item per ingredient: cooked apricorns if [cook],
     * and Cobblemon's tier material swapped for [tier]'s when given (0: keep it).
     */
    private fun grid(recipe: JsonObject, cook: Boolean, tier: Int = 0): CraftingInput {
        val pattern = recipe.getAsJsonArray("pattern").map { it.asString }
        val key = recipe.getAsJsonObject("key")
        val items = pattern.flatMap { row ->
            row.map { symbol ->
                if (symbol == ' ') {
                    ItemStack.EMPTY
                } else {
                    var json = key[symbol.toString()]
                    val tag = json.asJsonObject["tag"]?.asString
                    if (tier > 0 && tag != null && TIER_TAG.matches(tag)) {
                        json = JsonParser.parseString("""{"tag": "cobblemon:tier_${tier}_poke_ball_materials"}""")
                    }
                    val ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, json).getOrThrow()
                    val item = ingredient.items.first().item
                    val id = BuiltInRegistries.ITEM.getKey(item)
                    if (cook && id.namespace == "cobblemon" && id.path.endsWith("_apricorn")) {
                        ItemStack(modItem("cooked_${id.path}")!!)
                    } else {
                        ItemStack(item)
                    }
                }
            }
        }
        return CraftingInput.of(pattern.first().length, pattern.size, items)
    }

    private companion object {
        fun cobblemonId(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", path)

        /** Cobblemon's ball material tags, cobblemon:tier_N_poke_ball_materials. */
        val TIER_TAG = Regex("""cobblemon:tier_(\d)_poke_ball_materials""")

        fun cobblemonItem(path: String) = BuiltInRegistries.ITEM.get(cobblemonId(path))

        /** hammer -> material of its recipe (Pixelmon: 5 of the material + 2 sticks). */
        val HAMMER_MATERIALS = mapOf(
            "wood" to "#minecraft:planks",
            "stone" to "#minecraft:stone_tool_materials",
            "iron" to "#c:ingots/iron",
            "gold" to "#c:ingots/gold",
            "diamond" to "#c:gems/diamond",
            "aluminium" to "#c:ingots/aluminum",
            "ruby" to "#c:gems/ruby",
            "sapphire" to "#c:gems/sapphire",
            "amethyst" to "minecraft:amethyst_shard",
            "fire_stone" to "cobblemon:fire_stone",
            "water_stone" to "cobblemon:water_stone",
            "leaf_stone" to "cobblemon:leaf_stone",
            "thunder_stone" to "cobblemon:thunder_stone",
            "sun_stone" to "cobblemon:sun_stone",
            "moon_stone" to "cobblemon:moon_stone",
            "dawn_stone" to "cobblemon:dawn_stone",
            "dusk_stone" to "cobblemon:dusk_stone",
        )
    }
}
