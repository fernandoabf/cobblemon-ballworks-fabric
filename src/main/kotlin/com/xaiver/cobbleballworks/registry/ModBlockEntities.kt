package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType

object ModBlockEntities {
    // No data fixer type (null), like every modded block entity.
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    val BALL_ANVIL: BlockEntityType<BallAnvilBlockEntity> = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        CobbleBallworks.id("ball_anvil"),
        BlockEntityType.Builder.of(::BallAnvilBlockEntity, ModBlocks.BALL_ANVIL).build(null),
    )

    fun init() = Unit
}
