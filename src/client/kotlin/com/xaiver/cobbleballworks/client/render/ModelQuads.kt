package com.xaiver.cobbleballworks.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import java.util.WeakHashMap

/**
 * Draws baked JSON models from block entity renderers: every quad, untinted, lit by its normal.
 * Quad lists are kept per baked model, so a resource reload (new models) starts over.
 */
object ModelQuads {
    private val quads = WeakHashMap<BakedModel, List<BakedQuad>>()

    private val random = RandomSource.create()

    fun of(model: BakedModel): List<BakedQuad> = quads.getOrPut(model) {
        (listOf<Direction?>(null) + Direction.entries).flatMap { side ->
            random.setSeed(42L)
            model.getQuads(null, side, random)
        }
    }

    fun draw(model: BakedModel, poseStack: PoseStack, consumer: VertexConsumer, light: Int, overlay: Int) {
        val pose = poseStack.last()
        for (quad in of(model)) consumer.putBulkData(pose, quad, 1f, 1f, 1f, 1f, light, overlay)
    }
}
