package com.xaiver.cobbleballworks.item

import com.xaiver.cobbleballworks.block.BallAnvilBlock
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.state.BlockState
import java.util.WeakHashMap
import kotlin.math.ceil

/**
 * Hammering: holding left click on the anvil with a hammer strikes it at the hammer's pace.
 *
 * Fabric's AttackBlockCallback answers SUCCESS for a hammer on the anvil on both sides: the client
 * swings the hammer without mining the block (no cracks) and, while the click is held, sends a
 * START_DESTROY_BLOCK every tick; the server counts strikes there and cancels vanilla's handling.
 * Completed breaks are never used: they go through ServerPlayerGameMode.destroyBlock and its break
 * events, where protection mods can swallow them.
 *
 * Since a START comes every tick, the server sets the pace: one strike per [ticksBetweenStrikes],
 * the time vanilla would take to mine the anvil plus the pause it puts between two broken blocks,
 * as hammering paced itself in Pixelmon. Efficiency and Haste speed strikes up, as they speed up
 * mining; a hammer that "instamines" the anvil, and creative mode, still wait
 * [MIN_TICKS_BETWEEN_STRIKES].
 *
 * Fabric fires the server callback before vanilla's own checks, so reach, spawn protection and
 * adventure mode are checked here.
 */
object AnvilStrikes {
    /** The pause vanilla puts between two broken blocks (MultiPlayerGameMode.destroyDelay). */
    const val MIN_TICKS_BETWEEN_STRIKES = 5

    /** Ticks a vanilla mining cycle spends besides mining: the pause plus the tick that starts it. */
    private const val CYCLE_OVERHEAD = MIN_TICKS_BETWEEN_STRIKES + 1

    private val lastStrikeTick = WeakHashMap<ServerPlayer, Long>()

    fun init() {
        AttackBlockCallback.EVENT.register { player, level, _, pos, _ ->
            val state = level.getBlockState(pos)
            if (player.isSpectator || state.block !is BallAnvilBlock || player.mainHandItem.item !is HammerItem) {
                return@register InteractionResult.PASS
            }
            if (player is ServerPlayer && !level.isClientSide) {
                val anvil = level.getBlockEntity(pos) as? BallAnvilBlockEntity
                if (anvil != null && mayHammer(player, pos)) strike(player, anvil, state, pos)
            }
            InteractionResult.SUCCESS
        }
    }

    fun strike(player: ServerPlayer, anvil: BallAnvilBlockEntity, state: BlockState, pos: BlockPos): BallAnvilBlockEntity.StrikeResult {
        val hammer = player.mainHandItem
        val item = hammer.item as? HammerItem ?: return BallAnvilBlockEntity.StrikeResult.NOTHING
        if (!intervalElapsed(player, state, pos)) return BallAnvilBlockEntity.StrikeResult.NOTHING
        return anvil.strike(player, hammer, item.strength)
    }

    /** Ticks between two strikes of a hammer that mines [perTick] of the anvil per tick. */
    fun ticksBetweenStrikes(perTick: Float): Int =
        if (perTick >= 1f) MIN_TICKS_BETWEEN_STRIKES else ceil(1.0 / perTick).toInt() + CYCLE_OVERHEAD

    /** What vanilla's handleBlockBreakAction checks before it lets a START through. */
    private fun mayHammer(player: ServerPlayer, pos: BlockPos): Boolean =
        player.canInteractWithBlock(pos, 1.0) &&
            player.serverLevel().mayInteract(player, pos) &&
            !player.blockActionRestricted(player.serverLevel(), pos, player.gameMode.gameModeForPlayer)

    private fun intervalElapsed(player: ServerPlayer, state: BlockState, pos: BlockPos): Boolean {
        val level = player.serverLevel()
        val now = level.gameTime
        val last = synchronized(lastStrikeTick) { lastStrikeTick[player] }
        val required =
            if (player.abilities.instabuild) MIN_TICKS_BETWEEN_STRIKES else ticksBetweenStrikes(state.getDestroyProgress(player, level, pos))
        if (last != null && now - last < required) return false
        synchronized(lastStrikeTick) { lastStrikeTick[player] = now }
        return true
    }
}
