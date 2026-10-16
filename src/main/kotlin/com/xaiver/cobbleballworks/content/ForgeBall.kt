package com.xaiver.cobbleballworks.content

import net.minecraft.resources.ResourceLocation

/**
 * Poké Balls forged Pixelmon-style: cooked apricorn disc -> hammered on the anvil into a lid ->
 * lid + stone button + iron base.
 *
 * The first 22 are the forgeable balls of Pixelmon 1.12.2 (same order as its creative tab); the
 * last 5 only exist in Cobblemon. Master, Park, Beast, Dream, Cherish and the ancient balls have
 * no disc, as in Pixelmon, and keep Cobblemon's own recipes.
 */
enum class ForgeBall(val id: String) {
    POKE("poke"),
    GREAT("great"),
    ULTRA("ultra"),
    LEVEL("level"),
    MOON("moon"),
    FRIEND("friend"),
    LOVE("love"),
    SAFARI("safari"),
    HEAVY("heavy"),
    FAST("fast"),
    REPEAT("repeat"),
    TIMER("timer"),
    NEST("nest"),
    NET("net"),
    DIVE("dive"),
    LUXURY("luxury"),
    HEAL("heal"),
    DUSK("dusk"),
    PREMIER("premier"),
    SPORT("sport"),
    QUICK("quick"),
    LURE("lure"),
    CITRINE("citrine"),
    VERDANT("verdant"),
    AZURE("azure"),
    ROSEATE("roseate"),
    SLATE("slate"),
    ;

    /** Disc item path. The original Ballworks called the disc a "lid blank"; the id is kept. */
    val discPath: String get() = "${id}_lid_blank"

    val lidPath: String get() = "${id}_lid"

    /** The Cobblemon ball this lid is assembled into. */
    val ballId: ResourceLocation get() = ResourceLocation.fromNamespaceAndPath("cobblemon", "${id}_ball")
}
