package com.xaiver.cobbleballworks.item

import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState
import java.util.WeakHashMap

/**
 * Turns a completed "break" of the anvil into a strike (server side).
 *
 * The vanilla client mines the anvil at the hammer's speed and sends STOP_DESTROY_BLOCK each time
 * the progress reaches 100%. The server checks the timing of the first break against the
 * START_DESTROY_BLOCK packet, but when the block survives the client keeps going without sending a
 * new START, and the server accepts every later STOP at once. So the time since the previous strike
 * is checked here, with the same 70% tolerance the server uses for the first one.
 *
 * Efficiency and Haste speed strikes up, as they speed up mining. Once a hammer "instamines" the
 * anvil (Efficiency V on a strength 4+ hammer) the client would strike every tick, because vanilla
 * only pauses 5 ticks between blocks that took time to break; strikes keep that pause here.
 */
object AnvilStrikes {
    private const val TOLERANCE = 0.7f

    /** The pause vanilla puts between two broken blocks (MultiPlayerGameMode.destroyDelay). */
    const val MIN_TICKS_BETWEEN_STRIKES = 5

    private val lastStrikeTick = WeakHashMap<ServerPlayer, Long>()

    fun strike(player: ServerPlayer, anvil: BallAnvilBlockEntity, state: BlockState, pos: BlockPos): BallAnvilBlockEntity.StrikeResult {
        val hammer = player.mainHandItem
        val item = hammer.item as? HammerItem ?: return BallAnvilBlockEntity.StrikeResult.NOTHING
        if (!player.abilities.instabuild && !intervalElapsed(player, state, pos)) return BallAnvilBlockEntity.StrikeResult.NOTHING
        return anvil.strike(player, hammer, item.strength)
    }

    private fun intervalElapsed(player: ServerPlayer, state: BlockState, pos: BlockPos): Boolean {
        val level = player.serverLevel()
        val now = level.gameTime
        val last = synchronized(lastStrikeTick) { lastStrikeTick[player] }
        val perTick = state.getDestroyProgress(player, level, pos)
        if (last != null && (now - last < MIN_TICKS_BETWEEN_STRIKES || (now - last + 1) * perTick < TOLERANCE)) return false
        synchronized(lastStrikeTick) { lastStrikeTick[player] = now }
        return true
    }
}
