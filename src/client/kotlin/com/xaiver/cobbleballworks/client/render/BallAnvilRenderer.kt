package com.xaiver.cobbleballworks.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.xaiver.cobbleballworks.block.BallAnvilBlock
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

/** Draws the piece on the Poké Ball anvil: its stage while it is a disc, a dome once it is done. */
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
        val shape = if (anvil.isFinished()) PieceRenderer.Shape.DOME else PieceRenderer.Shape.forStage(anvil.stage)
        poseStack.pushPose()
        // Centre of the anvil's working face, then work in pixels.
        poseStack.translate(0.5, BallAnvilBlock.TOP_Y / 16.0, 0.5)
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f)
        PieceRenderer.render(piece, shape, poseStack, buffers, light, overlay)
        poseStack.popPose()
    }
}
