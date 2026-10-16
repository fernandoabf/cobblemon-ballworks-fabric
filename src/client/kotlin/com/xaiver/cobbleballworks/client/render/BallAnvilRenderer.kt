package com.xaiver.cobbleballworks.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.xaiver.cobbleballworks.block.BallAnvilBlock
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import com.xaiver.cobbleballworks.content.AnvilRules
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.world.level.block.HorizontalDirectionalBlock

/** Draws the piece on the Poké Ball anvil: a flat disc that rises stage by stage into the lid. */
class BallAnvilRenderer(@Suppress("UNUSED_PARAMETER") context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<BallAnvilBlockEntity> {

    override fun render(
        anvil: BallAnvilBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        buffers: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        val piece = anvil.item
        if (piece.isEmpty) return
        val target = if (anvil.isFinished()) 1f else anvil.stage / AnvilRules.STAGES.toFloat()
        val facing = anvil.blockState.getOptionalValue(HorizontalDirectionalBlock.FACING).orElse(Direction.NORTH)
        poseStack.pushPose()
        // Centre of the anvil's working face.
        poseStack.translate(0.5, BallAnvilBlock.TOP_Y / 16.0, 0.5)
        PieceRenderer.render(piece, PieceRenderer.smoothShaping(anvil, target, partialTick), facing, poseStack, buffers, light, overlay)
        poseStack.popPose()
    }
}
