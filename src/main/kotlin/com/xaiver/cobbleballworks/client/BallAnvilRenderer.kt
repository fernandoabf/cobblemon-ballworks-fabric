package com.xaiver.cobbleballworks.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.xaiver.cobbleballworks.block.BallAnvilBlock
import com.xaiver.cobbleballworks.block.BallAnvilBlockEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

/**
 * Client-only renderer for the Ball Anvil. Draws the stored workpiece laid flat on top
 * of the anvil, rotated to match the block's FACING, and scaled based on hammering
 * progress so a fresh blank reads as visibly smaller/flatter than a nearly-finished
 * formed part.
 *
 * Consumes three read-only BE accessors — [BallAnvilBlockEntity.storedItem],
 * [BallAnvilBlockEntity.progressRatio], [BallAnvilBlockEntity.requiredHits] — all of
 * which sync to the client via the standard block-entity update packet. No custom
 * networking, no state in the renderer.
 *
 * Fallback visual only: renders the stored item's own inventory model. Replacing this
 * with the imported Pixelmon `anvil_states/<ball>_disc_flat|stage1|stage2` block
 * models is a future BER milestone.
 */
@OnlyIn(Dist.CLIENT)
class BallAnvilRenderer(
    @Suppress("UNUSED_PARAMETER") ctx: BlockEntityRendererProvider.Context
) : BlockEntityRenderer<BallAnvilBlockEntity> {

    override fun render(
        be: BallAnvilBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val stack = be.storedItem
        if (stack.isEmpty) return

        val level = be.level ?: return

        poseStack.pushPose()

        // 1) Center the workpiece above the anvil's top-rim cap (y=15 in our 5-element
        //    model). A small +0.01 offset avoids z-fighting with the top face.
        poseStack.translate(0.5, TOP_Y_OFFSET, 0.5)

        // 2) Align with the anvil's FACING so the workpiece rotates with the block.
        val facingDegrees = when (be.blockState.getValue(BallAnvilBlock.FACING)) {
            Direction.EAST -> 90f
            Direction.SOUTH -> 180f
            Direction.WEST -> 270f
            else -> 0f // NORTH + anything non-horizontal
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(facingDegrees))

        // 3) Lay the "item/generated" sprite flat so its face points up. FIXED renders
        //    the quad vertical by default (item-frame orientation), so a -90° X
        //    rotation drops it into a pancake lying on the anvil.
        poseStack.mulPose(Axis.XP.rotationDegrees(-90f))

        // 4) Scale based on progress. Blank = 0.5 (flattest / smallest read); fully
        //    formed = 0.75 (biggest read). A finished item (no recipe left) treats as
        //    full-progress via progressRatio=1.0.
        val ratio = be.progressRatio.coerceIn(0f, 1f)
        val scale = MIN_SCALE + (MAX_SCALE - MIN_SCALE) * ratio
        poseStack.scale(scale, scale, scale)

        // 5) Lift slightly as progress increases so a fully-shaped lid sits a touch
        //    higher than a flat blank. Cosmetic; stays within the top_rim height.
        val lift = (ratio * PROGRESS_LIFT).toDouble()
        poseStack.translate(0.0, lift, 0.0)

        Minecraft.getInstance().itemRenderer.renderStatic(
            stack,
            ItemDisplayContext.FIXED,
            packedLight,
            packedOverlay,
            poseStack,
            bufferSource,
            level,
            0
        )

        poseStack.popPose()
    }

    companion object {
        /** y-offset (in block units) of the anvil's top rim = 15/16 + a small lift. */
        private const val TOP_Y_OFFSET = 15.0 / 16.0 + 0.02
        private const val MIN_SCALE = 0.5f
        private const val MAX_SCALE = 0.75f
        /** Extra y-lift applied linearly from 0 (blank) to this value at full progress. */
        private const val PROGRESS_LIFT = 0.06f
    }
}
