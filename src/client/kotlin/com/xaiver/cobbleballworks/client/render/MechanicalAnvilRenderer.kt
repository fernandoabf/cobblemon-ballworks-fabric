package com.xaiver.cobbleballworks.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.xaiver.cobbleballworks.block.MechanicalAnvilBlock
import com.xaiver.cobbleballworks.block.entity.MechanicalAnvilBlockEntity
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

/** Shows the piece being pressed on the Mechanical Anvil's platform, its stage following the progress. */
class MechanicalAnvilRenderer(@Suppress("UNUSED_PARAMETER") context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<MechanicalAnvilBlockEntity> {

    override fun render(
        anvil: MechanicalAnvilBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        buffers: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        val piece = anvil.input
        if (piece.isEmpty) return
        val stage = (anvil.progressRatio * 3).toInt().coerceIn(0, 2)
        poseStack.pushPose()
        poseStack.translate(0.5, MechanicalAnvilBlock.PLATFORM_Y / 16.0, 0.5)
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f)
        PieceRenderer.render(piece, PieceRenderer.Shape.forStage(stage), poseStack, buffers, light, overlay)
        poseStack.popPose()
    }
}
