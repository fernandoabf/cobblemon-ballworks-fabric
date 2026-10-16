package com.xaiver.cobbleballworks.content

import net.minecraft.resources.ResourceLocation

/** The seven apricorn colours. Raw apricorns are Cobblemon items; cooked ones are ours. */
enum class ApricornColor(val id: String) {
    RED("red"),
    YELLOW("yellow"),
    GREEN("green"),
    BLUE("blue"),
    PINK("pink"),
    BLACK("black"),
    WHITE("white"),
    ;

    val rawApricornId: ResourceLocation get() = ResourceLocation.fromNamespaceAndPath("cobblemon", "${id}_apricorn")

    val cookedPath: String get() = "cooked_${id}_apricorn"
}
