package com.xaiver.cobbleballworks.block.entity

import com.xaiver.cobbleballworks.config.BallworksConfig
import com.xaiver.cobbleballworks.content.AnvilRules
import com.xaiver.cobbleballworks.recipe.AnvilRecipe
import com.xaiver.cobbleballworks.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

/**
 * The Poké Ball anvil: holds one piece and the hammering progress, following Pixelmon's
 * TileEntityAnvil (see [AnvilRules]). [item] and [stage] are synced to clients for the renderer;
 * the accumulated [force] is saved but only matters on the server.
 */
class BallAnvilBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.BALL_ANVIL, pos, state) {

    /** The piece on the anvil (count 1) or empty. */
    var item: ItemStack = ItemStack.EMPTY
        private set

    /** 0 = flat, 1, 2; the third stage turns the piece into its result. */
    var stage: Int = 0
        private set

    /** Strike force accumulated towards the next stage. */
    var force: Int = 0
        private set

    val isEmpty: Boolean get() = item.isEmpty

    /** True when the piece on the anvil has no anvil recipe left: it is done (a lid or a base). */
    fun isFinished(): Boolean = !item.isEmpty && level?.let { AnvilRecipe.find(it, item) } == null

    /** Only pieces with an anvil recipe (discs) go on an empty anvil, as in Pixelmon. */
    fun canPlace(stack: ItemStack): Boolean {
        val level = level ?: return false
        return item.isEmpty && AnvilRecipe.find(level, stack) != null
    }

    /** Puts one of [stack] on the anvil; the caller removes it from wherever it came from. */
    fun place(stack: ItemStack, pitch: Float = 1.0f) {
        item = stack.copyWithCount(1)
        stage = 0
        force = 0
        sync()
        playSound(SoundEvents.ANVIL_LAND, 0.15f, pitch)
    }

    /** Removes the piece (progress is lost) and returns it. */
    fun take(): ItemStack {
        val out = item
        item = ItemStack.EMPTY
        stage = 0
        force = 0
        sync()
        return out
    }

    /**
     * One hammer strike of [strength], server side. Pixelmon's rule: the force adds up and, when it
     * reaches the recipe's force per stage, the excess is dropped and the piece advances a stage,
     * wearing the hammer; at stage 3 it becomes the result. A finished piece is knocked off into the
     * player's inventory once the strikes add up to [AnvilRules.COLLECT_FORCE], for free.
     */
    fun strike(player: Player, hammer: ItemStack, strength: Int): StrikeResult {
        val level = level ?: return StrikeResult.NOTHING
        if (level.isClientSide || item.isEmpty) return StrikeResult.NOTHING
        val recipe = AnvilRecipe.find(level, item)?.value()

        force += strength
        if (recipe == null) {
            if (force < AnvilRules.COLLECT_FORCE) return hit()
            val done = take()
            val finished = done.item // placeItemBackInInventory empties the stack it is given
            playSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 0.1f, 3.0f)
            player.inventory.placeItemBackInInventory(done)
            if (BallworksConfig.values.allowAnvilAutoreloading) autoReload(player, finished)
            return StrikeResult.COLLECTED
        }
        if (force < recipe.force) return hit()

        force = 0
        stage++
        playSound(SoundEvents.ANVIL_LAND, 0.15f, 1.0f)
        val wear = BallworksConfig.values.hammerDamagePerStage
        if (wear > 0) hammer.hurtAndBreak(wear, player, EquipmentSlot.MAINHAND)
        if (stage < AnvilRules.STAGES) {
            sync()
            return StrikeResult.STAGE
        }
        item = recipe.assemble(SingleRecipeInput(item), level.registryAccess()).copyWithCount(1)
        stage = 0
        sync()
        playSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 0.1f, 3.0f)
        return StrikeResult.SHAPED
    }

    /** A strike that only added force: nothing visible changed, so no client update is needed. */
    private fun hit(): StrikeResult {
        setChanged()
        return StrikeResult.HIT
    }

    /** Pixelmon's allowAnvilAutoreloading: the next piece that forges into [finished], main inventory first. */
    private fun autoReload(player: Player, finished: Item) {
        val level = level ?: return
        val inventory = player.inventory
        val slots = inventory.items + inventory.offhand
        val next = slots.firstOrNull { stack ->
            AnvilRecipe.find(level, stack)?.value()?.result?.item == finished
        } ?: return
        place(next, pitch = 0.9f)
        next.consume(1, player)
    }

    private fun playSound(sound: SoundEvent, volume: Float, pitch: Float) {
        level?.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, pitch)
    }

    private fun sync() {
        setChanged()
        val level = level ?: return
        if (!level.isClientSide) level.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_CLIENTS)
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        if (!item.isEmpty) tag.put(TAG_ITEM, item.save(registries))
        tag.putInt(TAG_STAGE, stage)
        tag.putInt(TAG_FORCE, force)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        item = if (tag.contains(TAG_ITEM)) ItemStack.parseOptional(registries, tag.getCompound(TAG_ITEM)) else ItemStack.EMPTY
        stage = tag.getInt(TAG_STAGE).coerceIn(0, AnvilRules.STAGES - 1)
        force = tag.getInt(TAG_FORCE).coerceAtLeast(0)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag = saveCustomOnly(registries)

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    enum class StrikeResult { NOTHING, HIT, STAGE, SHAPED, COLLECTED }

    private companion object {
        const val TAG_ITEM = "Item"
        const val TAG_STAGE = "Stage"
        const val TAG_FORCE = "Force"
    }
}
