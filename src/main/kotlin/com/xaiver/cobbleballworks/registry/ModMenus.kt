package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.menu.MechanicalAnvilMenu
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType

object ModMenus {
    val MECHANICAL_ANVIL: MenuType<MechanicalAnvilMenu> = Registry.register(
        BuiltInRegistries.MENU,
        CobbleBallworks.id("mechanical_anvil"),
        MenuType(::MechanicalAnvilMenu, FeatureFlags.VANILLA_SET),
    )

    fun init() = Unit
}
