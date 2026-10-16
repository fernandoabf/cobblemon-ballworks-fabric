package com.xaiver.cobbleballworks.gametest

import com.xaiver.cobbleballworks.block.MechanicalAnvilBlock
import com.xaiver.cobbleballworks.block.entity.MechanicalAnvilBlockEntity
import com.xaiver.cobbleballworks.content.ForgeBall
import com.xaiver.cobbleballworks.registry.ModBlocks
import com.xaiver.cobbleballworks.registry.ModItems
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HopperBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.HopperBlockEntity

/** Section 6 of docs/PLANO.md: the Mechanical Anvil, a furnace that hammers. */
class MechanicalAnvilGameTest : FabricGameTest {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun oneCoalForgesEightDiscs(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeMechanicalAnvil(helper)
        val disc = ModItems.DISCS.getValue(ForgeBall.POKE)
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_INPUT, ItemStack(disc, 16))
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_FUEL, ItemStack(Items.COAL))
        tick(helper, pos, anvil, 1)
        checks.check(anvil.isLit && helper.getBlockState(pos).getValue(MechanicalAnvilBlock.LIT)) { "the anvil should light up" }
        tick(helper, pos, anvil, 1700)
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_OUTPUT).let { it.`is`(ModItems.LIDS.getValue(ForgeBall.POKE)) && it.count == 8 }) {
            "one coal (1600 ticks) should forge 8 discs at 200 ticks each: ${anvil.getItem(MechanicalAnvilBlockEntity.SLOT_OUTPUT)}"
        }
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_INPUT).count == 8) { "8 discs should be left" }
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_FUEL).isEmpty && !anvil.isLit) { "the coal should be burnt out" }
        checks.check(!helper.getBlockState(pos).getValue(MechanicalAnvilBlock.LIT)) { "the block should stop glowing" }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun noFuelOrNoRoomMeansNoWork(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeMechanicalAnvil(helper)
        val disc = ModItems.DISCS.getValue(ForgeBall.GREAT)
        val lid = ModItems.LIDS.getValue(ForgeBall.GREAT)
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_INPUT, ItemStack(disc, 4))
        tick(helper, pos, anvil, 300)
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_OUTPUT).isEmpty && anvil.progress == 0) { "no fuel should mean no progress" }

        // Output full: the fuel stays untouched and nothing lights up.
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_OUTPUT, ItemStack(lid, 64))
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_FUEL, ItemStack(Items.COAL))
        tick(helper, pos, anvil, 300)
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_FUEL).count == 1 && !anvil.isLit) { "a full output should not burn fuel" }

        // Something that is not a piece does nothing either.
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_OUTPUT, ItemStack.EMPTY)
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_INPUT, ItemStack(Items.IRON_INGOT))
        tick(helper, pos, anvil, 300)
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_FUEL).count == 1) { "an iron ingot should not start the anvil" }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun lavaBucketLeavesTheBucketAndStrongerBallsTakeLonger(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeMechanicalAnvil(helper)
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_INPUT, ItemStack(ModItems.DISCS.getValue(ForgeBall.MASTER), 2))
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_FUEL, ItemStack(Items.LAVA_BUCKET))
        tick(helper, pos, anvil, 1)
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_FUEL).`is`(Items.BUCKET)) { "lava should leave its bucket behind" }
        // Master disc: force 15 -> 600 ticks.
        checks.check(anvil.totalTime == 600) { "a Master disc should take 600 ticks, takes ${anvil.totalTime}" }
        tick(helper, pos, anvil, 598)
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_OUTPUT).isEmpty) { "a Master disc finished too early" }
        tick(helper, pos, anvil, 2)
        checks.check(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_OUTPUT).`is`(ModItems.LIDS.getValue(ForgeBall.MASTER))) { "the Master lid should be done after 600 ticks" }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun automationSortsBySlot(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (_, anvil) = placeMechanicalAnvil(helper)
        val disc = ItemStack(ModItems.IRON_DISC)
        val coal = ItemStack(Items.COAL)
        for (side in Direction.entries) {
            checks.check(anvil.getSlotsForFace(side).toSet() == setOf(0, 1, 2)) { "every face should expose the 3 slots ($side)" }
        }
        checks.check(anvil.canPlaceItemThroughFace(MechanicalAnvilBlockEntity.SLOT_INPUT, disc, Direction.NORTH)) { "discs go in the input" }
        checks.check(!anvil.canPlaceItemThroughFace(MechanicalAnvilBlockEntity.SLOT_INPUT, coal, Direction.NORTH)) { "coal does not go in the input" }
        checks.check(anvil.canPlaceItemThroughFace(MechanicalAnvilBlockEntity.SLOT_FUEL, coal, Direction.UP)) { "coal goes in the fuel slot" }
        checks.check(!anvil.canPlaceItemThroughFace(MechanicalAnvilBlockEntity.SLOT_FUEL, disc, Direction.UP)) { "discs do not go in the fuel slot" }
        checks.check(!anvil.canPlaceItemThroughFace(MechanicalAnvilBlockEntity.SLOT_OUTPUT, ItemStack(ModItems.IRON_BASE), Direction.UP)) {
            "nothing goes in the output"
        }
        checks.check(!anvil.canTakeItemThroughFace(MechanicalAnvilBlockEntity.SLOT_INPUT, disc, Direction.DOWN)) { "the input cannot be pulled" }
        checks.check(!anvil.canTakeItemThroughFace(MechanicalAnvilBlockEntity.SLOT_FUEL, coal, Direction.DOWN)) { "fuel cannot be pulled" }
        checks.check(anvil.canTakeItemThroughFace(MechanicalAnvilBlockEntity.SLOT_FUEL, ItemStack(Items.BUCKET), Direction.DOWN)) {
            "the empty lava bucket can be pulled"
        }
        checks.check(anvil.canTakeItemThroughFace(MechanicalAnvilBlockEntity.SLOT_OUTPUT, ItemStack(ModItems.IRON_BASE), Direction.DOWN)) {
            "the output can be pulled"
        }
        checks.finish()
    }

    /** Real hoppers, real time: one feeds from the top, one from the side, one drains from below. */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 400)
    fun hoppersFeedAndDrain(helper: GameTestHelper) {
        val pos = BlockPos(3, 2, 3)
        helper.setBlock(pos, ModBlocks.MECHANICAL_ANVIL.defaultBlockState())
        helper.setBlock(pos.above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN))
        helper.setBlock(pos.west(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST))
        helper.setBlock(pos.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN))
        helper.setBlock(pos.below(2), Blocks.CHEST)
        helper.getBlockEntity<HopperBlockEntity>(pos.above()).setItem(0, ItemStack(ModItems.DISCS.getValue(ForgeBall.POKE), 3))
        helper.getBlockEntity<HopperBlockEntity>(pos.west()).setItem(0, ItemStack(Items.COAL, 1))
        helper.succeedWhen {
            val chest = helper.getBlockEntity<BlockEntity>(pos.below(2)) as net.minecraft.world.Container
            val lids = (0 until chest.containerSize).sumOf { chest.getItem(it).let { s -> if (s.`is`(ModItems.LIDS.getValue(ForgeBall.POKE))) s.count else 0 } }
            val anvil = helper.getBlockEntity<MechanicalAnvilBlockEntity>(pos)
            helper.assertTrue(lids >= 1, "no lid has reached the chest yet")
            helper.assertTrue((0 until chest.containerSize).none { chest.getItem(it).`is`(Items.COAL) || chest.getItem(it).`is`(ModItems.DISCS.getValue(ForgeBall.POKE)) },
                "the bottom hopper pulled fuel or discs out of the anvil")
            helper.assertTrue(anvil.getItem(MechanicalAnvilBlockEntity.SLOT_FUEL).isEmpty || anvil.isLit, "the side hopper should have fed the coal")
        }
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun breakingDropsTheContentsAndNbtRoundTrips(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeMechanicalAnvil(helper)
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_INPUT, ItemStack(ModItems.DISCS.getValue(ForgeBall.LOVE), 5))
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_FUEL, ItemStack(Items.CHARCOAL, 2))
        tick(helper, pos, anvil, 50)
        val registries = helper.level.registryAccess()
        val copy = BlockEntity.loadStatic(helper.absolutePos(pos), helper.getBlockState(pos), anvil.saveWithFullMetadata(registries), registries)
            as? MechanicalAnvilBlockEntity
        checks.check(copy != null && copy.getItem(0).count == 5 && copy.getItem(1).count == 1 && copy.progress == anvil.progress && copy.litTime == anvil.litTime) {
            "after save/load: ${copy?.getItem(0)} ${copy?.getItem(1)} progress ${copy?.progress} lit ${copy?.litTime}"
        }
        helper.level.destroyBlock(helper.absolutePos(pos), true)
        val drops = helper.getEntities(EntityType.ITEM, pos, 2.0).map { it.item }
        checks.check(drops.any { it.`is`(ModItems.DISCS.getValue(ForgeBall.LOVE)) && it.count == 5 }) { "the discs should drop: $drops" }
        checks.check(drops.any { it.`is`(Items.CHARCOAL) }) { "the fuel should drop: $drops" }
        checks.check(drops.any { it.`is`(ModBlocks.MECHANICAL_ANVIL_ITEM) }) { "the anvil itself should drop: $drops" }
        checks.finish()
    }

    private fun placeMechanicalAnvil(helper: GameTestHelper): Pair<BlockPos, MechanicalAnvilBlockEntity> {
        val pos = BlockPos(3, 2, 3)
        helper.setBlock(pos, ModBlocks.MECHANICAL_ANVIL.defaultBlockState())
        return pos to helper.getBlockEntity(pos)
    }

    /** Runs the anvil's server tick [ticks] times right now (much faster than waiting). */
    private fun tick(helper: GameTestHelper, pos: BlockPos, anvil: MechanicalAnvilBlockEntity, ticks: Int) {
        val absolute = helper.absolutePos(pos)
        repeat(ticks) { anvil.serverTick(helper.level, absolute, helper.level.getBlockState(absolute)) }
    }
}
