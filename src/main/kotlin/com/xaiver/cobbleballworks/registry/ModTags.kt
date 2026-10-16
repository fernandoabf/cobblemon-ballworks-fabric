package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

object ModTags {
    object Items {
        val HAMMERS: TagKey<Item> = item("hammers")
        val COOKED_APRICORNS: TagKey<Item> = item("cooked_apricorns")
        val DISCS: TagKey<Item> = item("discs")
        val LIDS: TagKey<Item> = item("lids")

        private fun item(path: String): TagKey<Item> = TagKey.create(Registries.ITEM, CobbleBallworks.id(path))
    }

    object Blocks {
        /** Blocks a hammer works on; its mining speed there is the hammer strength. */
        val HAMMERABLE: TagKey<Block> = TagKey.create(Registries.BLOCK, CobbleBallworks.id("hammerable"))
    }
}
