package com.xaiver.cobbleballworks.content

import net.minecraft.resources.ResourceLocation

/**
 * Every Cobblemon Poké Ball that has a crafting recipe, all forged Pixelmon-style: disc ->
 * hammered on the anvil into a lid -> lid + stone button + iron base.
 *
 * The disc recipe is Cobblemon's own ball recipe (same shape, materials and amount, with cooked
 * apricorns), so the better balls keep Cobblemon's cost; see docs/PLANO.md. Order: the 22
 * forgeable balls of Pixelmon 1.12.2, the Cobblemon apricorn balls, the special balls and the
 * ancient (Hisui) balls. Cherish and Ancient Origin have no recipe in Cobblemon and stay out.
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
    PARK("park"),
    DREAM("dream"),
    BEAST("beast"),
    MASTER("master"),
    ANCIENT_POKE("ancient_poke"),
    ANCIENT_GREAT("ancient_great"),
    ANCIENT_ULTRA("ancient_ultra"),
    ANCIENT_HEAVY("ancient_heavy"),
    ANCIENT_LEADEN("ancient_leaden"),
    ANCIENT_GIGATON("ancient_gigaton"),
    ANCIENT_FEATHER("ancient_feather"),
    ANCIENT_WING("ancient_wing"),
    ANCIENT_JET("ancient_jet"),
    ANCIENT_IVORY("ancient_ivory"),
    ANCIENT_CITRINE("ancient_citrine"),
    ANCIENT_VERDANT("ancient_verdant"),
    ANCIENT_AZURE("ancient_azure"),
    ANCIENT_ROSEATE("ancient_roseate"),
    ANCIENT_SLATE("ancient_slate"),
    ;

    /** Disc item path. The original Ballworks called the disc a "lid blank"; the id is kept. */
    val discPath: String get() = "${id}_lid_blank"

    val lidPath: String get() = "${id}_lid"

    /** The Cobblemon ball this lid is assembled into. */
    val ballId: ResourceLocation get() = ResourceLocation.fromNamespaceAndPath("cobblemon", "${id}_ball")
}
