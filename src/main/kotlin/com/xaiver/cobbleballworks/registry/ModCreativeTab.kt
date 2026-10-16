package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.content.HammerMaterial
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack

object ModCreativeTab {
    val BALLWORKS: CreativeModeTab = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        CobbleBallworks.id("ballworks"),
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.cobbleballworks.ballworks"))
            .icon { ItemStack(ModItems.hammer(HammerMaterial.IRON)) }
            .displayItems { _, output ->
                ModBlocks.items().forEach(output::accept)
                ModItems.all().forEach(output::accept)
            }
            .build(),
    )

    fun init() = Unit
}
