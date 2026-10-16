package com.xaiver.cobbleballworks.block

import com.mojang.serialization.MapCodec
import com.xaiver.cobbleballworks.block.entity.MechanicalAnvilBlockEntity
import com.xaiver.cobbleballworks.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult

/**
 * Pixelmon's Mechanical Anvil: right click opens its furnace-like screen; it glows and smokes at
 * the front while it works; breaking it (pickaxe) drops its contents.
 */
class MechanicalAnvilBlock(properties: Properties) : BaseEntityBlock(properties) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = simpleCodec(::MechanicalAnvilBlock)

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, LIT)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)

    override fun rotate(state: BlockState, rotation: Rotation): BlockState =
        state.setValue(FACING, rotation.rotate(state.getValue(FACING)))

    override fun mirror(state: BlockState, mirror: Mirror): BlockState =
        state.rotate(mirror.getRotation(state.getValue(FACING)))

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = MechanicalAnvilBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? =
        if (level.isClientSide) null else createTickerHelper(type, ModBlockEntities.MECHANICAL_ANVIL, MechanicalAnvilBlockEntity::serverTick)

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide) {
            val anvil = level.getBlockEntity(pos) as? MechanicalAnvilBlockEntity
            if (anvil != null) player.openMenu(anvil)
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        Containers.dropContentsOnDestroy(state, newState, level, pos)
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    /** Smoke and flames at the front while lit, like a furnace (Pixelmon: SMOKE_NORMAL + FLAME). */
    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (!state.getValue(LIT)) return
        val x = pos.x + 0.5
        val y = pos.y.toDouble()
        val z = pos.z + 0.5
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0f, 1.0f, false)
        }
        val facing = state.getValue(FACING)
        val along = random.nextDouble() * 0.6 - 0.3
        val dx = if (facing.axis == Direction.Axis.X) facing.stepX * 0.52 else along
        val dy = random.nextDouble() * 6.0 / 16.0
        val dz = if (facing.axis == Direction.Axis.Z) facing.stepZ * 0.52 else along
        level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0)
        level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0)
    }

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val LIT = BlockStateProperties.LIT

        /**
         * Height of the platform the piece rests on, in pixels (models/block/mechanical_anvil.json): the
         * body is 8 px tall so the press, hanging from y 14, has a 5 px stroke down to the platform.
         */
        const val PLATFORM_Y = 9.0
    }
}
