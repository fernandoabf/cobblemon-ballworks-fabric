package com.xaiver.cobbleballworks.block

import com.xaiver.cobbleballworks.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Connection
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

/**
 * Holds exactly one blank (or one finished item) plus a small hammering counter.
 * Progress and the stored stack are saved to NBT and replicated to the client via the
 * standard block-entity update packet.
 *
 * Interaction-facing helpers:
 *   [canInsertWorkpiece] / [insertWorkpiece] — loading a blank
 *   [canHammer]          / [applyHammer]     — a hammer strike (advances progress, may convert)
 *   [takeWorkpiece]                          — pulling the current item back out
 */
class BallAnvilBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.BALL_ANVIL.get(), pos, state) {

    var storedItem: ItemStack = ItemStack.EMPTY
        private set

    var progress: Int = 0
        private set

    /** True once [storedItem] is a finished part (no recipe remains for it). */
    val isFinished: Boolean
        get() = !storedItem.isEmpty && AnvilRecipes.forInput(storedItem) == null

    // Client-safe, read-only accessors consumed by the BlockEntityRenderer.
    // The renderer calls these every frame; none of them mutate state, and all of them
    // are defined in terms of [storedItem]/[progress] which are synced via the standard
    // block-entity update packet.

    /** Total hammer hits needed for the current blank, or 0 if nothing hammerable is loaded. */
    val requiredHits: Int
        get() = AnvilRecipes.forInput(storedItem)?.hits ?: 0

    /**
     * 0.0 when the anvil is empty or just-loaded, 1.0 when the blank is fully shaped
     * (i.e. [storedItem] is already the output). Monotonic; suitable for driving
     * rendering scale / lift per hammer strike.
     */
    val progressRatio: Float
        get() {
            if (storedItem.isEmpty) return 0f
            val hits = requiredHits
            if (hits <= 0) return 1f   // already a finished part
            return (progress.toFloat() / hits.toFloat()).coerceIn(0f, 1f)
        }

    // -------- Insertion --------

    fun canInsertWorkpiece(stack: ItemStack): Boolean =
        storedItem.isEmpty && AnvilRecipes.isSupportedBlank(stack)

    /**
     * Stores a single-count copy of [stack] on the anvil. Returns true on success.
     * The caller is responsible for shrinking the player's stack.
     */
    fun insertWorkpiece(stack: ItemStack): Boolean {
        if (!canInsertWorkpiece(stack)) return false
        storedItem = stack.copy().apply { count = 1 }
        progress = 0
        sync()
        return true
    }

    // -------- Hammering --------

    fun canHammer(): Boolean =
        AnvilRecipes.forInput(storedItem) != null

    /**
     * Performs one hammer strike: advances progress, damages [hammerStack] by 1, plays an
     * anvil sound, sends the progress actionbar to [player], and if the recipe is complete
     * replaces the stored blank with the formed part. Returns true if a hit was processed,
     * false if the anvil had nothing hammerable stored.
     *
     * Must be called server-side; the sound and state sync are intended for server use.
     */
    fun applyHammer(player: Player, hand: InteractionHand, hammerStack: ItemStack): Boolean {
        val recipe = AnvilRecipes.forInput(storedItem) ?: return false
        progress += 1
        val completed = progress >= recipe.hits
        val completedRecipe = if (completed) recipe else null
        if (completed) {
            storedItem = recipe.outputStack()
            progress = 0
        }
        sync()

        // Damage the hammer. Canonical Forge 1.20.1 call.
        hammerStack.hurtAndBreak(1, player) { p ->
            p.broadcastBreakEvent(hand)
        }

        // Force inventory sync so the client's durability bar reflects the hit this
        // tick instead of next.
        if (player is ServerPlayer) {
            player.inventoryMenu.broadcastChanges()
        }

        level?.playSound(
            null,
            worldPosition,
            SoundEvents.ANVIL_USE,
            SoundSource.BLOCKS,
            0.5f,
            1.2f
        )

        val msg = if (completedRecipe != null) {
            Component.translatable(
                "block.cobbleballworks.ball_anvil.finished",
                completedRecipe.outputStack().hoverName
            )
        } else {
            Component.translatable(
                "block.cobbleballworks.ball_anvil.progress",
                progress,
                recipe.hits
            )
        }
        player.displayClientMessage(msg, true)
        return true
    }

    // -------- Retrieval --------

    /** Pulls whatever is on the anvil and resets state. Caller hands it to the player. */
    fun takeWorkpiece(): ItemStack {
        val out = storedItem
        storedItem = ItemStack.EMPTY
        progress = 0
        sync()
        return out
    }

    // -------- NBT + sync --------

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        if (!storedItem.isEmpty) {
            tag.put(TAG_ITEM, storedItem.save(CompoundTag()))
        }
        tag.putInt(TAG_PROGRESS, progress)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        storedItem = if (tag.contains(TAG_ITEM)) ItemStack.of(tag.getCompound(TAG_ITEM)) else ItemStack.EMPTY
        progress = tag.getInt(TAG_PROGRESS)
    }

    override fun getUpdateTag(): CompoundTag = saveWithoutMetadata()

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? =
        ClientboundBlockEntityDataPacket.create(this)

    override fun onDataPacket(net: Connection, pkt: ClientboundBlockEntityDataPacket) {
        pkt.tag?.let { load(it) }
    }

    private fun sync() {
        setChanged()
        val lvl = level ?: return
        if (!lvl.isClientSide) {
            lvl.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
        }
    }

    companion object {
        private const val TAG_ITEM = "StoredItem"
        private const val TAG_PROGRESS = "Progress"
    }
}
