package com.xaiver.cobbleballworks.content

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * Hammer tiers, with the numbers of Pixelmon 1.12.2 (ItemHammer / GenericHammer).
 *
 * [strength] is both the force of one strike and the mining speed on the anvil, so a better
 * hammer hits harder and faster. [attackDamage] is the attack damage modifier (Pixelmon tools:
 * 2 + the tool material bonus). The order is the order of Pixelmon's creative tab, with netherite
 * (newer than Pixelmon's hammers) after diamond.
 */
enum class HammerMaterial(
    val id: String,
    val strength: Int,
    val durability: Int,
    val enchantability: Int,
    val attackDamage: Double,
    private val repairMaterial: RepairMaterial,
    /** Survives fire and lava as a dropped item, like vanilla netherite gear. */
    val fireResistant: Boolean = false,
) {
    WOOD("wood", 1, 59, 15, 2.0, RepairMaterial.Tag(ItemTags.PLANKS)),
    STONE("stone", 2, 131, 5, 3.0, RepairMaterial.Tag(ItemTags.STONE_TOOL_MATERIALS)),
    IRON("iron", 3, 250, 14, 4.0, RepairMaterial.Tag(c("ingots/iron"))),
    GOLD("gold", 4, 32, 22, 2.0, RepairMaterial.Tag(c("ingots/gold"))),
    DIAMOND("diamond", 5, 1561, 10, 5.0, RepairMaterial.Tag(c("gems/diamond"))),

    /**
     * Not in Pixelmon: the top hammer, upgraded from the diamond one at a smithing table like
     * vanilla netherite tools (vanilla's durability, enchantability and +1 damage over diamond).
     * Strength 8 finishes a regular disc in 3 strikes and a Master/Beast Ball disc in 6 (9 with
     * the strength 6 evolution stone hammers).
     */
    NETHERITE("netherite", 8, 2031, 15, 6.0, RepairMaterial.Tag(c("ingots/netherite")), fireResistant = true),
    ALUMINIUM("aluminium", 3, 200, 14, 4.0, RepairMaterial.Tag(c("ingots/aluminum"))),
    RUBY("ruby", 4, 300, 10, 5.0, RepairMaterial.Tag(c("gems/ruby"))),
    SAPPHIRE("sapphire", 4, 300, 10, 5.0, RepairMaterial.Tag(c("gems/sapphire"))),
    AMETHYST("amethyst", 4, 300, 10, 5.0, RepairMaterial.Single(ResourceLocation.withDefaultNamespace("amethyst_shard"))),
    FIRE_STONE("fire_stone", 6, 1561, 10, 6.0, RepairMaterial.cobblemon("fire_stone")),
    WATER_STONE("water_stone", 6, 1561, 10, 6.0, RepairMaterial.cobblemon("water_stone")),
    LEAF_STONE("leaf_stone", 4, 250, 14, 5.0, RepairMaterial.cobblemon("leaf_stone")),
    THUNDER_STONE("thunder_stone", 6, 1561, 10, 3.0, RepairMaterial.cobblemon("thunder_stone")),
    SUN_STONE("sun_stone", 6, 1561, 10, 3.0, RepairMaterial.cobblemon("sun_stone")),
    MOON_STONE("moon_stone", 6, 1561, 10, 3.0, RepairMaterial.cobblemon("moon_stone")),
    DAWN_STONE("dawn_stone", 6, 1561, 10, 3.0, RepairMaterial.cobblemon("dawn_stone")),
    DUSK_STONE("dusk_stone", 6, 1561, 10, 3.0, RepairMaterial.cobblemon("dusk_stone")),
    ;

    val itemPath: String get() = "${id}_hammer"

    /** Whether [stack] repairs this hammer in a vanilla anvil (the same material as its recipe). */
    fun isRepairMaterial(stack: ItemStack): Boolean = repairMaterial.matches(stack)

    /** Checked on every use instead of being cached, so it follows tag reloads. */
    sealed interface RepairMaterial {
        fun matches(stack: ItemStack): Boolean

        data class Tag(val tag: TagKey<Item>) : RepairMaterial {
            override fun matches(stack: ItemStack): Boolean = stack.`is`(tag)
        }

        data class Single(val id: ResourceLocation) : RepairMaterial {
            override fun matches(stack: ItemStack): Boolean = stack.itemHolder.`is`(id)
        }

        companion object {
            fun cobblemon(path: String): RepairMaterial =
                Single(ResourceLocation.fromNamespaceAndPath("cobblemon", path))
        }
    }
}

private fun c(path: String): TagKey<Item> =
    TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path))
