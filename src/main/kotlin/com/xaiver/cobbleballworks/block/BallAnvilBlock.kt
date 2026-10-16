package com.xaiver.cobbleballworks.block

import com.mojang.serialization.MapCodec
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * The Poké Ball anvil (Pixelmon's "Anvil"): right click with a disc puts it on top, right click
 * with the main hand takes the piece back, and holding left click with a hammer hammers it (see
 * HammerItem.canAttackBlock). Any other tool, or the bare hand, breaks it and drops the piece.
 */
class BallAnvilBlock(properties: Properties) : BaseEntityBlock(properties) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = simpleCodec(::BallAnvilBlock)

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)

    override fun rotate(state: BlockState, rotation: Rotation): BlockState =
        state.setValue(FACING, rotation.rotate(state.getValue(FACING)))

    override fun mirror(state: BlockState, mirror: Mirror): BlockState =
        state.rotate(mirror.getRotation(state.getValue(FACING)))

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        if (state.getValue(FACING).axis == Direction.Axis.Z) SHAPE_LONG_X else SHAPE_LONG_Z

    override fun isPathfindable(state: BlockState, type: PathComputationType): Boolean = false

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = BallAnvilBlockEntity(pos, state)

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): ItemInteractionResult {
        val anvil = level.getBlockEntity(pos) as? BallAnvilBlockEntity
            ?: return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        if (!anvil.isEmpty) {
            // Any right click with the main hand takes the piece back, whatever is held (no swap).
            if (hand != InteractionHand.MAIN_HAND) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
            if (!level.isClientSide) {
                player.inventory.placeItemBackInInventory(anvil.take())
                level.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.1f, 3.0f)
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide)
        }
        if (!anvil.canPlace(stack)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        if (!level.isClientSide) {
            anvil.place(stack)
            stack.consume(1, player)
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!state.`is`(newState.block)) {
            val anvil = level.getBlockEntity(pos) as? BallAnvilBlockEntity
            if (anvil != null && !anvil.isEmpty) popResource(level, pos, anvil.take())
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING

        /** Facing north/south: horn and heel along X. Matches models/block/ball_anvil.json. */
        private val SHAPE_LONG_X: VoxelShape = Shapes.or(box(3.0, 0.0, 4.0, 13.0, 7.0, 12.0), box(0.0, 7.0, 4.5, 16.0, 11.0, 11.5))
        private val SHAPE_LONG_Z: VoxelShape = Shapes.or(box(4.0, 0.0, 3.0, 12.0, 7.0, 13.0), box(4.5, 7.0, 0.0, 11.5, 11.0, 16.0))

        /** Height of the anvil's working face, in pixels; the renderer puts the piece there. */
        const val TOP_Y = 11.0
    }
}
