package com.xaiver.cobbleballworks.content

/**
 * The hammering rule of Pixelmon's anvil (TileEntityAnvil.blockHit, identical from 1.6.2 to
 * 1.12.2): every strike adds the hammer strength; when the accumulated force reaches the force of
 * a stage the excess is discarded and the piece advances one stage; after [STAGES] stages a disc
 * becomes a lid (or an iron disc becomes an iron base).
 */
object AnvilRules {
    /** Visual stages of a piece on the anvil: flat, stage 1, stage 2, then it turns into the result. */
    const val STAGES = 3

    /** Force needed per stage for discs (Pixelmon used 15 only for the aluminium bar). */
    const val DEFAULT_FORCE_PER_STAGE = 5

    /** Force needed to knock a finished piece off the anvil into the inventory by hammering it. */
    const val COLLECT_FORCE = 5

    /** Strikes to turn one piece into its result: 3 x ceil(force / strength). */
    fun strikesPerPiece(strength: Int, forcePerStage: Int = DEFAULT_FORCE_PER_STAGE): Int =
        STAGES * ceilDiv(forcePerStage, strength)

    private fun ceilDiv(a: Int, b: Int): Int = (a + b - 1) / b
}
