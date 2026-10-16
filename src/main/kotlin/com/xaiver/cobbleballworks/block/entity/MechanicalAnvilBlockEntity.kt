package com.xaiver.cobbleballworks.block.entity

import com.xaiver.cobbleballworks.block.MechanicalAnvilBlock
import com.xaiver.cobbleballworks.config.BallworksConfig
import com.xaiver.cobbleballworks.menu.MechanicalAnvilMenu
import com.xaiver.cobbleballworks.recipe.AnvilRecipe
import com.xaiver.cobbleballworks.registry.ModBlockEntities
import com.xaiver.cobbleballworks.registry.ModRecipes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState

/**
 * The Mechanical Anvil, Pixelmon's automatic forge: a furnace that hammers. Slot 0 takes pieces
 * with an anvil recipe, slot 1 any furnace fuel (vanilla burn times, so one coal makes 8 discs),
 * slot 2 is the output. It only lights up when there is something to process and room for it,
 * then burns the fuel out like a furnace. A piece takes [AnvilRecipe.processingTime] ticks (200
 * for a disc) and needs no hammer.
 *
 * Every face accepts insertion, sorted by slot (pieces to the input, fuel to the fuel slot); only
 * the output (and an empty bucket left by lava) can be extracted. No redstone, as in Pixelmon.
 */
class MechanicalAnvilBlockEntity(pos: BlockPos, state: BlockState) :
    BaseContainerBlockEntity(ModBlockEntities.MECHANICAL_ANVIL, pos, state), WorldlyContainer {

    private var items: NonNullList<ItemStack> = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY)

    /** Ticks of fuel left, and how long the current fuel item lasts in total. */
    var litTime = 0
        private set
    var litDuration = 0
        private set

    /** Ticks spent on the current piece, and how many it needs. */
    var progress = 0
        private set
    var totalTime = AnvilRecipe.TICKS_PER_FORCE * 5
        private set

    private val quickCheck: RecipeManager.CachedCheck<SingleRecipeInput, AnvilRecipe> = RecipeManager.createCheck(ModRecipes.ANVIL)

    /** What the client last got for the renderer, to send updates only when the piece changes visibly. */
    private var syncedInput: ItemStack = ItemStack.EMPTY
    private var syncedStage = -1

    val isLit: Boolean get() = litTime > 0

    /** The piece being worked (client side too, for the renderer). */
    val input: ItemStack get() = items[SLOT_INPUT]

    /** 0..1 progress of the current piece (client side too). */
    val progressRatio: Float get() = if (totalTime <= 0) 0f else (progress.toFloat() / totalTime).coerceIn(0f, 1f)

    val dataAccess: ContainerData = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0 -> litTime
            1 -> litDuration
            2 -> progress
            3 -> totalTime
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> litTime = value
                1 -> litDuration = value
                2 -> progress = value
                3 -> totalTime = value
            }
        }

        override fun getCount(): Int = MechanicalAnvilMenu.DATA_COUNT
    }

    override fun getDefaultName(): Component = Component.translatable("block.cobbleballworks.mechanical_anvil")

    override fun getItems(): NonNullList<ItemStack> = items

    override fun setItems(items: NonNullList<ItemStack>) {
        this.items = items
    }

    override fun getContainerSize(): Int = SLOT_COUNT

    override fun createMenu(id: Int, inventory: Inventory): AbstractContainerMenu =
        MechanicalAnvilMenu(id, inventory, this, dataAccess)

    // ---- Automation (hoppers and pipes) ----

    override fun getSlotsForFace(side: Direction): IntArray = ALL_SLOTS

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, side: Direction?): Boolean = canPlaceItem(slot, stack)

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, side: Direction): Boolean =
        slot == SLOT_OUTPUT || (slot == SLOT_FUEL && stack.`is`(Items.BUCKET))

    override fun canPlaceItem(slot: Int, stack: ItemStack): Boolean = when (slot) {
        SLOT_OUTPUT -> false
        SLOT_FUEL -> AbstractFurnaceBlockEntity.isFuel(stack) || (stack.`is`(Items.BUCKET) && !items[SLOT_FUEL].`is`(Items.BUCKET))
        else -> level?.let { AnvilRecipe.find(it, stack) } != null
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        val previous = items[slot]
        val samePiece = !stack.isEmpty && ItemStack.isSameItemSameComponents(previous, stack)
        items[slot] = stack
        stack.limitSize(getMaxStackSize(stack))
        if (slot == SLOT_INPUT && !samePiece) {
            totalTime = processingTime()
            progress = 0
            setChanged()
        }
    }

    // ---- Forging ----

    private fun recipe(): RecipeHolder<AnvilRecipe>? {
        val level = level ?: return null
        val piece = items[SLOT_INPUT]
        if (piece.isEmpty) return null
        return quickCheck.getRecipeFor(SingleRecipeInput(piece), level).orElse(null)
    }

    private fun processingTime(): Int = recipe()?.value()?.processingTime ?: AnvilRecipe.TICKS_PER_FORCE * 5

    /** Whether the piece in the input can be forged into the output slot. */
    private fun canForge(recipe: RecipeHolder<AnvilRecipe>?): Boolean {
        if (recipe == null || items[SLOT_INPUT].isEmpty) return false
        val result = recipe.value().getResultItem(level!!.registryAccess())
        if (result.isEmpty) return false
        val output = items[SLOT_OUTPUT]
        if (output.isEmpty) return true
        if (!ItemStack.isSameItemSameComponents(output, result)) return false
        return output.count + result.count <= minOf(maxStackSize, output.maxStackSize)
    }

    private fun forge(recipe: RecipeHolder<AnvilRecipe>) {
        val result = recipe.value().assemble(SingleRecipeInput(items[SLOT_INPUT]), level!!.registryAccess())
        val output = items[SLOT_OUTPUT]
        if (output.isEmpty) items[SLOT_OUTPUT] = result else output.grow(result.count)
        items[SLOT_INPUT].shrink(1)
        if (BallworksConfig.values.mechanicalAnvilSound) {
            level!!.playSound(null, worldPosition, SoundEvents.IRON_GOLEM_DEATH, SoundSource.BLOCKS, 0.7f, 1.0f)
        }
    }

    private fun burnDuration(fuel: ItemStack): Int =
        if (fuel.isEmpty) 0 else AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuel.item, 0)

    /** One server tick: AbstractFurnaceBlockEntity.serverTick with anvil recipes and no XP. */
    fun serverTick(level: Level, pos: BlockPos, blockState: BlockState) {
        var state = blockState
        val wasLit = isLit
        var changed = false
        if (isLit) litTime--

        val fuel = items[SLOT_FUEL]
        val hasInput = !items[SLOT_INPUT].isEmpty
        val hasFuel = !fuel.isEmpty
        if (isLit || (hasFuel && hasInput)) {
            val recipe = if (hasInput) recipe() else null
            if (!isLit && canForge(recipe)) {
                litTime = burnDuration(fuel)
                litDuration = litTime
                if (isLit) {
                    changed = true
                    if (hasFuel) {
                        val item = fuel.item
                        fuel.shrink(1)
                        if (fuel.isEmpty) items[SLOT_FUEL] = item.craftingRemainingItem?.let(::ItemStack) ?: ItemStack.EMPTY
                    }
                }
            }
            if (isLit && canForge(recipe)) {
                progress++
                if (progress >= totalTime) {
                    progress = 0
                    forge(recipe!!)
                    totalTime = processingTime()
                    changed = true
                }
            } else {
                progress = 0
            }
        } else if (!isLit && progress > 0) {
            progress = (progress - 2).coerceIn(0, totalTime)
        }

        if (wasLit != isLit) {
            changed = true
            state = state.setValue(MechanicalAnvilBlock.LIT, isLit)
            level.setBlock(pos, state, Block.UPDATE_ALL)
        }
        if (changed) setChanged(level, pos, state)
        syncForRenderer(level, pos, state)
    }

    /** The renderer shows the piece on top and its stage: update clients only when that changes. */
    private fun syncForRenderer(level: Level, pos: BlockPos, state: BlockState) {
        val stage = if (items[SLOT_INPUT].isEmpty) -1 else (progressRatio * 3).toInt().coerceIn(0, 2)
        if (stage == syncedStage && ItemStack.isSameItemSameComponents(syncedInput, items[SLOT_INPUT])) return
        syncedStage = stage
        syncedInput = items[SLOT_INPUT].copyWithCount(1)
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)
    }

    // ---- Persistence and sync ----

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(tag, items, registries)
        litTime = tag.getInt(TAG_LIT_TIME)
        litDuration = tag.getInt(TAG_LIT_DURATION)
        progress = tag.getInt(TAG_PROGRESS)
        totalTime = tag.getInt(TAG_TOTAL_TIME).takeIf { it > 0 } ?: AnvilRecipe.TICKS_PER_FORCE * 5
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        ContainerHelper.saveAllItems(tag, items, registries)
        tag.putInt(TAG_LIT_TIME, litTime)
        tag.putInt(TAG_LIT_DURATION, litDuration)
        tag.putInt(TAG_PROGRESS, progress)
        tag.putInt(TAG_TOTAL_TIME, totalTime)
    }

    /** Clients only need the piece and the progress, for the renderer. */
    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        val tag = CompoundTag()
        val piece = items[SLOT_INPUT]
        val visible = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY)
        if (!piece.isEmpty) visible[SLOT_INPUT] = piece.copyWithCount(1)
        ContainerHelper.saveAllItems(tag, visible, true, registries)
        tag.putInt(TAG_PROGRESS, progress)
        tag.putInt(TAG_TOTAL_TIME, totalTime)
        return tag
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    companion object {
        const val SLOT_INPUT = 0
        const val SLOT_FUEL = 1
        const val SLOT_OUTPUT = 2
        const val SLOT_COUNT = 3
        private val ALL_SLOTS = intArrayOf(SLOT_INPUT, SLOT_FUEL, SLOT_OUTPUT)

        private const val TAG_LIT_TIME = "BurnTime"
        private const val TAG_LIT_DURATION = "BurnDuration"
        private const val TAG_PROGRESS = "ForgeTime"
        private const val TAG_TOTAL_TIME = "ForgeTimeTotal"

        @JvmStatic
        fun serverTick(level: Level, pos: BlockPos, state: BlockState, anvil: MechanicalAnvilBlockEntity) =
            anvil.serverTick(level, pos, state)
    }
}
