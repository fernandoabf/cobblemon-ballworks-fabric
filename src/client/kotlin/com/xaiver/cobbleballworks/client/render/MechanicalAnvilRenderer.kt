package com.xaiver.cobbleballworks.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.block.MechanicalAnvilBlock
import com.xaiver.cobbleballworks.block.entity.MechanicalAnvilBlockEntity
import com.xaiver.cobbleballworks.content.AnvilRules
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

/**
 * The Mechanical Anvil's moving parts: the piece on the platform, its shape following the progress,
 * and the press, which hangs under the crossbar and, while the machine works (lit, with a piece),
 * comes down onto the piece once a second, squashes it a little and goes back up on its rod.
 */
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
        val state = anvil.blockState
        // Clients hear about the progress once per stage, like the hand anvil's stages.
        val stage = (anvil.progressRatio * AnvilRules.STAGES).toInt().coerceIn(0, AnvilRules.STAGES - 1)
        val shaping = if (piece.isEmpty) 0f else PieceRenderer.smoothShaping(anvil, stage / AnvilRules.STAGES.toFloat(), partialTick)
        val pieceHeight = if (piece.isEmpty) 0f else PieceRenderer.heightOf(piece, shaping) * 16f

        // Gap between the press and the platform, in pixels: REST_GAP at rest, the squashed piece's
        // height at the bottom of a stroke.
        val working = !piece.isEmpty && state.getValue(MechanicalAnvilBlock.LIT)
        val stroke = if (working) stroke(anvil, partialTick) else 0f
        val gap = Mth.lerp(stroke, REST_GAP, pieceHeight * PRESSED)

        if (!piece.isEmpty) {
            poseStack.pushPose()
            poseStack.translate(0.5, MechanicalAnvilBlock.PLATFORM_Y / 16.0, 0.5)
            val squash = if (gap < pieceHeight) gap / pieceHeight else 1f
            PieceRenderer.render(piece, shaping, state.getValue(MechanicalAnvilBlock.FACING), poseStack, buffers, light, overlay, squash)
            poseStack.popPose()
        }
        renderPress(REST_GAP - gap, poseStack, buffers, light, overlay)
    }

    /** The press [drop] pixels below its resting place, hanging from the crossbar by its rod. */
    private fun renderPress(drop: Float, poseStack: PoseStack, buffers: MultiBufferSource, light: Int, overlay: Int) {
        val models = Minecraft.getInstance().modelManager
        val consumer = buffers.getBuffer(Sheets.solidBlockSheet())
        models.getModel(PRESS_MODEL)?.let { press ->
            poseStack.pushPose()
            poseStack.translate(0.0, -drop / 16.0, 0.0)
            ModelQuads.draw(press, poseStack, consumer, light, overlay)
            poseStack.popPose()
        }
        if (drop > 0.01f) {
            models.getModel(ROD_MODEL)?.let { rod ->
                poseStack.pushPose()
                // The rod model is 1 px tall: stretch it from the top of the press to the crossbar.
                poseStack.translate(0.0, (PRESS_TOP - drop) / 16.0, 0.0)
                poseStack.scale(1f, drop, 1f)
                ModelQuads.draw(rod, poseStack, consumer, light, overlay)
                poseStack.popPose()
            }
        }
    }

    /**
     * How far down the press is, 0 (up) to 1 (on the piece), over a [PERIOD]-tick stroke: it falls,
     * rests on the piece, rises and waits. Machines next to each other keep different beats.
     */
    private fun stroke(anvil: MechanicalAnvilBlockEntity, partialTick: Float): Float {
        val tick = Math.floorMod((anvil.level?.gameTime ?: 0L) + anvil.blockPos.hashCode(), PERIOD.toLong())
        val phase = (tick + partialTick) / PERIOD
        return when {
            phase < FALL -> (phase / FALL).let { it * it }
            phase < HOLD -> 1f
            phase < RISE -> 1f - Mth.smoothstep(((phase - HOLD) / (RISE - HOLD)).toDouble()).toFloat()
            else -> 0f
        }
    }

    companion object {
        val PRESS_MODEL: ResourceLocation = CobbleBallworks.id("block/mechanical_anvil_press")
        val ROD_MODEL: ResourceLocation = CobbleBallworks.id("block/mechanical_anvil_rod")

        /** Extra models to load (CobbleBallworksClient adds them to the model loader). */
        val modelIds: List<ResourceLocation> = listOf(PRESS_MODEL, ROD_MODEL)

        /** The press hangs from y 14 (bottom) to 15 (top), 5 px over the platform (PLATFORM_Y). */
        private const val PRESS_TOP = 15f
        private val REST_GAP = 14f - MechanicalAnvilBlock.PLATFORM_Y.toFloat()

        /** At the bottom of a stroke the piece is pressed to this fraction of its height. */
        private const val PRESSED = 0.85f

        /** One stroke a second: 200 ticks per regular disc are 10 strokes. */
        private const val PERIOD = 20

        /** Phase marks of a stroke: end of the fall, of the rest on the piece and of the rise. */
        private const val FALL = 0.18f
        private const val HOLD = 0.3f
        private const val RISE = 0.75f
    }
}
