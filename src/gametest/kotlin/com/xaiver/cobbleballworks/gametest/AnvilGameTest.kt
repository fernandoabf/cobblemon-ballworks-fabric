package com.xaiver.cobbleballworks.gametest

import com.mojang.authlib.GameProfile
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity.StrikeResult
import com.xaiver.cobbleballworks.config.BallworksConfig
import com.xaiver.cobbleballworks.content.ForgeBall
import com.xaiver.cobbleballworks.content.HammerMaterial
import com.xaiver.cobbleballworks.item.AnvilStrikes
import com.xaiver.cobbleballworks.recipe.AnvilRecipe
import com.xaiver.cobbleballworks.registry.ModBlocks
import com.xaiver.cobbleballworks.registry.ModItems
import net.fabricmc.fabric.api.entity.FakePlayer
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import java.util.UUID

/** Section 5 of docs/PLANO.md: the Poké Ball anvil, through the real server code paths. */
class AnvilGameTest : FabricGameTest {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun anvilRecipesCoverEveryDiscAndTheIronDisc(helper: GameTestHelper) {
        val checks = Checks(helper)
        val level = helper.level
        for (ball in ForgeBall.entries) {
            val recipe = AnvilRecipe.find(level, ItemStack(ModItems.DISCS.getValue(ball)))?.value()
            checks.check(recipe?.result?.item == ModItems.LIDS.getValue(ball)) { "${ball.id} disc does not hammer into its lid" }
            val expectedForce = when (ball) {
                ForgeBall.MASTER, ForgeBall.BEAST, ForgeBall.ANCIENT_ORIGIN -> 15
                ForgeBall.DREAM, ForgeBall.CHERISH -> 10
                else -> 5
            }
            checks.check(recipe?.force == expectedForce) { "${ball.id} disc force ${recipe?.force}, expected $expectedForce" }
            checks.check(AnvilRecipe.find(level, ItemStack(ModItems.LIDS.getValue(ball))) == null) { "${ball.id} lid should be finished" }
        }
        val base = AnvilRecipe.find(level, ItemStack(ModItems.IRON_DISC))?.value()
        checks.check(base?.result?.item == ModItems.IRON_BASE && base.force == 5) { "the iron disc does not hammer into an iron base" }
        checks.check(base?.processingTime == 200) { "a force-5 piece should take 200 ticks in the Mechanical Anvil" }
        val anvil = ModBlocks.BALL_ANVIL.defaultBlockState()
        for (material in HammerMaterial.entries) {
            // Strength 1 mines at 1.01 so that Efficiency counts (vanilla only adds it above speed 1).
            val speed = ItemStack(ModItems.hammer(material)).getDestroySpeed(anvil)
            checks.check(speed == maxOf(material.strength.toFloat(), 1.01f)) { "${material.id} hammer mines the anvil at $speed" }
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun discsGoOnTheAnvilFromEitherHand(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeAnvil(helper)
        val player = fakePlayer(helper, "bw-place", GameType.SURVIVAL)
        val poke = ModItems.DISCS.getValue(ForgeBall.POKE)

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(poke, 3))
        rightClick(helper, player, pos)
        checks.check(anvil.item.item == poke && anvil.item.count == 1) { "main hand: anvil holds ${anvil.item}" }
        checks.check(player.mainHandItem.count == 2) { "main hand: ${player.mainHandItem.count} discs left, expected 2" }

        // Occupied: any main hand right click takes the piece back, no swap.
        rightClick(helper, player, pos)
        checks.check(anvil.isEmpty) { "right click on an occupied anvil should take the piece" }
        checks.check(player.mainHandItem.count == 3) { "the piece should come back to the inventory" }

        // Hammer in the main hand, discs in the off hand: the click falls through to the off hand.
        val great = ModItems.DISCS.getValue(ForgeBall.GREAT)
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(ModItems.hammer(HammerMaterial.IRON)))
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack(great, 2))
        rightClick(helper, player, pos)
        checks.check(anvil.item.item == great) { "off hand: anvil holds ${anvil.item}" }
        checks.check(player.offhandItem.count == 1) { "off hand: ${player.offhandItem.count} discs left, expected 1" }

        // Only pieces with an anvil recipe go on the anvil.
        anvil.take()
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(ModItems.LIDS.getValue(ForgeBall.POKE)))
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack(Items.IRON_INGOT))
        rightClick(helper, player, pos)
        checks.check(anvil.isEmpty) { "a lid or an iron ingot should not go on the anvil" }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun strikesAndWearFollowPixelmon(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (_, anvil) = placeAnvil(helper)
        val player = fakePlayer(helper, "bw-strikes", GameType.SURVIVAL)
        for (material in HammerMaterial.entries) {
            for ((ball, force) in listOf(ForgeBall.POKE to 5, ForgeBall.MASTER to 15)) {
                val disc = ModItems.DISCS.getValue(ball)
                val lid = ModItems.LIDS.getValue(ball)
                val hammer = ItemStack(ModItems.hammer(material))
                anvil.take()
                anvil.place(ItemStack(disc))
                var strikes = 0
                val stages = mutableListOf<Int>()
                while (anvil.item.item == disc && strikes < 200) {
                    val result = anvil.strike(player, hammer, material.strength)
                    strikes++
                    if (result == StrikeResult.STAGE) stages += strikes
                }
                val expected = 3 * ((force + material.strength - 1) / material.strength)
                checks.check(anvil.item.item == lid) { "${material.id} hammer: ${ball.id} disc became ${anvil.item}" }
                checks.check(strikes == expected) { "${material.id} hammer: $strikes strikes for a force-$force disc, expected $expected" }
                checks.check(stages.size == 2) { "${material.id} hammer: stages advanced at strikes $stages" }
                checks.check(hammer.damageValue == 3) { "${material.id} hammer lost ${hammer.damageValue} durability, expected 1 per stage" }
            }
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun finishedPieceIsKnockedOffByHammeringForFree(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (_, anvil) = placeAnvil(helper)
        val player = fakePlayer(helper, "bw-collect", GameType.SURVIVAL)
        val lid = ModItems.LIDS.getValue(ForgeBall.ULTRA)
        val hammer = ItemStack(ModItems.hammer(HammerMaterial.WOOD))
        anvil.place(ItemStack(lid))
        val results = (1..5).map { anvil.strike(player, hammer, 1) }
        checks.check(results.take(4).all { it == StrikeResult.HIT } && results[4] == StrikeResult.COLLECTED) {
            "a strength-1 hammer should knock a lid off at the 5th strike: $results"
        }
        checks.check(anvil.isEmpty) { "the anvil should be empty" }
        checks.check(player.inventory.countItem(lid) == 1) { "the lid should go to the inventory" }
        checks.check(hammer.damageValue == 0) { "collecting should not wear the hammer" }
        checks.check(anvil.strike(player, hammer, 1) == StrikeResult.NOTHING && hammer.damageValue == 0) {
            "hammering an empty anvil should do nothing"
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun takingThePieceBackLosesTheProgress(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeAnvil(helper)
        val player = fakePlayer(helper, "bw-takeback", GameType.SURVIVAL)
        val disc = ModItems.DISCS.getValue(ForgeBall.NET)
        anvil.place(ItemStack(disc))
        anvil.strike(player, ItemStack(ModItems.hammer(HammerMaterial.DIAMOND)), 5)
        checks.check(anvil.stage == 1) { "a diamond strike should advance one stage" }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(ModItems.hammer(HammerMaterial.DIAMOND)))
        rightClick(helper, player, pos)
        checks.check(anvil.isEmpty && anvil.stage == 0 && anvil.force == 0) { "the anvil should be reset" }
        checks.check(player.inventory.countItem(disc) == 1) { "the disc (not a lid) should come back" }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun hammerNeverBreaksTheAnvilButOtherToolsDo(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeAnvil(helper)
        val absolute = helper.absolutePos(pos)
        val player = fakePlayer(helper, "bw-break", GameType.SURVIVAL)
        val disc = ModItems.DISCS.getValue(ForgeBall.POKE)
        anvil.place(ItemStack(disc))

        // ServerPlayerGameMode.destroyBlock is what a completed mining of the block calls.
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(ModItems.hammer(HammerMaterial.DIAMOND)))
        checks.check(!player.gameMode.destroyBlock(absolute)) { "the hammer broke the anvil" }
        checks.check(helper.getBlockState(pos).`is`(ModBlocks.BALL_ANVIL)) { "the anvil is gone after a hammer strike" }
        checks.check(anvil.stage == 1) { "the completed 'break' should have been a strike (stage ${anvil.stage})" }

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(Items.IRON_PICKAXE))
        checks.check(player.gameMode.destroyBlock(absolute)) { "a pickaxe should break the anvil" }
        checks.check(helper.getBlockState(pos).isAir) { "the anvil should be gone" }
        checks.check(helper.getEntities(net.minecraft.world.entity.EntityType.ITEM, pos, 2.0).any { it.item.`is`(ModBlocks.BALL_ANVIL_ITEM) }) {
            "breaking should drop the anvil"
        }
        checks.check(helper.getEntities(net.minecraft.world.entity.EntityType.ITEM, pos, 2.0).any { it.item.`is`(disc) }) {
            "breaking should drop the piece on it, still a disc"
        }
        checks.finish()
    }

    /** Later strikes come without a new START packet, so the anvil checks their timing itself. */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun strikesFasterThanMiningAreIgnored(helper: GameTestHelper) {
        val (pos, anvil) = placeAnvil(helper)
        val absolute = helper.absolutePos(pos)
        val player = fakePlayer(helper, "bw-timing", GameType.SURVIVAL)
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(ModItems.hammer(HammerMaterial.WOOD)))
        anvil.place(ItemStack(ModItems.DISCS.getValue(ForgeBall.POKE)))
        player.gameMode.destroyBlock(absolute)
        player.gameMode.destroyBlock(absolute)
        if (anvil.force != 1) helper.fail("an instant second strike was accepted (force ${anvil.force})")
        // A wooden hammer mines the anvil in 30 ticks; 70% of that is the tolerance.
        helper.runAfterDelay(22) {
            player.gameMode.destroyBlock(absolute)
            if (anvil.force != 2) helper.fail("a strike after the mining time was refused (force ${anvil.force})")
            helper.succeed()
        }
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun creativeHammeringDoesNotWearTheHammer(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeAnvil(helper)
        val absolute = helper.absolutePos(pos)
        val player = fakePlayer(helper, "bw-creative", GameType.CREATIVE)
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(ModItems.hammer(HammerMaterial.DIAMOND)))
        anvil.place(ItemStack(ModItems.DISCS.getValue(ForgeBall.POKE)))
        repeat(3) { player.gameMode.destroyBlock(absolute) }
        checks.check(anvil.item.item == ModItems.LIDS.getValue(ForgeBall.POKE)) { "3 creative clicks with a diamond hammer: ${anvil.item}" }
        checks.check(player.mainHandItem.damageValue == 0) { "creative hammering wore the hammer" }
        checks.check(helper.getBlockState(pos).`is`(ModBlocks.BALL_ANVIL)) { "creative hammering broke the anvil" }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun pieceAndProgressSurviveSaveAndLoad(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeAnvil(helper)
        val player = fakePlayer(helper, "bw-nbt", GameType.SURVIVAL)
        val disc = ModItems.DISCS.getValue(ForgeBall.LUXURY)
        anvil.place(ItemStack(disc))
        val hammer = ItemStack(ModItems.hammer(HammerMaterial.STONE))
        repeat(7) { anvil.strike(player, hammer, 2) } // 2+2+2 -> stage 1, 2+2+2 -> stage 2, +2
        checks.check(anvil.stage == 2 && anvil.force == 2) { "setup: stage ${anvil.stage}, force ${anvil.force}" }
        val registries = helper.level.registryAccess()
        val tag = anvil.saveWithFullMetadata(registries)
        val copy = BlockEntity.loadStatic(helper.absolutePos(pos), helper.getBlockState(pos), tag, registries) as? BallAnvilBlockEntity
        checks.check(copy != null && copy.item.item == disc && copy.stage == 2 && copy.force == 2) {
            "after save/load: ${copy?.item} stage ${copy?.stage} force ${copy?.force}"
        }
        checks.finish()
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun autoReloadPullsTheNextDisc(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (_, anvil) = placeAnvil(helper)
        val player = fakePlayer(helper, "bw-reload", GameType.SURVIVAL)
        val disc = ModItems.DISCS.getValue(ForgeBall.QUICK)
        val lid = ModItems.LIDS.getValue(ForgeBall.QUICK)
        player.inventory.add(ItemStack(Items.DIRT, 5))
        player.inventory.add(ItemStack(disc, 2))
        val hammer = ItemStack(ModItems.hammer(HammerMaterial.DIAMOND))
        val previous = BallworksConfig.values
        try {
            BallworksConfig.values = previous.copy(allowAnvilAutoreloading = false)
            anvil.place(ItemStack(lid))
            anvil.strike(player, hammer, 5)
            checks.check(anvil.isEmpty) { "with autoreload off the anvil should stay empty" }
            BallworksConfig.values = previous.copy(allowAnvilAutoreloading = true)
            anvil.place(ItemStack(lid))
            anvil.strike(player, hammer, 5)
            checks.check(anvil.item.item == disc) { "with autoreload on the next disc should be on the anvil, found ${anvil.item}" }
            checks.check(player.inventory.countItem(disc) == 1) { "autoreload should take one disc from the inventory" }
            checks.check(player.inventory.countItem(lid) == 2) { "both lids should be in the inventory" }
        } finally {
            BallworksConfig.values = previous
        }
        checks.finish()
    }

    /**
     * Hammers take the mining enchantments: Efficiency makes strikes come faster (never stronger),
     * Unbreaking saves wear and Mending fits; Fortune and Silk Touch do not.
     */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun enchantmentsWorkOnHammers(helper: GameTestHelper) {
        val checks = Checks(helper)
        val (pos, anvil) = placeAnvil(helper)
        val absolute = helper.absolutePos(pos)
        val state = helper.level.getBlockState(absolute)
        val player = fakePlayer(helper, "bw-enchanted", GameType.SURVIVAL)
        val efficiency = enchantment(helper, Enchantments.EFFICIENCY)
        for (material in HammerMaterial.entries) {
            val plain = ItemStack(ModItems.hammer(material))
            for (key in listOf(Enchantments.EFFICIENCY, Enchantments.UNBREAKING, Enchantments.MENDING)) {
                checks.check(enchantment(helper, key).value().canEnchant(plain)) { "${material.id} hammer should take ${key.location()}" }
            }
            for (key in listOf(Enchantments.FORTUNE, Enchantments.SILK_TOUCH)) {
                checks.check(!enchantment(helper, key).value().canEnchant(plain)) { "${material.id} hammer should not take ${key.location()}" }
            }
            val fast = plain.copy().also { it.enchant(efficiency, 3) }
            equip(player, plain)
            val base = state.getDestroyProgress(player, helper.level, absolute)
            equip(player, fast)
            val boosted = state.getDestroyProgress(player, helper.level, absolute)
            checks.check(boosted > base * 1.5f) { "${material.id} hammer: Efficiency III should strike faster ($base -> $boosted per tick)" }
        }

        // Unbreaking III keeps 1 wear in 4 on average: 20 discs are 60 wear events.
        val hammer = ItemStack(ModItems.hammer(HammerMaterial.NETHERITE)).also { it.enchant(enchantment(helper, Enchantments.UNBREAKING), 3) }
        repeat(20) {
            anvil.take()
            anvil.place(ItemStack(ModItems.DISCS.getValue(ForgeBall.POKE)))
            repeat(3) { anvil.strike(player, hammer, HammerMaterial.NETHERITE.strength) }
        }
        checks.check(hammer.damageValue in 1 until 45) { "Unbreaking III left ${hammer.damageValue} of 60 wear" }
        checks.finish()
    }

    /** Efficiency V lets a strong hammer "instamine" the anvil; strikes still keep vanilla's 5-tick pause. */
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun instantStrikesKeepThePause(helper: GameTestHelper) {
        val (pos, anvil) = placeAnvil(helper)
        val absolute = helper.absolutePos(pos)
        val player = fakePlayer(helper, "bw-instant", GameType.SURVIVAL)
        val hammer = ItemStack(ModItems.hammer(HammerMaterial.NETHERITE)).also { it.enchant(enchantment(helper, Enchantments.EFFICIENCY), 5) }
        equip(player, hammer)
        anvil.place(ItemStack(ModItems.DISCS.getValue(ForgeBall.MASTER)))
        if (helper.level.getBlockState(absolute).getDestroyProgress(player, helper.level, absolute) < 1f) {
            helper.fail("an Efficiency V netherite hammer should instamine the anvil")
        }
        player.gameMode.destroyBlock(absolute)
        player.gameMode.destroyBlock(absolute)
        if (anvil.stage != 0 || anvil.force != HammerMaterial.NETHERITE.strength) {
            helper.fail("a second strike in the same tick was accepted (stage ${anvil.stage}, force ${anvil.force})")
        }
        helper.runAfterDelay(AnvilStrikes.MIN_TICKS_BETWEEN_STRIKES.toLong()) {
            player.gameMode.destroyBlock(absolute)
            if (anvil.stage != 1) helper.fail("a strike after the pause was refused (stage ${anvil.stage}, force ${anvil.force})")
            helper.succeed()
        }
    }

    private fun enchantment(helper: GameTestHelper, key: ResourceKey<Enchantment>): Holder<Enchantment> =
        helper.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key)

    /**
     * Puts [stack] in the main hand with its attribute modifiers (Efficiency adds mining speed), as
     * a ticking player's equipment check would; fake players do not tick.
     */
    private fun equip(player: FakePlayer, stack: ItemStack) {
        player.mainHandItem.forEachModifier(EquipmentSlot.MAINHAND) { attribute, modifier ->
            player.getAttribute(attribute)?.removeModifier(modifier.id())
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, stack)
        stack.forEachModifier(EquipmentSlot.MAINHAND) { attribute, modifier ->
            player.getAttribute(attribute)?.let {
                it.removeModifier(modifier.id())
                it.addTransientModifier(modifier)
            }
        }
    }

    private fun placeAnvil(helper: GameTestHelper): Pair<BlockPos, BallAnvilBlockEntity> {
        val pos = BlockPos(3, 2, 3)
        helper.setBlock(pos.below(), Blocks.STONE)
        helper.setBlock(pos, ModBlocks.BALL_ANVIL.defaultBlockState())
        return pos to helper.getBlockEntity(pos)
    }

    /** A survival or creative server player of its own, standing next to the anvil. */
    private fun fakePlayer(helper: GameTestHelper, name: String, mode: GameType): FakePlayer {
        val player = FakePlayer.get(helper.level, GameProfile(UUID.nameUUIDFromBytes(name.toByteArray()), name))
        player.setGameMode(mode)
        player.inventory.clearContent()
        val stand = helper.absolutePos(BlockPos(3, 2, 2))
        player.moveTo(stand.x + 0.5, stand.y.toDouble(), stand.z + 0.5, 0f, 0f)
        player.setOnGround(true)
        return player
    }

    /** What the client does on a right click: try the main hand, then the off hand. */
    private fun rightClick(helper: GameTestHelper, player: FakePlayer, pos: BlockPos) {
        val absolute = helper.absolutePos(pos)
        val hit = BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)
        for (hand in InteractionHand.entries) {
            val result = player.gameMode.useItemOn(player, helper.level, player.getItemInHand(hand), hand, hit)
            if (result.consumesAction()) return
        }
    }
}
