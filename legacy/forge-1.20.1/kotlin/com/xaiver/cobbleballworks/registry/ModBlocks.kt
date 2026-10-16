package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.block.BallAnvilBlock
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object ModBlocks {
    val BLOCKS: DeferredRegister<Block> =
        DeferredRegister.create(ForgeRegistries.BLOCKS, CobbleBallworks.MOD_ID)

    val BALL_ANVIL: RegistryObject<Block> = BLOCKS.register("ball_anvil") {
        BallAnvilBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.ANVIL)
                .strength(3.5f, 1200.0f)
                .requiresCorrectToolForDrops()
                // Non-full-cube geometry (base/waist/top/horn with internal gaps).
                // Without this, MC treats the block as a full opaque cube for occlusion
                // and culls neighbor faces against it, leaving see-through holes where
                // our model has gaps.
                .noOcclusion()
        )
    }

    // Block item registered into the main ITEMS register so every item ships from one place.
    val BALL_ANVIL_ITEM: RegistryObject<Item> = ModItems.ITEMS.register("ball_anvil") {
        BlockItem(BALL_ANVIL.get(), Item.Properties())
    }

    fun register(bus: IEventBus) {
        BLOCKS.register(bus)
    }
}
