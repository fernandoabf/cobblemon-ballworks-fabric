package com.xaiver.cobbleballworks.gametest

import com.xaiver.cobbleballworks.item.HammerItem
import com.xaiver.cobbleballworks.registry.ModCreativeTab
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks

/** Section 8 of docs/PLANO.md: everything the mod registers exists, with the Pixelmon numbers. */
class RegistryGameTest : FabricGameTest {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun itemsAreRegistered(helper: GameTestHelper) {
        val checks = Checks(helper)

        for (color in COLORS) {
            val item = modItem("cooked_${color}_apricorn")
            checks.check(item != null) { "missing cooked_${color}_apricorn" }
            checks.check(item == null || ItemStack(item).maxStackSize == 64) { "cooked_${color}_apricorn should stack to 64" }
            checks.check(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("cobblemon", "${color}_apricorn"))) {
                "Cobblemon has no ${color}_apricorn"
            }
        }
        for (ball in BALLS) {
            checks.check(modItem("${ball}_lid_blank") != null) { "missing disc ${ball}_lid_blank" }
            checks.check(modItem("${ball}_lid") != null) { "missing lid ${ball}_lid" }
            checks.check(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("cobblemon", "${ball}_ball"))) {
                "Cobblemon has no ${ball}_ball"
            }
        }
        checks.check(modItem("base_blank") != null) { "missing base_blank (iron disc)" }
        checks.check(modItem("pokeball_base") != null) { "missing pokeball_base (iron base)" }

        // No crafting recipe in Cobblemon -> nothing to forge; the mold had no use.
        for (unforgeable in listOf("cherish", "ancient_origin")) {
            checks.check(modItem("${unforgeable}_lid") == null && modItem("${unforgeable}_lid_blank") == null) {
                "$unforgeable should not have a disc/lid"
            }
        }
        checks.check(modItem("ball_mold") == null) { "ball_mold should not exist" }

        for ((material, expected) in HAMMERS) {
            val item = modItem("${material}_hammer")
            if (item !is HammerItem) {
                checks.fail("${material}_hammer is missing or not a HammerItem")
                continue
            }
            val (strength, durability) = expected
            val stack = ItemStack(item)
            checks.check(item.strength == strength) { "${material}_hammer strength ${item.strength}, expected $strength" }
            checks.check(stack.maxDamage == durability) { "${material}_hammer durability ${stack.maxDamage}, expected $durability" }
            checks.check(stack.maxStackSize == 1) { "${material}_hammer should not stack" }
            checks.check(stack.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 1.0f) {
                "${material}_hammer should mine other blocks like a bare hand"
            }
            checks.check(item.isEnchantable(stack) && item.enchantmentValue > 0) { "${material}_hammer should be enchantable" }
        }
        checks.check(BuiltInRegistries.ITEM.keySet().count { it.namespace == MOD_ID && it.path.endsWith("_hammer") } == HAMMERS.size) {
            "unexpected number of hammers"
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun creativeTabHasEveryItem(helper: GameTestHelper) {
        val checks = Checks(helper)
        val server = helper.level.server
        val tab = ModCreativeTab.BALLWORKS
        tab.buildContents(CreativeModeTab.ItemDisplayParameters(server.worldData.enabledFeatures(), false, server.registryAccess()))
        val inTab = tab.displayItems.map { BuiltInRegistries.ITEM.getKey(it.item) }.toSet()
        val modItems = BuiltInRegistries.ITEM.keySet().filter { it.namespace == MOD_ID }.toSet()
        checks.check(modItems.isNotEmpty()) { "no items registered" }
        for (id in modItems - inTab) checks.fail("$id is not in the creative tab")
        checks.check(tab.displayItems.size == inTab.size) { "the creative tab lists an item twice" }
        checks.finish()
    }

    companion object {
        val COLORS = listOf("red", "yellow", "green", "blue", "pink", "black", "white")

        /** Every Cobblemon ball with a crafting recipe (46): all of them are forged. */
        val BALLS = listOf(
            "poke", "great", "ultra", "level", "moon", "friend", "love", "safari", "heavy", "fast", "repeat",
            "timer", "nest", "net", "dive", "luxury", "heal", "dusk", "premier", "sport", "quick", "lure",
            "citrine", "verdant", "azure", "roseate", "slate", "park", "dream", "beast", "master",
            "ancient_poke", "ancient_great", "ancient_ultra", "ancient_heavy", "ancient_leaden", "ancient_gigaton",
            "ancient_feather", "ancient_wing", "ancient_jet", "ancient_ivory", "ancient_citrine", "ancient_verdant",
            "ancient_azure", "ancient_roseate", "ancient_slate",
        )

        /** material -> (strength, durability), from Pixelmon 1.12.2 ItemHammer/GenericHammer. */
        val HAMMERS = mapOf(
            "wood" to (1 to 59),
            "stone" to (2 to 131),
            "iron" to (3 to 250),
            "gold" to (4 to 32),
            "diamond" to (5 to 1561),
            "aluminium" to (3 to 200),
            "ruby" to (4 to 300),
            "sapphire" to (4 to 300),
            "amethyst" to (4 to 300),
            "leaf_stone" to (4 to 250),
            "fire_stone" to (6 to 1561),
            "water_stone" to (6 to 1561),
            "thunder_stone" to (6 to 1561),
            "sun_stone" to (6 to 1561),
            "moon_stone" to (6 to 1561),
            "dawn_stone" to (6 to 1561),
            "dusk_stone" to (6 to 1561),
        )
    }
}
