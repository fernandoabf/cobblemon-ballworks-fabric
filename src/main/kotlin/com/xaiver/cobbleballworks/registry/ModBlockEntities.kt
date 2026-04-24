package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.block.BallAnvilBlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object ModBlockEntities {
    val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CobbleBallworks.MOD_ID)

    val BALL_ANVIL: RegistryObject<BlockEntityType<BallAnvilBlockEntity>> =
        BLOCK_ENTITIES.register("ball_anvil") {
            @Suppress("DEPRECATION")
            BlockEntityType.Builder.of(::BallAnvilBlockEntity, ModBlocks.BALL_ANVIL.get()).build(null)
        }

    fun register(bus: IEventBus) {
        BLOCK_ENTITIES.register(bus)
    }
}
