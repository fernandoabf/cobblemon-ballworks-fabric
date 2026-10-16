package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister

object ModCreativeTabs {
    val TABS: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CobbleBallworks.MOD_ID)

    val BALLWORKS_TAB = TABS.register("ballworks") {
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cobbleballworks.ballworks"))
            .icon { ItemStack(ModItems.HAMMERS["iron"]!!.get()) }
            .displayItems { _, output ->
                // Workstation first
                output.accept(ModBlocks.BALL_ANVIL_ITEM.get())
                // Tiered hammers in material-progression order (matches HAMMER_DURABILITY map)
                val hammerOrder = listOf(
                    "wood", "stone", "iron", "gold", "aluminium", "diamond",
                    "amethyst", "ruby", "sapphire",
                    "dawn_stone", "dusk_stone", "fire_stone", "leaf_stone",
                    "moon_stone", "sun_stone", "thunder_stone", "water_stone",
                )
                hammerOrder.forEach { id ->
                    ModItems.HAMMERS[id]?.get()?.let(output::accept)
                }
                // Shared base chain
                output.accept(ModItems.BASE_BLANK.get())
                output.accept(ModItems.POKEBALL_BASE.get())
                // Industrial mold
                output.accept(ModItems.BALL_MOLD.get())
                // Per-ball lid blanks, then lids
                BallRegistry.ALL.forEach { id ->
                    ModItems.LID_BLANKS[id]?.get()?.let(output::accept)
                }
                BallRegistry.ALL.forEach { id ->
                    ModItems.LIDS[id]?.get()?.let(output::accept)
                }
            }
            .build()
    }

    fun register(bus: IEventBus) {
        TABS.register(bus)
    }
}
