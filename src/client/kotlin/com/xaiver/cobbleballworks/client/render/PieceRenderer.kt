package com.xaiver.cobbleballworks.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.world.item.ItemStack
import java.util.WeakHashMap

/**
 * Draws a piece being forged: a disc that curves up stage by stage into a dome (the lid), like
 * Pixelmon's ModelDiscFlat/Stage1/Stage2/HemiSphere (whose exported geometry is upside down and
 * 3.8 px wide; this one is 6 px wide and stepped like pixel art).
 *
 * The piece is textured with its own item sprite: the sides show the sprite's opaque area wrapped
 * around the piece (bottom rows at the bottom), the top shows the widest row of colour. So every
 * ball gets the right colours, including items added by datapacks and resource packs.
 *
 * Coordinates are pixels with the origin at the centre of the piece's base.
 */
object PieceRenderer {

    data class Layer(val width: Float, val bottom: Float, val top: Float)

    enum class Shape(vararg layers: Layer) {
        FLAT(Layer(6f, 0f, 0.8f)),
        STAGE_1(Layer(6f, 0f, 0.8f), Layer(4f, 0.8f, 1.4f)),
        STAGE_2(Layer(6f, 0f, 0.9f), Layer(4f, 0.9f, 1.9f), Layer(2f, 1.9f, 2.3f)),
        DOME(Layer(6f, 0f, 1.2f), Layer(4f, 1.2f, 2.5f), Layer(2f, 2.5f, 3f)),
        ;

        val layers: List<Layer> = layers.toList()
        val height: Float = layers.last().top

        companion object {
            fun forStage(stage: Int): Shape = when (stage) {
                0 -> FLAT
                1 -> STAGE_1
                else -> STAGE_2
            }
        }
    }

    private const val WIDTH = 6f

    fun render(stack: ItemStack, shape: Shape, poseStack: PoseStack, buffers: MultiBufferSource, light: Int, overlay: Int) {
        val sprite = Minecraft.getInstance().itemRenderer.getModel(stack, null, null, 0).particleIcon
        val area = areaOf(sprite)
        val painter = Painter(buffers.getBuffer(Sheets.cutoutBlockSheet()), poseStack.last(), sprite, area, shape.height, light, overlay)
        for (layer in shape.layers) {
            val half = layer.width / 2
            if (layer.width <= 2f) {
                painter.box(-half, layer.bottom, -half, half, layer.top, half)
            } else {
                // A cross of two boxes: a square with its corners cut, which reads as round.
                painter.box(-half, layer.bottom, -(half - 1), half, layer.top, half - 1)
                painter.box(-(half - 1), layer.bottom, -half, half - 1, layer.top, half)
            }
        }
    }

    /** Opaque bounds of a sprite and its widest row of colour, in sprite pixels. */
    private class SpriteArea(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val bandY: Float, val bandX0: Float, val bandX1: Float)

    private val areas = WeakHashMap<TextureAtlasSprite, SpriteArea>()

    private fun areaOf(sprite: TextureAtlasSprite): SpriteArea = synchronized(areas) {
        areas.getOrPut(sprite) { measure(sprite) }
    }

    private fun measure(sprite: TextureAtlasSprite): SpriteArea {
        val contents = sprite.contents()
        val w = contents.width()
        val h = contents.height()
        val spans = (0 until h).map { y ->
            val xs = (0 until w).filter { x -> !contents.isTransparent(0, x, y) }
            if (xs.isEmpty()) null else xs.first() to xs.last() + 1
        }
        val rows = spans.indices.filter { spans[it] != null }
        if (rows.isEmpty()) return SpriteArea(0f, 0f, w.toFloat(), h.toFloat(), h / 2f, 0f, w.toFloat())
        val x0 = rows.minOf { spans[it]!!.first }
        val x1 = rows.maxOf { spans[it]!!.second }
        // Widest row above the bottom one (the bottom row is usually a dark rim); lowest on ties.
        val candidates = if (rows.size > 1) rows.dropLast(1) else rows
        val band = candidates.maxWith(compareBy<Int> { spans[it]!!.second - spans[it]!!.first }.thenBy { it })
        val (bx0, bx1) = spans[band]!!
        val scale = 16f / w
        return SpriteArea(
            x0 * scale, rows.first() * scale, x1 * scale, (rows.last() + 1) * scale,
            (band + 0.5f) * scale, (bx0 + 0.5f) * scale, (bx1 - 0.5f) * scale,
        )
    }

    private class Painter(
        val consumer: VertexConsumer,
        val pose: PoseStack.Pose,
        val sprite: TextureAtlasSprite,
        val area: SpriteArea,
        val height: Float,
        val light: Int,
        val overlay: Int,
    ) {
        /** Horizontal position t in [-3, 3] across the piece -> sprite u (pixels). */
        fun sideU(t: Float): Float = area.x0 + (t + WIDTH / 2) / WIDTH * (area.x1 - area.x0)

        /** Height y in [0, height] -> sprite v (pixels): the bottom of the piece is the bottom of the sprite. */
        fun sideV(y: Float): Float = area.y1 - y / height * (area.y1 - area.y0)

        fun topU(x: Float): Float = area.bandX0 + (x + WIDTH / 2) / WIDTH * (area.bandX1 - area.bandX0)

        fun box(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float) {
            // Sides, as seen from outside: left to right, top to bottom.
            quad(x1, z0, x0, z0, y0, y1, 0f, 0f, -1f, sideU(-x1), sideU(-x0)) // north: +X is on the left
            quad(x0, z1, x1, z1, y0, y1, 0f, 0f, 1f, sideU(x0), sideU(x1)) // south
            quad(x1, z1, x1, z0, y0, y1, 1f, 0f, 0f, sideU(-z1), sideU(-z0)) // east: +Z is on the left
            quad(x0, z0, x0, z1, y0, y1, -1f, 0f, 0f, sideU(z0), sideU(z1)) // west
            // Top: one row of colour stretched over it, so a cut-out sprite never leaves holes.
            val v = area.bandY
            vertex(x0, y1, z0, topU(x0), v - 0.25f, 0f, 1f, 0f)
            vertex(x0, y1, z1, topU(x0), v + 0.25f, 0f, 1f, 0f)
            vertex(x1, y1, z1, topU(x1), v + 0.25f, 0f, 1f, 0f)
            vertex(x1, y1, z0, topU(x1), v - 0.25f, 0f, 1f, 0f)
            // Bottom: the same row (it rests on the anvil and is rarely seen).
            vertex(x0, y0, z1, topU(x0), v + 0.25f, 0f, -1f, 0f)
            vertex(x0, y0, z0, topU(x0), v - 0.25f, 0f, -1f, 0f)
            vertex(x1, y0, z0, topU(x1), v - 0.25f, 0f, -1f, 0f)
            vertex(x1, y0, z1, topU(x1), v + 0.25f, 0f, -1f, 0f)
        }

        /** A vertical face from (ax, az) on the viewer's left to (bx, bz) on the right. */
        private fun quad(ax: Float, az: Float, bx: Float, bz: Float, y0: Float, y1: Float, nx: Float, ny: Float, nz: Float, u0: Float, u1: Float) {
            val vTop = sideV(y1)
            val vBottom = sideV(y0)
            vertex(ax, y1, az, u0, vTop, nx, ny, nz)
            vertex(ax, y0, az, u0, vBottom, nx, ny, nz)
            vertex(bx, y0, bz, u1, vBottom, nx, ny, nz)
            vertex(bx, y1, bz, u1, vTop, nx, ny, nz)
        }

        private fun vertex(x: Float, y: Float, z: Float, u: Float, v: Float, nx: Float, ny: Float, nz: Float) {
            consumer.addVertex(pose.pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(sprite.getU(u / 16f), sprite.getV(v / 16f))
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz)
        }
    }
}
