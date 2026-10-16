package com.xaiver.cobbleballworks.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.xaiver.cobbleballworks.CobbleBallworks
import com.xaiver.cobbleballworks.content.ForgeBall
import com.xaiver.cobbleballworks.registry.ModItems
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import java.util.WeakHashMap

/**
 * Draws the piece being forged with Cobblemon's own 3D Poké Ball, the model it uses for a ball held
 * in hand (models/piece, made by tools/derive_piece_models.py): a lid is the top half of its ball,
 * and a disc is that same half squashed flat, rising into the dome stage by stage, so every stage
 * keeps the ball's real shape and colours. Iron discs and bases are the bottom half in iron, dome
 * up. Anything else an anvil recipe accepts lies flat as its item.
 *
 * The pose is in block units with the origin at the centre of the surface the piece rests on.
 */
object PieceRenderer {
    /**
     * Cobblemon's ball is half a block wide; at this size the lid (5.5 x 2.9 px) fits the anvil's
     * 7 px face and the 3 px gap under the Mechanical Anvil's press.
     */
    private const val SCALE = 0.65f

    /** Height of a flat disc, as a fraction of the finished dome. */
    private const val FLAT_HEIGHT = 0.3f

    /** A flat disc is a little wider than the dome it becomes. */
    private const val FLAT_WIDTH = 1.12f

    /** Shaping gained per tick while the piece catches up with a new stage. */
    private const val GROWTH_PER_TICK = 0.08f

    private class Piece(val model: ResourceLocation, val upsideDown: Boolean)

    private fun lidModel(ball: ForgeBall) = CobbleBallworks.id("piece/${ball.id}_lid")

    private val IRON_BASE_MODEL = CobbleBallworks.id("piece/iron_base")

    /** Extra models to load (CobbleBallworksClient adds them to the model loader). */
    val modelIds: List<ResourceLocation> = ForgeBall.entries.map(::lidModel) + IRON_BASE_MODEL

    private val pieces: Map<Item, Piece> by lazy {
        buildMap {
            for (ball in ForgeBall.entries) {
                val piece = Piece(lidModel(ball), upsideDown = false)
                put(ModItems.DISCS.getValue(ball), piece)
                put(ModItems.LIDS.getValue(ball), piece)
            }
            val iron = Piece(IRON_BASE_MODEL, upsideDown = true)
            put(ModItems.IRON_DISC, iron)
            put(ModItems.IRON_BASE, iron)
        }
    }

    /**
     * @param shaping 0 = a flat disc, 1 = the finished dome
     * @param facing where the front of the piece (the side the button goes on) looks
     */
    fun render(
        stack: ItemStack,
        shaping: Float,
        facing: Direction,
        poseStack: PoseStack,
        buffers: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - facing.toYRot()))
        val piece = pieces[stack.item]
        val model = piece?.let { Minecraft.getInstance().modelManager.getModel(it.model) }
        if (piece == null || model == null) {
            renderFlatItem(stack, poseStack, buffers, light, overlay)
        } else {
            val t = shaping.coerceIn(0f, 1f)
            val width = Mth.lerp(t, FLAT_WIDTH, 1f)
            poseStack.scale(SCALE * width, SCALE * Mth.lerp(t, FLAT_HEIGHT, 1f), SCALE * width)
            val geometry = geometryOf(model)
            if (piece.upsideDown) {
                // Turned over around Z: x -> -x, y -> -y; its old top becomes the bottom.
                poseStack.translate((geometry.minX + geometry.maxX) / 2, geometry.maxY, -(geometry.minZ + geometry.maxZ) / 2)
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f))
            } else {
                poseStack.translate(-(geometry.minX + geometry.maxX) / 2, -geometry.minY, -(geometry.minZ + geometry.maxZ) / 2)
            }
            val consumer = buffers.getBuffer(Sheets.cutoutBlockSheet())
            val pose = poseStack.last()
            for (quad in geometry.quads) consumer.putBulkData(pose, quad, 1f, 1f, 1f, 1f, light, overlay)
        }
        poseStack.popPose()
    }

    /** A piece without a 3D model of its own, lying flat like an item on a table. */
    private fun renderFlatItem(stack: ItemStack, poseStack: PoseStack, buffers: MultiBufferSource, light: Int, overlay: Int) {
        poseStack.translate(0f, 1 / 64f, 0f)
        poseStack.mulPose(Axis.XP.rotationDegrees(90f))
        poseStack.scale(0.5f, 0.5f, 0.5f)
        Minecraft.getInstance().itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poseStack, buffers, null, 0)
    }

    /**
     * Eases [target] in for [anvil]: when a stage is reached the piece grows into it over a few
     * ticks instead of jumping; a new piece (a lower target) shows up at once.
     */
    fun smoothShaping(anvil: BlockEntity, target: Float, partialTick: Float): Float {
        val now = (anvil.level?.gameTime ?: 0L) + partialTick
        val state = shown.getOrPut(anvil) { floatArrayOf(target, now) }
        val elapsed = (now - state[1]).coerceIn(0f, 20f)
        state[1] = now
        state[0] = if (target <= state[0]) target else minOf(target, state[0] + elapsed * GROWTH_PER_TICK)
        return state[0]
    }

    private val shown = WeakHashMap<BlockEntity, FloatArray>()

    /** Every quad of a baked model and its bounds; rebuilt when a resource reload bakes new models. */
    private class Geometry(val quads: List<BakedQuad>) {
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

        init {
            for (quad in quads) {
                val data = quad.vertices
                val stride = data.size / 4
                for (vertex in 0 until 4) {
                    val x = Float.fromBits(data[vertex * stride])
                    val y = Float.fromBits(data[vertex * stride + 1])
                    val z = Float.fromBits(data[vertex * stride + 2])
                    minX = minOf(minX, x); minY = minOf(minY, y); minZ = minOf(minZ, z)
                    maxX = maxOf(maxX, x); maxY = maxOf(maxY, y); maxZ = maxOf(maxZ, z)
                }
            }
            if (quads.isEmpty()) { minX = 0f; minY = 0f; minZ = 0f; maxX = 0f; maxY = 0f; maxZ = 0f }
        }
    }

    private val geometries = WeakHashMap<BakedModel, Geometry>()

    private val random = RandomSource.create()

    private fun geometryOf(model: BakedModel): Geometry = geometries.getOrPut(model) {
        val quads = (listOf<Direction?>(null) + Direction.entries).flatMap { side ->
            random.setSeed(42L)
            model.getQuads(null, side, random)
        }
        Geometry(quads)
    }
}
