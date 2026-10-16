package com.xaiver.cobbleballworks.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.xaiver.cobbleballworks.block.MechanicalAnvilBlock
import com.xaiver.cobbleballworks.block.entity.MechanicalAnvilBlockEntity
import com.xaiver.cobbleballworks.content.AnvilRules
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

/** Shows the piece being pressed on the Mechanical Anvil's platform, its shape following the progress. */
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
        // Clients hear about the progress once per stage, like the hand anvil's stages.
        val stage = (anvil.progressRatio * AnvilRules.STAGES).toInt().coerceIn(0, AnvilRules.STAGES - 1)
        val facing = anvil.blockState.getValue(MechanicalAnvilBlock.FACING)
        poseStack.pushPose()
        poseStack.translate(0.5, MechanicalAnvilBlock.PLATFORM_Y / 16.0, 0.5)
        val shaping = PieceRenderer.smoothShaping(anvil, stage / AnvilRules.STAGES.toFloat(), partialTick)
        PieceRenderer.render(piece, shaping, facing, poseStack, buffers, light, overlay)
        poseStack.popPose()
    }
}
