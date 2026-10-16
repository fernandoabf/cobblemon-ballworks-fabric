package com.xaiver.cobbleballworks.registry

import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.block.BallAnvilBlock
import com.xaiver.cobbleballworks.block.MechanicalAnvilBlock
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

object ModBlocks {
    /**
     * Pixelmon's anvil: hardness 1.0 and no tool needed (so a hammer of strength S takes 30/S ticks
     * per strike), metal sounds, very high blast resistance.
     */
    val BALL_ANVIL: BallAnvilBlock = register(
        "ball_anvil",
        BallAnvilBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0f, 1200.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .pushReaction(PushReaction.BLOCK),
        ),
    )

    val BALL_ANVIL_ITEM: BlockItem = registerItem(BALL_ANVIL)

    /**
     * Pixelmon's Mechanical Anvil: hardness 2.5, needs a pickaxe, glows (light 15, as in Pixelmon)
     * while it works.
     */
    val MECHANICAL_ANVIL: MechanicalAnvilBlock = register(
        "mechanical_anvil",
        MechanicalAnvilBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion()
                .lightLevel { state -> if (state.getValue(MechanicalAnvilBlock.LIT)) 15 else 0 },
        ),
    )

    val MECHANICAL_ANVIL_ITEM: BlockItem = registerItem(MECHANICAL_ANVIL)

    /** Block items, in creative tab order. */
    fun items(): List<Item> = listOf(BALL_ANVIL_ITEM, MECHANICAL_ANVIL_ITEM)

    private fun <T : Block> register(path: String, block: T): T =
        Registry.register(BuiltInRegistries.BLOCK, CobbleBallworks.id(path), block)

    private fun registerItem(block: Block): BlockItem =
        Registry.register(BuiltInRegistries.ITEM, BuiltInRegistries.BLOCK.getKey(block), BlockItem(block, Item.Properties()))

    fun init() = Unit
}
