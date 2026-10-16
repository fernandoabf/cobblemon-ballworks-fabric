package com.xaiver.cobbleballworks.content

import net.minecraft.resources.ResourceLocation

/**
 * Every Cobblemon Poké Ball, all forged Pixelmon-style: disc -> hammered on the anvil into a lid
 * -> lid + stone button + iron base.
 *
 * The disc recipe is Cobblemon's ball recipe (same shape and apricorns, cooked), with the material
 * in the middle, the discs per craft and the force on the anvil set by the ball's [CatchClass], so a
 * ball costs what it helps a capture; see docs/PLANO.md. Cherish and Ancient Origin have no recipe in
 * Cobblemon, so theirs are the forge's own in the same cross. Order: the 22 forgeable balls of
 * Pixelmon 1.12.2, the Cobblemon apricorn balls, the special balls and the ancient (Hisui) balls.
 */
enum class ForgeBall(val id: String, val catchClass: CatchClass) {
    POKE("poke", CatchClass.BASIC),
    GREAT("great", CatchClass.GOOD),
    ULTRA("ultra", CatchClass.STRONG),
    LEVEL("level", CatchClass.SITUATIONAL),
    MOON("moon", CatchClass.SITUATIONAL),
    FRIEND("friend", CatchClass.BASIC),
    LOVE("love", CatchClass.EXTREME),
    SAFARI("safari", CatchClass.GOOD),
    HEAVY("heavy", CatchClass.SITUATIONAL),
    FAST("fast", CatchClass.SITUATIONAL),
    REPEAT("repeat", CatchClass.SITUATIONAL),
    TIMER("timer", CatchClass.SITUATIONAL),
    NEST("nest", CatchClass.SITUATIONAL),
    NET("net", CatchClass.SITUATIONAL),
    DIVE("dive", CatchClass.SITUATIONAL),
    LUXURY("luxury", CatchClass.BASIC),
    HEAL("heal", CatchClass.BASIC),
    DUSK("dusk", CatchClass.SITUATIONAL),
    PREMIER("premier", CatchClass.BASIC),
    SPORT("sport", CatchClass.GOOD),
    QUICK("quick", CatchClass.EXTREME),
    LURE("lure", CatchClass.SITUATIONAL),
    CITRINE("citrine", CatchClass.BASIC),
    VERDANT("verdant", CatchClass.BASIC),
    AZURE("azure", CatchClass.BASIC),
    ROSEATE("roseate", CatchClass.BASIC),
    SLATE("slate", CatchClass.BASIC),
    PARK("park", CatchClass.STRONG),
    DREAM("dream", CatchClass.SITUATIONAL),
    BEAST("beast", CatchClass.EXTREME),
    MASTER("master", CatchClass.GUARANTEED),
    CHERISH("cherish", CatchClass.BASIC),
    ANCIENT_POKE("ancient_poke", CatchClass.BASIC),
    ANCIENT_GREAT("ancient_great", CatchClass.GOOD),
    ANCIENT_ULTRA("ancient_ultra", CatchClass.STRONG),
    ANCIENT_HEAVY("ancient_heavy", CatchClass.BASIC),
    ANCIENT_LEADEN("ancient_leaden", CatchClass.GOOD),
    ANCIENT_GIGATON("ancient_gigaton", CatchClass.STRONG),
    ANCIENT_FEATHER("ancient_feather", CatchClass.BASIC),
    ANCIENT_WING("ancient_wing", CatchClass.GOOD),
    ANCIENT_JET("ancient_jet", CatchClass.STRONG),
    ANCIENT_IVORY("ancient_ivory", CatchClass.BASIC),
    ANCIENT_CITRINE("ancient_citrine", CatchClass.BASIC),
    ANCIENT_VERDANT("ancient_verdant", CatchClass.BASIC),
    ANCIENT_AZURE("ancient_azure", CatchClass.BASIC),
    ANCIENT_ROSEATE("ancient_roseate", CatchClass.BASIC),
    ANCIENT_SLATE("ancient_slate", CatchClass.BASIC),
    ANCIENT_ORIGIN("ancient_origin", CatchClass.GUARANTEED),
    ;

    /** Disc item path. The original Ballworks called the disc a "lid blank"; the id is kept. */
    val discPath: String get() = "${id}_lid_blank"

    val lidPath: String get() = "${id}_lid"

    /** The Cobblemon ball this lid is assembled into. */
    val ballId: ResourceLocation get() = ResourceLocation.fromNamespaceAndPath("cobblemon", "${id}_ball")
}
