package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.item.BallHammerItem
import com.xaiver.cobbleballworks.item.BallMoldItem
import net.minecraft.world.item.Item
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object ModItems {
    val ITEMS: DeferredRegister<Item> =
        DeferredRegister.create(ForgeRegistries.ITEMS, CobbleBallworks.MOD_ID)

    private fun plainItem() = Item.Properties()

    // Shared parts
    val BASE_BLANK: RegistryObject<Item> =
        ITEMS.register("base_blank") { Item(plainItem()) }

    val POKEBALL_BASE: RegistryObject<Item> =
        ITEMS.register("pokeball_base") { Item(plainItem()) }

    // Reusable industrial mold — made at a Tinkers' Construct casting table (not
    // craftable), used as the "extra/mold" input on Mekanism: More Machine's
    // Stamping Factory. Stack size 1 so one mold occupies one slot cleanly; the
    // crafting-remainder override keeps it from being consumed by processors
    // that respect that hook.
    val BALL_MOLD: RegistryObject<Item> =
        ITEMS.register("ball_mold") { BallMoldItem(plainItem().stacksTo(1)) }

    // Tiered hammers. Wood < stone < iron < gold < diamond is the primary progression;
    // everything off that track (aluminium, gems, evolution stones) matches iron so the
    // primary path has clear stakes. Every entry is a BallHammerItem, so they all return
    // to the crafting grid with +1 damage and all pass the anvil's `is BallHammerItem`
    // check — tier only affects how many hammerings you get out of one.
    private const val IRON_DURABILITY = 250

    private val HAMMER_DURABILITY: Map<String, Int> = mapOf(
        "wood" to 60,
        "stone" to 132,
        "iron" to IRON_DURABILITY,
        "gold" to 500,
        "diamond" to 1024,
        "aluminium" to IRON_DURABILITY,
        "amethyst" to IRON_DURABILITY,
        "ruby" to IRON_DURABILITY,
        "sapphire" to IRON_DURABILITY,
        "dawn_stone" to IRON_DURABILITY,
        "dusk_stone" to IRON_DURABILITY,
        "fire_stone" to IRON_DURABILITY,
        "leaf_stone" to IRON_DURABILITY,
        "moon_stone" to IRON_DURABILITY,
        "sun_stone" to IRON_DURABILITY,
        "thunder_stone" to IRON_DURABILITY,
        "water_stone" to IRON_DURABILITY,
    )

    val HAMMERS: Map<String, RegistryObject<Item>> =
        HAMMER_DURABILITY.mapValues { (id, durability) ->
            ITEMS.register("${id}_hammer") { BallHammerItem(plainItem().durability(durability)) }
        }

    // Per-ball parts, driven by BallRegistry so adding a ball is a one-line change.
    val LID_BLANKS: Map<String, RegistryObject<Item>> =
        BallRegistry.ALL.associateWith { id ->
            ITEMS.register("${id}_lid_blank") { Item(plainItem()) }
        }

    val LIDS: Map<String, RegistryObject<Item>> =
        BallRegistry.ALL.associateWith { id ->
            ITEMS.register("${id}_lid") { Item(plainItem()) }
        }

    fun register(bus: IEventBus) {
        ITEMS.register(bus)
    }
}
