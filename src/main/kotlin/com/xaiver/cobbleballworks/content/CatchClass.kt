package com.xaiver.cobbleballworks.content

/**
 * How much a ball helps a capture, from its catch rate multiplier in Cobblemon 1.8.2 (PokeBalls and
 * CatchRateModifiers), and what its disc costs in the forge because of it: the metal in the middle
 * of the disc recipe follows the multiplier, and the strongest balls also give fewer discs per craft
 * and need more force per stage on the anvil. The recipe's shape and apricorns stay Cobblemon's.
 *
 * [tier] is the Cobblemon material tag in the middle (cobblemon:tier_N_poke_ball_materials: copper,
 * iron, gold, diamond), 0 for the guaranteed balls, whose recipe is Master Ball-like (netherite and
 * a nether star). Situational multipliers count at their best, when their condition holds.
 */
enum class CatchClass(val tier: Int, val discs: Int, val force: Int) {
    /** No catch bonus: Poké, Premier, Heal, Friend, Luxury, Cherish, the apricorn balls, plain ancient balls. */
    BASIC(1, 4, 5),

    /** x1.5: Great, Safari, Sport, Ancient Great, Leaden, Wing. */
    GOOD(2, 4, 5),

    /** x2 to x2.5: Ultra, Park, Ancient Ultra, Gigaton, Jet. */
    STRONG(3, 4, 5),

    /** x3 to x4 when their condition holds: Net, Dive, Dusk, Repeat, Nest, Timer, Level, Moon, Heavy, Lure, Fast, Dream. */
    SITUATIONAL(4, 4, 10),

    /** x5 to x8 when their condition holds: Quick (first turn), Love, Beast (Ultra Beasts). */
    EXTREME(4, 2, 10),

    /** Always catches: Master, Ancient Origin. */
    GUARANTEED(0, 1, 15),
}
