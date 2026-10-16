package com.xaiver.cobbleballworks.menu

import com.xaiver.cobbleballworks.block.entity.MechanicalAnvilBlockEntity
import com.xaiver.cobbleballworks.recipe.AnvilRecipe
import com.xaiver.cobbleballworks.registry.ModMenus
import net.minecraft.util.Mth
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity

/**
 * The Mechanical Anvil screen is Pixelmon's: a furnace layout (input 56,17; fuel 56,53;
 * output 116,35) with the burn flame and the progress arrow.
 */
class MechanicalAnvilMenu(
    containerId: Int,
    playerInventory: Inventory,
    private val container: Container,
    private val data: ContainerData,
) : AbstractContainerMenu(ModMenus.MECHANICAL_ANVIL, containerId) {

    /** Client side: the server fills the slots and the data. */
    constructor(containerId: Int, playerInventory: Inventory) :
        this(containerId, playerInventory, SimpleContainer(MechanicalAnvilBlockEntity.SLOT_COUNT), SimpleContainerData(DATA_COUNT))

    private val level: Level = playerInventory.player.level()

    init {
        checkContainerSize(container, MechanicalAnvilBlockEntity.SLOT_COUNT)
        checkContainerDataCount(data, DATA_COUNT)
        addSlot(object : Slot(container, MechanicalAnvilBlockEntity.SLOT_INPUT, 56, 17) {
            override fun mayPlace(stack: ItemStack): Boolean = canForge(stack)
        })
        addSlot(object : Slot(container, MechanicalAnvilBlockEntity.SLOT_FUEL, 56, 53) {
            override fun mayPlace(stack: ItemStack): Boolean = isFuel(stack) || stack.`is`(Items.BUCKET)

            override fun getMaxStackSize(stack: ItemStack): Int = if (stack.`is`(Items.BUCKET)) 1 else super.getMaxStackSize(stack)
        })
        addSlot(object : Slot(container, MechanicalAnvilBlockEntity.SLOT_OUTPUT, 116, 35) {
            override fun mayPlace(stack: ItemStack): Boolean = false
        })
        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18))
            }
        }
        for (column in 0 until 9) {
            addSlot(Slot(playerInventory, column, 8 + column * 18, 142))
        }
        addDataSlots(data)
    }

    private fun canForge(stack: ItemStack): Boolean = AnvilRecipe.find(level, stack) != null

    private fun isFuel(stack: ItemStack): Boolean = AbstractFurnaceBlockEntity.isFuel(stack)

    override fun stillValid(player: Player): Boolean = container.stillValid(player)

    /** Shift click, as in a furnace: output -> inventory, pieces -> input, fuel -> fuel slot. */
    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots[index]
        if (!slot.hasItem()) return ItemStack.EMPTY
        val stack = slot.item
        val original = stack.copy()
        when {
            index == OUTPUT -> {
                if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, true)) return ItemStack.EMPTY
                slot.onQuickCraft(stack, original)
            }
            index == INPUT || index == FUEL -> if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, false)) return ItemStack.EMPTY
            canForge(stack) -> if (!moveItemStackTo(stack, INPUT, INPUT + 1, false)) return ItemStack.EMPTY
            isFuel(stack) -> if (!moveItemStackTo(stack, FUEL, FUEL + 1, false)) return ItemStack.EMPTY
            index < HOTBAR_START -> if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) return ItemStack.EMPTY
            !moveItemStackTo(stack, INVENTORY_START, HOTBAR_START, false) -> return ItemStack.EMPTY
        }
        if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
        if (stack.count == original.count) return ItemStack.EMPTY
        slot.onTake(player, stack)
        return original
    }

    val isLit: Boolean get() = data.get(0) > 0

    /** Fuel left, 0..1, for the flame. */
    val litProgress: Float
        get() {
            val duration = data.get(1).takeIf { it != 0 } ?: 200
            return Mth.clamp(data.get(0).toFloat() / duration, 0f, 1f)
        }

    /** Progress of the current piece, 0..1, for the arrow. */
    val forgeProgress: Float
        get() {
            val progress = data.get(2)
            val total = data.get(3)
            return if (total != 0 && progress != 0) Mth.clamp(progress.toFloat() / total, 0f, 1f) else 0f
        }

    companion object {
        const val DATA_COUNT = 4
        private const val INPUT = 0
        private const val FUEL = 1
        private const val OUTPUT = 2
        private const val INVENTORY_START = 3
        private const val HOTBAR_START = 30
        private const val HOTBAR_END = 39
    }
}
