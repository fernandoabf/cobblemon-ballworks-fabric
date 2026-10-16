package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.content.ApricornColor
import com.xaiver.cobbleballworks.content.ForgeBall
import com.xaiver.cobbleballworks.content.HammerMaterial
import com.xaiver.cobbleballworks.item.HammerItem
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import java.util.EnumMap

object ModItems {
    /** Furnace: raw Cobblemon apricorn -> cooked apricorn, the ingredient of every disc. */
    val COOKED_APRICORNS: Map<ApricornColor, Item> =
        ApricornColor.entries.associateWithTo(EnumMap(ApricornColor::class.java)) { register(it.cookedPath, Item(Item.Properties())) }

    /** Iron Disc (id inherited from the original Ballworks): 3 iron ingots in a row -> 5. */
    val IRON_DISC: Item = register("base_blank", Item(Item.Properties()))

    /** Iron Base: an iron disc hammered on the anvil. */
    val IRON_BASE: Item = register("pokeball_base", Item(Item.Properties()))

    val DISCS: Map<ForgeBall, Item> =
        ForgeBall.entries.associateWithTo(EnumMap(ForgeBall::class.java)) { register(it.discPath, Item(Item.Properties())) }

    val LIDS: Map<ForgeBall, Item> =
        ForgeBall.entries.associateWithTo(EnumMap(ForgeBall::class.java)) { register(it.lidPath, Item(Item.Properties())) }

    val HAMMERS: Map<HammerMaterial, HammerItem> =
        HammerMaterial.entries.associateWithTo(EnumMap(HammerMaterial::class.java)) { register(it.itemPath, HammerItem(it)) }

    fun hammer(material: HammerMaterial): HammerItem = HAMMERS.getValue(material)

    /** Every item of the mod, in creative tab order. */
    fun all(): List<Item> = buildList {
        addAll(HAMMERS.values)
        addAll(COOKED_APRICORNS.values)
        add(IRON_DISC)
        add(IRON_BASE)
        addAll(DISCS.values)
        addAll(LIDS.values)
    }

    private fun <T : Item> register(path: String, item: T): T =
        Registry.register(BuiltInRegistries.ITEM, CobbleBallworks.id(path), item)

    /** Forces the object (and so the registration above) to load. */
    fun init() = Unit
}
