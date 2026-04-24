package com.xaiver.cobbleballworks.block

import com.xaiver.cobbleballworks.item.BallHammerItem
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.BlockHitResult

/**
 * Right-click interaction dispatcher for the Ball Anvil. All state lives on
 * [BallAnvilBlockEntity]; this class only decides which helper to call and takes care of
 * side effects that need the player's inventory or the block-break path.
 *
 * `use` is the 1.20.1 six-arg signature. [FACING] tracks horizontal placement so the
 * imported Pixelmon-style model renders toward the player — matches the four-variant
 * blockstate shipped in `assets/cobbleballworks/blockstates/ball_anvil.json`.
 */
class BallAnvilBlock(properties: BlockBehaviour.Properties) : BaseEntityBlock(properties) {

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun rotate(state: BlockState, rotation: Rotation): BlockState =
        state.setValue(FACING, rotation.rotate(state.getValue(FACING)))

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        // Inlined instead of calling `state.rotate(...)` — that BlockState helper is
        // deprecated as an internal shortcut; this form does the same work without
        // touching the deprecated API.
        val derivedRotation = mirror.getRotation(state.getValue(FACING))
        return state.setValue(FACING, derivedRotation.rotate(state.getValue(FACING)))
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        BallAnvilBlockEntity(pos, state)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        val be = level.getBlockEntity(pos) as? BallAnvilBlockEntity
            ?: return InteractionResult.PASS
        val held = player.getItemInHand(hand)

        // 1) Holding the hammer -> try to advance progress on whatever is loaded.
        if (held.item is BallHammerItem) {
            if (!be.canHammer()) {
                return InteractionResult.PASS
            }
            if (!level.isClientSide) {
                be.applyHammer(player, hand, held)
            }
            return InteractionResult.sidedSuccess(level.isClientSide)
        }

        // 2) Holding a supported blank on an empty anvil -> load it.
        if (be.canInsertWorkpiece(held)) {
            if (!level.isClientSide) {
                if (be.insertWorkpiece(held) && !player.abilities.instabuild) {
                    held.shrink(1)
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide)
        }

        // 3) Empty hand + something stored -> take it out.
        if (held.isEmpty && !be.storedItem.isEmpty) {
            val canTake = be.isFinished || player.isShiftKeyDown
            if (canTake) {
                if (!level.isClientSide) {
                    val taken = be.takeWorkpiece()
                    if (!taken.isEmpty && !player.inventory.add(taken)) {
                        player.drop(taken, false)
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide)
            }
        }

        return InteractionResult.PASS
    }

    // `BlockBehaviour.onRemove` is marked @Deprecated by Mojang as an internal-use
    // override hook — there is no non-deprecated replacement in Forge 1.20.1, and every
    // block-entity-bearing block has to override it to drop its contents on break. The
    // suppression silences the Kotlin warning on both the override and the required
    // `super.onRemove(...)` call inside.
    @Suppress("DEPRECATION")
    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        if (!state.`is`(newState.block)) {
            val be = level.getBlockEntity(pos)
            if (be is BallAnvilBlockEntity && !be.storedItem.isEmpty) {
                Containers.dropItemStack(
                    level,
                    pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                    be.storedItem
                )
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    companion object {
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
    }
}
